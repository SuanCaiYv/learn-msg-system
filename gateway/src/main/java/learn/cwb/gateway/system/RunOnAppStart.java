package learn.cwb.gateway.system;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.gateway.handler.ForwardHandler;
import learn.cwb.gateway.handler.GlobalVariable;
import learn.cwb.gateway.zookeeper.ZookeeperOps;
import learn.cwb.gateway.zookeeper.impl.ZookeeperOpsImpl;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

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
        for (String address : nodes) {
            setNode(address);
        }
    }

    private static void watchedEvents(WatchedEvent event) {
        Set<String> now = new HashSet<>(ZOOKEEPER_OPS.getChild(SystemConstant.IM_NODE_PATH_PREFIX, RunOnAppStart::watchedEvents));
        if (event.getType().equals(Watcher.Event.EventType.NodeCreated)) {
            for (String address : GlobalVariable.SERVERS.keySet()) {
                if (!now.contains(address)) {
                    delNode(address);
                }
            }
        } else if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
            for (String address : now) {
                if (!GlobalVariable.SERVERS.containsKey(address)) {
                    setNode(address);
                }
            }
        } else {
            // do nothing
        }
    }

    private static void setNode(String address) {
        String[] tmp = address.split(":");
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(NativeUtils.workerEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("LengthBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.Head.HEAD_SIZE - 12, 0));
                        pipeline.addLast("ByteToMsgCodec", new Byte2MsgCodec());
                        pipeline.addLast("ForwardHandler", new ForwardHandler());
                    }
                })
                .connect(tmp[0], Integer.parseInt(tmp[1]))
                .syncUninterruptibly();
        GlobalVariable.SERVERS.put(address, channelFuture.channel());
    }

    private static void delNode(String address) {
        Channel channel = GlobalVariable.SERVERS.get(address);
        channel.close();
        GlobalVariable.SERVERS.remove(address);
    }
}
