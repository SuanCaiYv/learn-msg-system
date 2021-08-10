package learn.cwb.common.config;

import learn.cwb.common.config.kafka.MsgRcdSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 1:02 上午
 */
public class KafKaConfig {
    static final String bootstrapServer = "127.0.0.1:9091,127.0.0.1:9092,127.0.0.1:9093";

    private final KafkaProducer<String, Object> producer;

    private KafKaConfig() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 3000);
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024 * 1024 * 10);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1024 * 10);
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MsgRcdSerializer.class);
        producer = new KafkaProducer<>(properties);
    }

    private static final class KafKaConfigHolder {
        // 类变量只可能被赋值一次
        // 类变量的初始化发生在类加载的时候，随后调用clinit<>方法进行类变量赋值
        private static final KafKaConfig config = new KafKaConfig();
    }

    public static KafKaConfig getInstance() {
        // 此时碰到KafKaConfigHolder会触发KafKaConfigHolder的加载，然后才是读取config的值。
        return KafKaConfigHolder.config;
    }

    public KafkaProducer<String, Object> getProducer() {
        return producer;
    }
}
