package com.babel.venus.util;

import com.babel.common.lottery.utils.order.OrderUtils;
import com.babel.venus.exception.VenusException;
import com.babel.venus.service.feign.VenusOutServerConsumer;
import com.babel.venus.storage.local.LocalCacheManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * User: joey
 * Date: 2018/1/2
 * Time: 14:08
 */
@Component
public class VenusUtil {

    private static final Logger logger = LoggerFactory.getLogger(VenusUtil.class);
    @Resource
    private VenusOutServerConsumer venusOutServerConsumer;

    private Integer getLotteryType(Long lotteryId) {
        Integer lotteryType = LocalCacheManage.lotteryTypeCache.getIfPresent(lotteryId);
        if (lotteryType == null) {
            Map<Long, Integer> longIntegerMap = venusOutServerConsumer.getLotteryTypes();
            if (longIntegerMap.size() == 0) {
                throw VenusException.NO_LOTTERY_TYPES;
            } else {
                LocalCacheManage.lotteryTypeCache.putAll(longIntegerMap);
                lotteryType = longIntegerMap.get(lotteryId);
                if (lotteryType == null) {
                    logger.error("--> no lottery type : lotteryId:{}", lotteryId);
                    throw VenusException.NO_LOTTERY_TYPE;
                }
            }
        }
        return lotteryType;
    }

    public Long getPdate(Long lotteryId, Long pCode) {
        Integer lotteryType= getLotteryType(lotteryId);
        Long pdate;
        //如果是新疆时时彩或者是六合彩，通过接口获取pdate
        if (lotteryId == 13L || lotteryId == 14L || lotteryType == com.babel.common.lottery.LotteryType.SIX.code()) {
            pdate = LocalCacheManage.pdateCache.getIfPresent(lotteryId + "_" + pCode);
            if (pdate == null) {
                pdate = venusOutServerConsumer.getPdateByPCode(lotteryId, pCode);
            }
            if (pdate == null || pdate == 0) {
                throw VenusException.INVALID_PDATE;
            } else {
                LocalCacheManage.pdateCache.put(lotteryId + "_" + pCode, pdate);
            }
        } else {
            pdate = OrderUtils.getPdate(pCode);
        }
        return pdate;
    }
}
