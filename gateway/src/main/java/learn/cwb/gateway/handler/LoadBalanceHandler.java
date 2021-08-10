package learn.cwb.gateway.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.gateway.redis.RedisOps;
import learn.cwb.gateway.redis.impl.RedisOpsImpl;
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
            long tmp = msg.getHead().getSenderId();
            int mod = Math.min(GlobalVariable.AVAILABLE_SERVERS.size(), 17);
            while (tmp >= GlobalVariable.AVAILABLE_SERVERS.size()) {
                tmp %= mod;
            }
            String address0 = "";
            // 负载均衡
            for (String address : GlobalVariable.AVAILABLE_SERVERS) {
                if (tmp == 0) {
                    address0 = address;
                    break;
                }
                -- tmp;
            }
            Msg ans = Msg.withText(address0);
            Msg.Head head = ans.getHead();
            head.setType(Msg.Head.Type.ESTABLISH);
            head.setCreatedTime(System.currentTimeMillis());
            head.setSenderId(Msg.Head.SERVER);
            head.setReceiverId(msg.getHead().getSenderId());
            head.setId(new long[] {0, 0});
            ctx.writeAndFlush(ans);
        }
    }
}
