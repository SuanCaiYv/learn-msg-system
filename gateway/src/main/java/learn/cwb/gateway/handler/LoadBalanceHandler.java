package learn.cwb.gateway.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.gateway.redis.RedisOps;
import learn.cwb.gateway.redis.impl.RedisOpsImpl;
import learn.cwb.gateway.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 9:10 下午
 */
public class LoadBalanceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceHandler.class);

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        if (!msg.getHead().getType().equals(Msg.Head.Type.ESTABLISH)) {
            ctx.fireChannelRead(msg);
        } else {
            long senderId = msg.getHead().getSenderId();
            long tmp = senderId;
            int mod = Math.min(GlobalVariable.SERVERS.size(), 17);
            while (tmp >= GlobalVariable.SERVERS.size()) {
                tmp %= mod;
            }
            System.out.println(tmp);
            System.out.println(GlobalVariable.SERVERS.size());
            String address0 = "";
            // 负载均衡
            for (String address : GlobalVariable.SERVERS.keySet()) {
                if (tmp == 0) {
                    REDIS_OPS.setObj(SystemConstant.USER_IN_CLUSTER_PREFIX + senderId, address);
                    address0 = address;
                    break;
                }
                -- tmp;
            }
            Msg ans = Msg.withText(address0);
            Msg.Head head = ans.getHead();
            head.setType(Msg.Head.Type.ESTABLISH);
            head.setCreatedTime(System.currentTimeMillis());
            head.setSenderId(0);
            head.setReceiverId(msg.getHead().getSenderId());
            head.setId(new long[] {0, 0});
            ctx.writeAndFlush(ans);
        }
    }
}
