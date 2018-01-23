package com.babel.venus.vo;

import lombok.Data;

import java.util.List;

/**
 * User: joey
 * Date: 2017/9/5
 * Time: 1:20
 */
@Data
public class UserOrderVo {

    /**
     * 方案号
     */
    private String orderId;
    /**
     * 执行状态
     */
    private Integer procState;
    /**
     * 期号
     */
    private Long pcode;
    /**
     * 统计日期
     */
    private Integer pdate;
    /**
     * 是否追号
     */
    private Integer ifChase;
    /**
     * 注单状态 :
     *  1,等待开奖
        31,未中奖
        32,已派彩
        4,用户撤单
        5,系统撤单
        6,中奖停追
        71,存在异常
        81,异常处理中
     */
    private Integer orderStatus;

    /**
     * 玩法id
     */
    private Long playId;
    /**
     * 彩种id
     */
    private Long lotteryId;

    private List<String> orderIds;

    public UserOrderVo() {
    }

    public UserOrderVo(Long pcode) {
        this.pcode = pcode;
    }

    public UserOrderVo(Long pcode, Long lotteryId) {
        this.pcode = pcode;
        this.lotteryId = lotteryId;
    }

    public UserOrderVo(Long pcode, Integer ifChase, Integer orderStatus) {
        this.pcode = pcode;
        this.ifChase = ifChase;
        this.orderStatus = orderStatus;
    }
}
