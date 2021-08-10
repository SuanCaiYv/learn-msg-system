package learn.cwb.common.zookeeper.impl;

import learn.cwb.common.config.ZookeeperConfig;
import learn.cwb.common.zookeeper.ZookeeperOps;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 3:20 下午
 */
public class ZookeeperOpsImpl implements ZookeeperOps {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperOpsImpl.class);

    private static final ZookeeperConfig zookeeperConfig = ZookeeperConfig.getInstance();

    @Override
    public void addTmpNode(String path) {
        try {
            zookeeperConfig.getClient()
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("create temp node({}) failed: {}", path, e.getMessage());
        }
    }

    @Override
    public void addTmpNode(String path, byte[] data) {
        try {
            zookeeperConfig.getClient()
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path, data);
        } catch (Exception e) {
            LOGGER.error("create temp node({}) failed: {}", path, e.getMessage());
        }
    }

    @Override
    public void delTmpNode(String path) {
        try {
            zookeeperConfig.getClient()
                    .delete()
                    .deletingChildrenIfNeeded()
                    .forPath(path);
        } catch (Exception e) {
            LOGGER.error("delete tmp node({}) failed: {}", path, e.getMessage());
        }
    }

    @Override
    public byte[] readTmpNode(String path) {
        try {
            return zookeeperConfig.getClient()
                    .getData()
                    .forPath(path);
        } catch (Exception e) {
            LOGGER.info("from {} read data failed: {}", path, e.getMessage());
            return null;
        }
    }

    @Override
    public void addNode(String path) {
        try {
            zookeeperConfig.getClient()
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path);
        } catch (Exception e) {
            LOGGER.error("create node({}) failed: {}", path, e.getMessage());
        }
    }

    @Override
    public void addNode(String path, byte[] data) {
        try {
            zookeeperConfig.getClient()
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, data);
        } catch (Exception e) {
            LOGGER.error("create node({}) failed: {}", path, e.getMessage());
        }
    }

    @Override
    public void delNode(String path) {
        delTmpNode(path);
    }

    @Override
    public byte[] readNode(String path) {
        return readTmpNode(path);
    }

    @Override
    public List<String> getChild(String path, CuratorWatcher watcher) {
        try {
            return zookeeperConfig.getClient()
                    .getChildren()
                    .usingWatcher(watcher)
                    .forPath(path);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<String> getChild(String path) {
        try {
            return zookeeperConfig.getClient()
                    .getChildren()
                    .forPath(path);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }
}
