package learn.cwb.gateway.system;

import learn.cwb.gateway.handler.GlobalVariable;
import learn.cwb.gateway.zookeeper.ZookeeperOps;
import learn.cwb.gateway.zookeeper.impl.ZookeeperOpsImpl;
import org.apache.zookeeper.WatchedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 8:47 下午
 */
public class RunOnAppStart {
    private static final ZookeeperOps ZOOKEEPER_OPS = new ZookeeperOpsImpl();

    public static void hook() {
        refreshAvailableNodes();
    }

    private static void refreshAvailableNodes() {
        ZookeeperOps zookeeperOps = new ZookeeperOpsImpl();
        List<String> nodes = zookeeperOps.getChild(SystemConstant.IM_NODE_PATH_PREFIX, RunOnAppStart::watchedEvents);
        GlobalVariable.AVAILABLE_SERVERS.addAll(nodes);
    }

    private static void watchedEvents(WatchedEvent event) {
        Set<String> now = new HashSet<>(ZOOKEEPER_OPS.getChild(SystemConstant.IM_NODE_PATH_PREFIX, RunOnAppStart::watchedEvents));
        GlobalVariable.AVAILABLE_SERVERS.removeIf(address -> !now.contains(address));
        GlobalVariable.AVAILABLE_SERVERS.addAll(now);
    }
}
