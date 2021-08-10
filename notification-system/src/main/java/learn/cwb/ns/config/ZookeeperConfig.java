package learn.cwb.ns.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 2:38 下午
 */
public class ZookeeperConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

    private static class ZookeeperConfigHolder {
        private static final ZookeeperConfig instance = new ZookeeperConfig();
    }

    private final CuratorFramework client;

    private ZookeeperConfig() {
        client = CuratorFrameworkFactory
                .builder()
                .connectString("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183")
                .connectionTimeoutMs(3000)
                .retryPolicy(new RetryNTimes(5, 1000))
                .build();
        client.start();
    }

    public CuratorFramework getClient() {
        return client;
    }

    public static ZookeeperConfig getInstance() {
        return ZookeeperConfigHolder.instance;
    }

    public static void main(String[] args) throws Exception {
        getInstance().getClient()
                .delete()
                .forPath("/msl");
    }
}
