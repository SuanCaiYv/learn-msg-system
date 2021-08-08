package learn.cwb.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 12:14 上午
 */
public class SSLUtils {
    static final Logger LOGGER = LoggerFactory.getLogger(SSLUtils.class);

    private static final File serverCrtChainFile;

    private static final File serverKeyFile;

    private static final File clientCrtChainFile;

    private static final File clientKeyFile;

    private static final File caFile;

    static {
        URL crtChainResource1 = Thread.currentThread().getContextClassLoader().getResource("ssl/server.crt");
        URL keyResource1 = Thread.currentThread().getContextClassLoader().getResource("ssl/pkcs8_server.key");
        URL crtChainResource2 = Thread.currentThread().getContextClassLoader().getResource("ssl/client.crt");
        URL keyResource2 = Thread.currentThread().getContextClassLoader().getResource("ssl/pkcs8_client.key");
        URL caCrtResource = Thread.currentThread().getContextClassLoader().getResource("ssl/ca.crt");
        assert crtChainResource1 != null;
        serverCrtChainFile = new File(crtChainResource1.getPath());
        assert keyResource1 != null;
        serverKeyFile = new File(keyResource1.getPath());
        assert crtChainResource2 != null;
        clientCrtChainFile = new File(crtChainResource2.getPath());
        assert keyResource2 != null;
        clientKeyFile = new File(keyResource2.getPath());
        assert caCrtResource != null;
        caFile = new File(caCrtResource.getPath());
    }

    public static File serverCrtChainFile() {
        return serverCrtChainFile;
    }

    public static File serverKeyFile() {
        return serverKeyFile;
    }

    public static File clientCrtChainFile() {
        return clientCrtChainFile;
    }

    public static File clientKeyFile() {
        return clientKeyFile;
    }

    public static File caFile() {
        return caFile;
    }
}
