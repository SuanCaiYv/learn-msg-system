package learn.cwb.external.controller;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.handler.HeartbeatHandler;
import learn.cwb.common.kafka.KafkaOps;
import learn.cwb.common.kafka.impl.KafkaOpsImpl;
import learn.cwb.common.redis.RedisOps;
import learn.cwb.common.redis.impl.RedisOpsImpl;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.common.zookeeper.ZookeeperOps;
import learn.cwb.common.zookeeper.impl.ZookeeperOpsImpl;
import learn.cwb.external.system.SystemConstant;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/12 9:00 下午
 */
public class NSController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSController.class);

    private static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Channel> SERVERS = new ConcurrentHashMap<>();

    private static final ExecutorService THREAD_POOL = NativeUtils.defaultExecutorService();

    private static final EventExecutorGroup EVENT_EXECUTOR_GROUP = NativeUtils.defaultEventExecutorGroup();

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    private static final ZookeeperOps ZOOKEEPER_OPS = new ZookeeperOpsImpl();

    private static final KafkaOps KAFKA_OPS = new KafkaOpsImpl();

    private NSController() {
        refreshAvailableNodes();
    }

    private static class NSControllerHolder{
        private static final NSController instance = new NSController();
    }

    public static NSController getInstance() {
        return NSController.NSControllerHolder.instance;
    }

    public void work() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap
                .channel(NativeUtils.serverChannel())
                .group(NativeUtils.bossEventLoopGroup(), NativeUtils.workerEventLoopGroup())
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.EMPTY_SIZE - 12, 0));
                        pipeline.addLast(new Byte2MsgCodec());
                        pipeline.addLast(new IdleStateHandler(0, 0, SystemConstant.IDLE_TIME, TimeUnit.SECONDS));
                        pipeline.addLast(new HeartbeatHandler());
                        pipeline.addLast(new KeepAliveHandler());
                        pipeline.addLast(new NSHandler());
                        pipeline.addLast(EVENT_EXECUTOR_GROUP, new ForwardHandler());
                    }
                })
                .bind("127.0.0.1", SystemConstant.NS_PORT)
                .syncUninterruptibly();
    }

    private static void refreshAvailableNodes() {
        ZookeeperOps zookeeperOps = new ZookeeperOpsImpl();
        List<String> nodes = zookeeperOps.getChild(SystemConstant.NS_NODE_PATH_PREFIX, NSController::watchedEvents);
        for (String address : nodes) {
            setNSNode(address);
        }
    }

    private static void watchedEvents(WatchedEvent event) {
        Set<String> now = new HashSet<>(ZOOKEEPER_OPS.getChild(SystemConstant.NS_NODE_PATH_PREFIX, NSController::watchedEvents));
        for (String address : SERVERS.keySet()) {
            if (!now.contains(address)) {
                delNSNode(address);
            }
        }
        for (String address : now) {
            if (address.equals(NativeUtils.myIP())) {
                continue;
            }
            if (!SERVERS.containsKey(address)) {
                setNSNode(address);
            }
        }
    }

    private static void setNSNode(String address) {
        String[] tmp = address.split(":");
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(NativeUtils.workerEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("LengthBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.Head.HEAD_SIZE - 12, 0));
                        pipeline.addLast("ByteToMsgCodec", new Byte2MsgCodec());
                        pipeline.addLast("HeartbeatHandler", new HeartbeatHandler());
                        pipeline.addLast("DirectHandler", new NSHandler());
                    }
                })
                .connect(tmp[0], Integer.parseInt(tmp[1]))
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        LOGGER.info("我是{}", ((InetSocketAddress) channel.localAddress()).getPort());
        channel.writeAndFlush(Msg.withEstablish(-REDIS_OPS.getAutoIncrementId()));
        SERVERS.put(address, channelFuture.channel());
    }

    private static void delNSNode(String address) {
        Channel channel = SERVERS.get(address);
        channel.close();
        SERVERS.remove(address);
    }

    private static class ForwardHandler extends ChannelInboundHandlerAdapter {
        private static final Logger LOGGER = LoggerFactory.getLogger(ForwardHandler.class);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
            Msg msg = (Msg) msg0;
            long senderId = msg.getHead().getSenderId();
            long receiverId = msg.getHead().getReceiverId();
            String address = (String) REDIS_OPS.getObj(SystemConstant.USER_IN_NS_CLUSTER_PREFIX + receiverId);
            if (address == null) {
                LOGGER.info("用户{}不在线", receiverId);
            } else {
                Channel userChannel = SERVERS.get(address);
                userChannel.writeAndFlush(msg);
            }
        }
    }

    private static class NSHandler extends ChannelInboundHandlerAdapter {
        private static final Logger LOGGER = LoggerFactory.getLogger(NSHandler.class);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg0) {
            Msg msg = (Msg) msg0;
            if (!msg.getHead().getType().equals(Msg.Head.Type.NOTIFICATION)) {
                ctx.fireChannelRead(msg0);
                return ;
            }
            long senderId = msg.getHead().getSenderId();
            long receiverId = msg.getHead().getReceiverId();
            // 全局通知
            if (receiverId < 0) {
                SERVERS.values().forEach(channel -> {
                    channel.writeAndFlush(msg);
                });
            } else if (!CHANNEL_MAP.containsKey(receiverId)) {
                LOGGER.info("对{}的请求转发到其他服务器处理", receiverId);
                ctx.fireChannelRead(msg0);
            } else {
                Channel userChannel = CHANNEL_MAP.get(receiverId);
                userChannel.writeAndFlush(msg);
            }
        }
    }

    private static class KeepAliveHandler extends ChannelInboundHandlerAdapter {
        private static final Logger LOGGER = LoggerFactory.getLogger(KeepAliveHandler.class);

        private static final Msg HEARTBEAT_PACKAGE = Msg.withPing();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().attr(AttributeKey.valueOf(SystemConstant.CHANNEL_IDENTIFIER)).set(-1L);
            ctx.channel().attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_REMAIN_COUNT_NAME)).set(SystemConstant.HEARTBEAT_COUNT);
            ctx.channel().attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_CONTINUATION)).set(true);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            channelRmv(channel);
            channel.close().addListener((ChannelFutureListener) future -> {
                InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
                LOGGER.warn("Channel({})已被关闭.", (address.getHostName() + ":" + address.getPort()));
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
            Msg msg = (Msg) msg0;
            long senderId = msg.getHead().getSenderId();
            long receiverId = msg.getHead().getReceiverId();
            Channel channel = ctx.channel();
            switch (msg.getHead().getType()) {
                // 对于心跳包的回复
                case PONG -> {
                    if (LOGGER.isDebugEnabled()) {
                        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
                        LOGGER.debug("Channel({})发送了一个Pong包.", (address.getHostName() + ":" + address.getPort()));
                    }
                    channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_CONTINUATION)).set(false);
                }
                // 初始连接建立
                case ESTABLISH -> {
                    channel.attr(AttributeKey.valueOf(SystemConstant.CHANNEL_IDENTIFIER)).set(senderId);
                    channelAdd(channel);
                }
                // 关闭连接
                case CLOSE -> {
                    channelRmv(channel);
                    channel.close().addListener((ChannelFutureListener) future -> {
                        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
                        LOGGER.warn("Channel({})想要关闭连接.", (address.getHostName() + ":" + address.getPort()));
                    });
                }
                default -> {
                    ctx.fireChannelRead(msg0);
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                if (LOGGER.isDebugEnabled()) {
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    LOGGER.debug("Channel({})已经有一段时间没有活动了，我们想要发送一个心跳包进行探活.", (address.getHostName() + ":" + address.getPort()));
                }
                sendHeartbeatPackage(ctx);
            } else {
                ctx.fireUserEventTriggered(evt);
            }
        }

        private static void sendHeartbeatPackage(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            int remainCount = (int) channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_REMAIN_COUNT_NAME)).get();
            boolean heartbeatContinuation = (boolean) channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_CONTINUATION)).get();
            if (remainCount > 0 && heartbeatContinuation) {
                remainCount --;
                channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_REMAIN_COUNT_NAME)).set(remainCount);
                ctx.writeAndFlush(HEARTBEAT_PACKAGE);
                channel.eventLoop().schedule(() -> sendHeartbeatPackage(ctx), SystemConstant.HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
            } else if (!heartbeatContinuation) {
                if (LOGGER.isDebugEnabled()) {
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    LOGGER.debug("Channel({})在第{}次尝试后收到了Pong包.", (address.getHostName() + ":" + address.getPort()), remainCount);
                }
                channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_REMAIN_COUNT_NAME)).set(SystemConstant.HEARTBEAT_COUNT);
                channel.attr(AttributeKey.valueOf(SystemConstant.HEARTBEAT_CONTINUATION)).set(true);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    LOGGER.debug("Channel({})下线了，所以我们决定关闭这个Channel.", (address.getHostName() + ":" + address.getPort()));
                }
                channelRmv(channel);
                channel.close().addListener((ChannelFutureListener) future -> {
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    LOGGER.info("Channel({})已被关闭.", (address.getHostName() + ":" + address.getPort()));
                });
            }
        }

        private static void channelRmv(Channel channel) {
            long id = (long) channel.attr(AttributeKey.valueOf(SystemConstant.CHANNEL_IDENTIFIER)).get();
            CHANNEL_MAP.remove(id);
            THREAD_POOL.execute(() -> {
                REDIS_OPS.delObj(SystemConstant.USER_IN_NS_CLUSTER_PREFIX + id);
            });
        }

        private static void channelAdd(Channel channel) {
            long id = (long) channel.attr(AttributeKey.valueOf(SystemConstant.CHANNEL_IDENTIFIER)).get();
            CHANNEL_MAP.put(id, channel);
            THREAD_POOL.execute(() -> {
                REDIS_OPS.setObj(SystemConstant.USER_IN_NS_CLUSTER_PREFIX + id, NativeUtils.myAddress(SystemConstant.NS_PORT));
            });
        }
    }
}
