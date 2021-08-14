package learn.cwb.mock.pressure;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.handler.HeartbeatHandler;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/13 8:12 下午
 */
public class IMTest {
    static Random random = new Random();

    static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger("logger");
        int length1 = 10;
        int length2 = 1000;
        for (int i = 1; i <= length1; ++ i) {
            EventLoopGroup eventLoopGroup = NativeUtils.singleEventLoopGroup();
            final Channel[] channels = new Channel[length2];
            for (int j = 1; j <= length2; ++ j) {
                Channel channel = work((long) i * length1 + j, eventLoopGroup, logger);
                channels[j-1] = channel;
            }
            final int ii = i;
//            executorService.execute(() -> {
//                for (;;) {
//                    LockSupport.parkNanos(Duration.ofMillis(1000).toNanos());
//                    for (Channel channel : channels) {
//                        for (int k = 0; k < 100; ++ k) {
//                            String input = UUID.randomUUID().toString().substring(0, 10);
//                            Msg src = Msg.withText(input);
//                            src.getHead().setSenderId(ii);
//                            long receiverId = Math.abs(random.nextInt()) % (length1 * length2);
//                            if (receiverId == ii) {
//                                ++ receiverId;
//                            }
//                            src.getHead().setReceiverId(receiverId);
//                            channel.writeAndFlush(src);
//                        }
//                    }
//                }
//            });
            System.out.println(i);
        }
        System.out.println("done");
        LockSupport.park();
    }

    private static Channel work(long senderId, EventLoopGroup eventLoopGroup, Logger logger) {
        String address = connect(senderId);
        // logger.info("连接到: {}服务器", address);
        String[] tmp = address.split(":");
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(eventLoopGroup)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.EMPTY_SIZE - 12, 0));
                        pipeline.addLast(new Byte2MsgCodec());
                        pipeline.addLast(new HeartbeatHandler());
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
                                Msg msg = (Msg) msg0 ;
                                if (msg.getHead().getType().equals(Msg.Head.Type.TEXT)) {
                                    // logger.info("用户: 👉{}👈从👉{}👈读到了: {}", msg.getHead().getReceiverId(), msg.getHead().getSenderId(), new String(msg.getBody().getBody()));
                                }
                            }
                        });
                    }
                })
                .connect(tmp[0], Integer.parseInt(tmp[1]))
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        // logger.info("我是{}", ((InetSocketAddress) channel.localAddress()).getPort());
        Msg establishMsg = Msg.withIMEstablish(senderId);
        channel.writeAndFlush(establishMsg);
        return channel;
    }

    private static String connect(long senderId) {
        final String[] address = {null};
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(NativeUtils.singleEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.EMPTY_SIZE - 12, 0));
                        pipeline.addLast(new Byte2MsgCodec());
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
                                Msg msg = (Msg) msg0 ;
                                if (msg.getHead().getType().equals(Msg.Head.Type.ESTABLISH)) {
                                    address[0] = new String(msg.getBody().getBody());
                                }
                            }
                        });
                    }
                })
                .connect("127.0.0.1", 10410)
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        Msg establishedMsg = Msg.withIMEstablish(senderId);
        channel.writeAndFlush(establishedMsg);
        while (address[0] == null) {
            LockSupport.parkNanos(100000);
        }
        return address[0];
    }
}
