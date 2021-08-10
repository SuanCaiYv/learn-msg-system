package learn.cwb.common.kafka;

import learn.cwb.common.system.SystemConstant;
import learn.cwb.common.transport.MsgRcd;
import learn.cwb.common.config.KafKaConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:19 上午
 */
public class KafkaOpsImpl implements KafkaOps {
    static final Logger LOGGER = LoggerFactory.getLogger(KafkaOpsImpl.class);

    private static final KafKaConfig kafkaConfig = KafKaConfig.getInstance();

    @Override
    public void put(MsgRcd msgRcd) {
        kafkaConfig.getProducer().send(new ProducerRecord<>(SystemConstant.KAFKA_TOPIC, msgRcd));
    }

    @Override
    public void put(String key, MsgRcd msgRcd) {
        kafkaConfig.getProducer().send(new ProducerRecord<>(SystemConstant.KAFKA_TOPIC, key, msgRcd));
    }
}
