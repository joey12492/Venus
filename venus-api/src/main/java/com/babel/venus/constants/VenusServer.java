package com.babel.venus.constants;

/**
 * User: joey
 * Date: 2017/9/22
 * Time: 11:59
 */
public class VenusServer {

    public static final String SERVER_NAME="venus";
    /**
     * 开奖总入口
     */
    public static final String DRAW_PRIZE_BUS = "/apis/lottery/draw/prize";

    /**
     * 真正的开奖接口
     */
    public static final String DRAW_PRIZE_INTERNAL = "/apis/lottery/internal/draw/prize";

    /**
     * 手动开奖
     */
    public static final String MANUAL_DRAW_PRIZE = "/apis/lottery/manual/draw/prize";
    /**
     * 号码验证失败，确认后继续开奖
     */
    public static final String REPAIR_DRAW_PRIZE = "/apis/lottery/repair/draw/prize";
    /**
     * 单独派奖
     */
    public static final String SINGLE_ORDER_PAYOFF = "/apis/lottery/single/order/payoff";
}
