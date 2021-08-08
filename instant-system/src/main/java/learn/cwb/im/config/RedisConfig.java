package learn.cwb.im.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 1:01 上午
 */
public class RedisConfig {
    static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    private final RedisCommands<String, Object> redisCommands;

    private RedisConfig() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final RedisClient redisClient = RedisClient.create("redis://127.0.0.1:10200");
        StatefulRedisConnection<String, Object> connection = redisClient.connect(new RedisCodec<>() {

            @Override
            public String decodeKey(ByteBuffer bytes) {
                return StandardCharsets.UTF_8.decode(bytes).toString();
            }

            @Override
            public Object decodeValue(ByteBuffer bytes) {
                String val = StandardCharsets.UTF_8.decode(bytes).toString();
                try {
                    return objectMapper.readValue(val, Object.class);
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage());
                    return null;
                }
            }

            @Override
            public ByteBuffer encodeKey(String key) {
                return StandardCharsets.UTF_8.encode(key);
            }

            @Override
            public ByteBuffer encodeValue(Object value) {
                try {
                    String json = objectMapper.writeValueAsString(value);
                    return StandardCharsets.UTF_8.encode(json);
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage());
                    return null;
                }
            }
        });
        redisCommands = connection.sync();
    }

    private static class RedisConfigHolder {
        private static final RedisConfig redisConfig = new RedisConfig();
    }

    public static RedisConfig getInstance() {
        return RedisConfigHolder.redisConfig;
    }

    public RedisCommands<String, Object> getRedisCommands() {
        return redisCommands;
    }
}
