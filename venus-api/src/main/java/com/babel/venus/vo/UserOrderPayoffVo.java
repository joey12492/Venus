package com.babel.venus.vo;

import lombok.Data;

/**
 * User: joey
 * Date: 2017/11/14
 * Time: 14:47
 */
@Data
public class UserOrderPayoffVo {

    /**
     * 方案号
     */
    private String orderId;
    /**
     * 下注倍数
     */
    private Integer multiple;
    /**
     * 钱类型，元角分模式(y元/j角/f分)
     */
    private String moneyMode;

    private int firstPrizeNum;
    private int secondPrizeNum;
    private int thirdPrizeNum;
    private int forthPrizeNum;
    private int fifthPrizeNum;
    private int isTied;
    private int isZodiacYear;

}
