package learn.cwb.im;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/9 12:59 上午
 */
public class Client {

    public static void main(String[] args) {
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
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .connect("127.0.0.1", 10410)
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        Msg msg = Msg.withPing();
        Msg.Head head = msg.getHead();
        head.setType(Msg.Head.Type.ESTABLISH);
        head.setSenderId(1L);
        channel.writeAndFlush(msg);
    }
}
