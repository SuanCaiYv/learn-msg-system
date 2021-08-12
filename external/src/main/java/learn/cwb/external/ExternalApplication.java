package learn.cwb.external;

import learn.cwb.external.controller.IMController;
import learn.cwb.external.controller.NSController;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/12 12:52 上午
 */
public class ExternalApplication {

    public static void main(String[] args) {
        IMController.getInstance().work();
        NSController.getInstance().work();
    }
}
