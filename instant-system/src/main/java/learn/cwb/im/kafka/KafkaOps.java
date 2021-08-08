package learn.cwb.im.kafka;

import learn.cwb.common.transport.MsgRcd;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 8:38 下午
 */
public interface KafkaOps {
    void put(MsgRcd msgRcd);

    void put(String key, MsgRcd msgRcd);
}
