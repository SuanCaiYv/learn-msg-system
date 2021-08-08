package learn.cwb.gateway.redis.impl;

import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.SetArgs;
import learn.cwb.gateway.config.RedisConfig;
import learn.cwb.gateway.redis.RedisOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 8:37 下午
 */
public class RedisOpsImpl implements RedisOps {
    static final Logger LOGGER = LoggerFactory.getLogger(RedisOpsImpl.class);

    static final RedisConfig redisConfig = RedisConfig.getInstance();

    @Override
    public void setObj(String key, Object value) {
        redisConfig.getRedisCommands().set(key, value);
    }

    @Override
    public void setObj(String key, Object value, Duration timeout) {
        redisConfig.getRedisCommands().set(key, value, SetArgs.Builder.ex(timeout));
    }

    @Override
    public Object getObj(String key) {
        return redisConfig.getRedisCommands().get(key);
    }

    @Override
    public void delObj(String key) {
        redisConfig.getRedisCommands().del(key);
    }

    @Override
    public void setZSet(String key, Object value, double score) {
        redisConfig.getRedisCommands().zadd(key, score, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setZSet(String key, ScoredValue<Object> value) {
        redisConfig.getRedisCommands().zadd(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setZSet(String key, Collection<ScoredValue<Object>> values) {
        values.forEach(p -> redisConfig.getRedisCommands().zadd(key, p));
    }

    @Override
    public List<ScoredValue<Object>> getZSet(String key, double from, double to) {
        return redisConfig.getRedisCommands().zrangebyscoreWithScores(key, Range.create(from, to));
    }

    @Override
    public List<Object> getZSetRaw(String key, double from, double to) {
        return redisConfig.getRedisCommands().zrangebyscore(key, Range.create(from, to));
    }

    @Override
    public List<ScoredValue<Object>> getZSet(String key, double offset, long count) {
        return redisConfig.getRedisCommands().zrangebyscoreWithScores(key, Range.create(offset, offset).lte(offset), Limit.from(count));
    }

    @Override
    public List<Object> getZSetRaw(String key, double offset, long count) {
        return redisConfig.getRedisCommands().zrangebyscore(key, Range.create(offset, offset).lte(offset), Limit.from(count));
    }

    @Override
    public void delZSet(String key) {
        redisConfig.getRedisCommands().del(key);
    }
}
