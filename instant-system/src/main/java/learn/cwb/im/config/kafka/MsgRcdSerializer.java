package learn.cwb.im.config.kafka;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import learn.cwb.common.transport.MsgRcd;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 12:21 上午
 */
public class MsgRcdSerializer implements Serializer<MsgRcd> {
    @Override
    public byte[] serialize(String topic, MsgRcd data) {
        ByteBuf src = Unpooled.buffer();
        src.writeBytes(data.getQueueId().getBytes(StandardCharsets.UTF_8));
        src.writeLong(data.getRealTime());
        data.getMsg().serialize(src);
        if (src.hasArray()) {
            return src.array();
        } else {
            byte[] bytes = new byte[src.readableBytes()];
            src.readBytes(bytes, 0, bytes.length);
            return bytes;
        }
    }
}
