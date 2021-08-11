package learn.cwb.gateway.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        if (!msg.getHead().getType().equals(Msg.Head.Type.ESTABLISH)) {
            ctx.fireChannelRead(msg);
        } else {
            long senderId = msg.getHead().getSenderId();
            String body = new String(msg.getBody().getBody());
            int mod = Math.min(GlobalVariable.AVAILABLE_IM_SERVERS.size(), 17);
            while (senderId >= GlobalVariable.AVAILABLE_IM_SERVERS.size()) {
                senderId %= mod;
            }
            String address0 = "";
            // 负载均衡
            if (body.equals(SystemConstant.IM_ESTABLISH)) {
                for (String address : GlobalVariable.AVAILABLE_IM_SERVERS) {
                    if (senderId == 0) {
                        address0 = address;
                        break;
                    }
                    -- senderId;
                }
            } else if (body.equals(SystemConstant.NS_ESTABLISH)) {
                for (String address : GlobalVariable.AVAILABLE_NS_SERVERS) {
                    if (senderId == 0) {
                        address0 = address;
                        break;
                    }
                    -- senderId;
                }
            } else {
                ctx.writeAndFlush(Msg.withError());
                return ;
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
