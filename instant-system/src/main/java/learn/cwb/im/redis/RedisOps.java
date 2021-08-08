package learn.cwb.im.redis;

import io.lettuce.core.ScoredValue;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 2:43 下午
 * <br/>
 * 随便实例化，因为底层操作是线程安全的
 */
public interface RedisOps {
    void setObj(String key, Object value);

    void setObj(String key, Object value, Duration timeout);

    Object getObj(String key);

    void delObj(String key);

    void setZSet(String key, Object value, double score);

    void setZSet(String key, ScoredValue<Object> value);

    void setZSet(String key, Collection<ScoredValue<Object>> values);

    List<ScoredValue<Object>> getZSet(String key, double from, double to);

    List<Object> getZSetRaw(String key, double from, double to);

    /**
     * 只支持从最新开始到最旧计数
     * @param key key
     * @param offset offset
     * @param count count
     * @return list
     */
    List<ScoredValue<Object>> getZSet(String key, double offset, long count);

    List<Object> getZSetRaw(String key, double offset, long count);

    void delZSet(String key);
}
