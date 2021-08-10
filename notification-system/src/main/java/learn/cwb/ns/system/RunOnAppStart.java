package learn.cwb.ns.system;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.handler.HeartbeatHandler;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.common.zookeeper.ZookeeperOps;
import learn.cwb.common.zookeeper.impl.ZookeeperOpsImpl;
import learn.cwb.ns.handler.GlobalVariable;
import org.apache.zookeeper.WatchedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 4:02 下午
 */
public class RunOnAppStart {
    private static final ZookeeperOps ZOOKEEPER_OPS = new ZookeeperOpsImpl();

    public static void hookBeforeStart() {
        refreshAvailableNodes();
    }

    public static void hookAfterStart() {
        registerWithZookeeper();
    }

    private static void registerWithZookeeper() {
        ZookeeperOps zookeeperOps = new ZookeeperOpsImpl();
        String myAddress = NativeUtils.myIP() + ":" + SystemConstant.MY_PORT;
        zookeeperOps.addTmpNode(SystemConstant.IM_NODE_PATH_PREFIX + "/" + myAddress);
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
        for (String address : GlobalVariable.OTHER_SERVERS.keySet()) {
            if (!now.contains(address)) {
                delNode(address);
            }
        }
        for (String address : now) {
            if (address.equals(NativeUtils.myIP())) {
                continue;
            }
            if (!GlobalVariable.OTHER_SERVERS.containsKey(address)) {
                setNode(address);
            }
        }
    }

    private static void setNode(String address) {
        System.out.println("run" + address);
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
                        pipeline.addLast("HeartbeatHandler", new HeartbeatHandler());
                        pipeline.addLast("ForwardHandler", new InstantMsgHandler());
                        pipeline.addLast("MaybeError", new ForwardHandler());
                    }
                })
                .connect(tmp[0], Integer.parseInt(tmp[1]))
                .syncUninterruptibly();
        GlobalVariable.OTHER_SERVERS.put(address, channelFuture.channel());
    }

    private static void delNode(String address) {
        Channel channel = GlobalVariable.OTHER_SERVERS.get(address);
        channel.close();
        GlobalVariable.OTHER_SERVERS.remove(address);
    }
}
