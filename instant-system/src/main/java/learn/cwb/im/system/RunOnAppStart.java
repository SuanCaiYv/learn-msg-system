package learn.cwb.im.system;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.im.handler.DirectHandler;
import learn.cwb.im.handler.GlobalVariable;
import learn.cwb.im.zookeeper.ZookeeperOps;
import learn.cwb.im.zookeeper.impl.ZookeeperOpsImpl;

import java.net.InetAddress;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 4:02 下午
 */
public class RunOnAppStart {
    public static void hookBeforeStart() {
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap
                .channel(NativeUtils.clientChannel())
                .group(NativeUtils.workerEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("LengthBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.Head.HEAD_SIZE - 12, 0));
                        pipeline.addLast(new Byte2MsgCodec());
                        pipeline.addLast(new DirectHandler());
                    }
                })
                .connect("127.0.0.1", 10410)
                .syncUninterruptibly();
        GlobalVariable.GATEWAY = channelFuture.channel();
    }

    public static void hookAfterStart() {
        registerWithZookeeper();
    }

    private static void registerWithZookeeper() {
        ZookeeperOps zookeeperOps = new ZookeeperOpsImpl();
        InetAddress address = NativeUtils.getLocalHostExactAddress();
        // TODO 有bug，找不到真实IP
        String ipStr = "127.0.0.1";
        assert address != null;
        String myAddress = ipStr + ":" + SystemConstant.MY_PORT;
        zookeeperOps.addTmpNode(SystemConstant.IM_NODE_PATH_PREFIX + "/" + myAddress);
    }
}
