package learn.cwb.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device MacBookPro
 * @time 2021/8/10 11:26
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        if (msg.getHead().getType().equals(Msg.Head.Type.PING)) {
            Msg pong = Msg.withPong(msg.getHead().getReceiverId());
            ctx.writeAndFlush(pong);
        } else {
            ctx.fireChannelRead(msg0);
        }
    }
}
