package learn.cwb.common.util;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 12:13 上午
 */
public class NativeUtils {
    static final Logger LOGGER = LoggerFactory.getLogger(NativeUtils.class);

    private static final String osName = System.getProperty("os.name").replaceAll(" ", "").toLowerCase();

    private static final int cpuNums = NettyRuntime.availableProcessors();

    public static EventLoopGroup bossEventLoopGroup() {
        if (osName.contains("macos") || osName.contains("osx")) {
            return new KQueueEventLoopGroup(cpuNums >= 16 ? cpuNums >> 2 : 1);
        } else if (osName.contains("linux")) {
            return new EpollEventLoopGroup(cpuNums >= 16 ? cpuNums >> 2 : 1);
        } else {
            return new NioEventLoopGroup(cpuNums >= 16 ? cpuNums >> 2 : 1);
        }
    }

    public static EventLoopGroup workerEventLoopGroup() {
        if (osName.contains("macos") || osName.contains("osx")) {
            return new KQueueEventLoopGroup(cpuNums >= 32 ? cpuNums << 2 : cpuNums);
        } else if (osName.contains("linux")) {
            return new EpollEventLoopGroup(cpuNums >= 32 ? cpuNums << 2 : cpuNums);
        } else {
            return new NioEventLoopGroup(cpuNums >= 32 ? cpuNums << 2 : cpuNums);
        }
    }

    public static Class<? extends ServerChannel> serverChannel() {
        if (osName.contains("macos") || osName.contains("osx")) {
            return KQueueServerSocketChannel.class;
        } else if (osName.contains("linux")) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    public static Class<? extends Channel> clientChannel() {
        if (osName.contains("macos")) {
            return KQueueSocketChannel.class;
        } else if (osName.contains("linux")) {
            return EpollSocketChannel.class;
        } else {
            return NioSocketChannel.class;
        }
    }

    public static String myIP() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
