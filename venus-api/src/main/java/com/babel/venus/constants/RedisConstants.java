package com.babel.venus.constants;

/**
 * RedisConstants
 *
 * @author zjh
 * @date 2017/5/10
 */
public class RedisConstants {

    /*24 小时*/
    public static final int EXPIRE_ONE_DAY = 60 * 60 * 24;
    /* 3 小时 */
    public static final int EXPIRE_THREE_HOUR = 10800;
    /* 1 小时 */
    public static final int EXPIRE_ONE_HOUR = 3600;

    /**
     * 批量pop redis中的数据条数
     */
    public static final int BATCH_POP_NUM = 300;
    /**
     * 单台机器批量pop redis中的数据次数
     */
    public static final int BATCH_POP_PROC_TIME = 100;

    /**
     * 获取当期有订单的玩法列表
     */
    private static final String current_bet_ids = "v_c_lot_ids";

    public static String getCurrentBetIds(long pcode) {
        return current_bet_ids + "_" + pcode;
    }

    /**
     * 当期未处理的订单列表
     */
    private static final String current_orders = "v_c_orders";

    public static String getCurrentOrders(long pcode, long playId) {
        return current_orders + "_" + pcode + "_" + playId;
    }

    /**
     * 追单数据处理的锁
     */
    private static final String chase_draw_win_lock = "v_c_chase_lock";

    public static String getChaseDrawWinLock(long pcode, long lotteryId) {
        return chase_draw_win_lock + "_" + pcode + "_" + lotteryId;
    }

    /**
     * 再次开奖的处理过后的定单id
     */
    private static final String processed_order_ids = "v_c_o_proc_ids_";

    public static String getProccessedOrderIdes(long pcode, long lotteryId) {
        return processed_order_ids + pcode + "_" + lotteryId;
    }

    /**
     * venus开奖调用内部开奖时设置锁
     */
    private static final String venus_draw_lock = "v_draw_prize_lock_";

    public static String getVenusDrawLockKey(Long lotteryId, Long pcode) {
        return venus_draw_lock + pcode + "_" + lotteryId;
    }

    /**
     * venus开奖完成后调用hermes的数量
     */
    private static final String venus_draw_suc_req_hermes_lock = "v_suc_r_hermes_lock_";

    public static String getDrawSucReqHermesLockKey(Long lotteryId, Long pcode) {
        return venus_draw_suc_req_hermes_lock + pcode + "_" + lotteryId;
    }

    /**
     * 彩种类型
     */
    public static final String LOTTERY_TYPES="v_lottery_types_";

}
