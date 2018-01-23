package com.babel.venus.vo;

import lombok.Data;

/**
 * User: joey
 * Date: 2017/9/6
 * Time: 17:11
 */
@Data
public class OrderProcNoticeVo {

    private Long pCode;  //期数

    private Integer procStatus; //处理状态

    private Long lotteryId; //彩种ID

    private Integer prizeStatus; //开奖状态 PrizeStatus

}
