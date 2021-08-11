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
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static EventLoopGroup singleEventLoopGroup() {
        if (osName.contains("macos") || osName.contains("osx")) {
            return new KQueueEventLoopGroup(1);
        } else if (osName.contains("linux")) {
            return new EpollEventLoopGroup(1);
        } else {
            return new NioEventLoopGroup(1);
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

    public static EventExecutorGroup defaultEventExecutorGroup() {
        return new DefaultEventExecutorGroup(cpuNums, new ThreadFactory() {
            final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("Executor-" + index.getAndIncrement() + "-Only for I/O blocking operations");
                return thread;
            }
        });
    }

    public static ExecutorService defaultExecutorService() {
        final Logger LOGGER = LoggerFactory.getLogger("inner class from defaultExecutorService");
        return new ThreadPoolExecutor(
                Math.max(1, cpuNums >> 1),
                cpuNums, 12,
                TimeUnit.HOURS,
                new LinkedBlockingDeque<>(1 << 30),
                new ThreadFactory() {
                    final AtomicInteger index = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName("ThreadPool-" + index.getAndIncrement() + "-Only for I/O blocking operations");
                        return thread;
                    }
                },
                (r, executor) -> LOGGER.error("There are to much task to run! This shouldn't happen as design, please consider to expand your app's scale.")
        );
    }

    public static String myIP() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String address = "127.0.0.1";
            // return localHost.getHostAddress();
            // TODO 小BUG
            return address;
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 网上扒的获取本机真实IP的方法
     */
    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddress;
                        }
                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddress;
                        }
                    }
                }
            }
            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getLocalHostExactAddress().getHostAddress());
    }
}
