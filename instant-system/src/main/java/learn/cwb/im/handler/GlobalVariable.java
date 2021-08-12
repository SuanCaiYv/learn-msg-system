package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutorGroup;
import learn.cwb.common.util.NativeUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 2:05 下午
 */
public class GlobalVariable {
    public static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, Channel> OTHER_SERVERS = new ConcurrentHashMap<>();

    public static final ExecutorService THREAD_POOL = NativeUtils.defaultExecutorService();

    public static final EventExecutorGroup EVENT_EXECUTOR_GROUP = NativeUtils.defaultEventExecutorGroup();

    public static Integer PORT = null;
}
