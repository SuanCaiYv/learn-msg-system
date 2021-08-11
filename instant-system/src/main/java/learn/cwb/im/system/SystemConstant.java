package learn.cwb.im.system;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:40 下午
 */
public class SystemConstant {
    public static final int MY_PORT = 10422;

    public static final long IDLE_TIME = 120;

    public static final long HEARTBEAT_INTERVAL = 10;

    public static final int HEARTBEAT_COUNT = 5;

    public static final String HEARTBEAT_REMAIN_COUNT_NAME = "HEARTBEAT_COUNT_NAME";

    public static final String HEARTBEAT_CONTINUATION = "HEARTBEAT_CONTINUATION";

    public static final String CHANNEL_IDENTIFIER = "SENDER_ID";

    public static final String USER_IN_CLUSTER_PREFIX = "USER_IM_IN_CLUSTER_";

    public static final String USER_INBOX_PREFIX = "USER_INBOX_";

    public static final String IM_NODE_PATH_PREFIX = "/MSG/IM/NODE";
}
