package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/9 12:09 上午
 */
public class DirectHandler extends ChannelInboundHandlerAdapter {
    private static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = GlobalVariable.CHANNEL_MAP;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        long receiverId = msg.getHead().getReceiverId();
        Channel channel = CHANNEL_MAP.get(receiverId);
        if (channel != null) {
            channel.writeAndFlush(msg);
        }
    }
}
