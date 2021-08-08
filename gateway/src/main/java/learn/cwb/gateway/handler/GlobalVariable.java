package learn.cwb.gateway.handler;

import io.netty.channel.Channel;

import java.util.TreeMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 8:51 下午
 */
public class GlobalVariable {
    public static final TreeMap<String, Channel> SERVERS = new TreeMap<>();
}
