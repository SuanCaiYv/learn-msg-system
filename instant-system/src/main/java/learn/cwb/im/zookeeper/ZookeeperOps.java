package learn.cwb.im.zookeeper;

import org.apache.curator.framework.api.CuratorWatcher;

import java.util.List;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 2:38 下午
 */
public interface ZookeeperOps {
    void addTmpNode(String path);

    void addTmpNode(String path, byte[] data);

    void delTmpNode(String path);

    byte[] readTmpNode(String path);

    void addNode(String path);

    void addNode(String path, byte[] data);

    void delNode(String path);

    byte[] readNode(String path);

    List<String> getChild(String path, CuratorWatcher watcher);

    List<String> getChild(String path);
}
