package learn.cwb.im.handler;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 2:05 下午
 */
public class GlobalVariable {
    public static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, Channel> OTHER_SERVERS = new ConcurrentHashMap<>();
}
