package learn.cwb.common.transport;

import lombok.*;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 9:02 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgRcd {
    // sender-receiver的形式
    private String queueId;

    // 毫秒级
    private long realTime;

    private Msg msg;

    public static MsgRcd withMsg(Msg msg) {
        String queueId = null;
        if (msg.getHead().getSenderId() <= msg.getHead().getReceiverId()) {
            queueId = String.format("%020d", msg.getHead().getSenderId()) + "-" + String.format("%020d", msg.getHead().getReceiverId());
        } else {
            queueId = String.format("%020d", msg.getHead().getReceiverId()) + "-" + String.format("%020d", msg.getHead().getSenderId());
        }
        long realTime = msg.getHead().getArrivedTime();
        return MsgRcd.builder()
                .queueId(queueId)
                .realTime(realTime)
                .msg(msg)
                .build();
    }
}
