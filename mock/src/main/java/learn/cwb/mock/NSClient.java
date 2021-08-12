package learn.cwb.mock;

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
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/11 8:04 下午
 */
public class NSClient {
    static final Logger LOGGER = LoggerFactory.getLogger(NSClient.class);

    static class Client1 {
        public static void main(String[] args) {
            work(1);
        }
    }

    static class Client2 {
        public static void main(String[] args) {
            work(2);
        }
    }

    static class Client3 {
        public static void main(String[] args) {
            work(3);
        }
    }

    static class Client4 {
        public static void main(String[] args) {
            work(4);
        }
    }

    static class Client5 {
        public static void main(String[] args) {
            work(5);
        }
    }

    private static void work(long senderId) {
        String address = connect(senderId);
        LOGGER.info("连接到: {}服务器", address);
        String[] tmp = address.split(":");
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(NativeUtils.workerEventLoopGroup())
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
                                if (msg.getHead().getType().equals(Msg.Head.Type.NOTIFICATION)) {
                                    LOGGER.info("{}读到了通知: {}", msg.getHead().getReceiverId(), new String(msg.getBody().getBody()));
                                }
                            }
                        });
                    }
                })
                .connect(tmp[0], Integer.parseInt(tmp[1]))
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        LOGGER.info("我是{}", ((InetSocketAddress) channel.localAddress()).getPort());
        Msg establishMsg = Msg.withEstablish(senderId);
        channel.writeAndFlush(establishMsg);
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
        Msg establishedMsg = Msg.withNSEstablish(senderId);
        channel.writeAndFlush(establishedMsg);
        while (address[0] == null) {
            LockSupport.parkNanos(100000);
        }
        return address[0];
    }
}
