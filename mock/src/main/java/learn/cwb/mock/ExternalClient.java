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

import java.util.Scanner;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/12 10:43 下午
 */
public class ExternalClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalClient.class);

    static class Run1 {
        public static void main(String[] args) {
            sendNotification();
        }
    }

    private static void sendNotification() {
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
                        pipeline.addLast(new HeartbeatHandler());
                    }
                })
                .connect("127.0.0.1", 10450)
                .syncUninterruptibly();
        Channel channel = channelFuture.channel();
        Msg establish = Msg.withEstablish(0);
        channel.writeAndFlush(establish);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String str = scanner.nextLine();
            Msg msg = Msg.withText(str);
            msg.getHead().setType(Msg.Head.Type.NOTIFICATION);
            msg.getHead().setSenderId(0);
            msg.getHead().setReceiverId(-1);
            channel.writeAndFlush(msg);
        }
    }
}
