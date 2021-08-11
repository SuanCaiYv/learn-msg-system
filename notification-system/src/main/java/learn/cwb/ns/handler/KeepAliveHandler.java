package learn.cwb.ns.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import learn.cwb.common.redis.RedisOps;
import learn.cwb.common.redis.impl.RedisOpsImpl;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.ns.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/11 3:00 下午
 */
public class KeepAliveHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeepAliveHandler.class);

    private static final Msg HEARTBEAT_PACKAGE = Msg.withPing();

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

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
        GlobalVariable.CHANNEL_MAP.remove(id);
        REDIS_OPS.delObj(SystemConstant.USER_IN_CLUSTER_PREFIX + id);
    }

    private static void channelAdd(Channel channel) {
        long id = (long) channel.attr(AttributeKey.valueOf(SystemConstant.CHANNEL_IDENTIFIER)).get();
        GlobalVariable.CHANNEL_MAP.put(id, channel);
        REDIS_OPS.setObj(SystemConstant.USER_IN_CLUSTER_PREFIX + id, NativeUtils.myIP());
        System.out.println(GlobalVariable.CHANNEL_MAP.get(id));
    }
}
