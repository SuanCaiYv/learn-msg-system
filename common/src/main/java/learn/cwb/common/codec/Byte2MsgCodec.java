package learn.cwb.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import learn.cwb.common.transport.Msg;

import java.util.List;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:30 上午
 */
public class Byte2MsgCodec extends ByteToMessageCodec<Msg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, ByteBuf out) throws Exception {
        msg.serialize(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Msg msg = Msg.deserialize(in);
        out.add(msg);
    }
}
