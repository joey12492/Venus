package com.babel.venus.storage.redis;

import com.alibaba.fastjson.JSON;
import com.babel.venus.constants.RedisConstants;
import com.babel.venus.constants.VenusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: joey
 * Date: 2017/10/2
 * Time: 18:29
 */
@Service
public class CommonRedisService {

    private static final Logger logger = LoggerFactory.getLogger(UserOrderRedisService.class);

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 获取处理追单数据的锁
     * @return
     */
    public long getChaseDrawWinLock(long pcode, long lotteryId) {
        try {
            String key = RedisConstants.getChaseDrawWinLock(pcode, lotteryId);
            long result = redisTemplate.boundValueOps(key).increment(1);
            redisTemplate.expire(key, VenusConstants.ONE_DAY_SEC, TimeUnit.SECONDS);
            logger.info("--> get chase proc lock, result :{}",result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2L;
    }

}
