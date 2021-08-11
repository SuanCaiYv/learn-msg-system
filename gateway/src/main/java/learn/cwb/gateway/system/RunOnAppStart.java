package learn.cwb.gateway.system;

import learn.cwb.common.zookeeper.ZookeeperOps;
import learn.cwb.common.zookeeper.impl.ZookeeperOpsImpl;
import learn.cwb.gateway.handler.GlobalVariable;
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
        List<String> nodes1 = zookeeperOps.getChild(SystemConstant.IM_NODE_PATH_PREFIX, RunOnAppStart::watchedIMEvents);
        GlobalVariable.AVAILABLE_IM_SERVERS.addAll(nodes1);
        List<String> nodes2 = zookeeperOps.getChild(SystemConstant.NS_NODE_PATH_PREFIX, RunOnAppStart::watchedNSEvents);
        GlobalVariable.AVAILABLE_NS_SERVERS.addAll(nodes2);
    }

    private static void watchedIMEvents(WatchedEvent event) {
        Set<String> now = new HashSet<>(ZOOKEEPER_OPS.getChild(SystemConstant.IM_NODE_PATH_PREFIX, RunOnAppStart::watchedIMEvents));
        GlobalVariable.AVAILABLE_IM_SERVERS.removeIf(address -> !now.contains(address));
        GlobalVariable.AVAILABLE_IM_SERVERS.addAll(now);
    }

    private static void watchedNSEvents(WatchedEvent event) {
        Set<String> now = new HashSet<>(ZOOKEEPER_OPS.getChild(SystemConstant.NS_NODE_PATH_PREFIX, RunOnAppStart::watchedNSEvents));
        GlobalVariable.AVAILABLE_NS_SERVERS.removeIf(address -> !now.contains(address));
        GlobalVariable.AVAILABLE_NS_SERVERS.addAll(now);
    }
}
