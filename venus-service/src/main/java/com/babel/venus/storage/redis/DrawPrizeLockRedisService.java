package com.babel.venus.storage.redis;

import com.babel.venus.config.VenusRefreshConfig;
import com.babel.venus.constants.RedisConstants;
import com.babel.venus.service.feign.VenusOutServerConsumer;
import com.babel.venus.util.ThreadTaskPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: joey
 * Date: 2017/11/15
 * Time: 18:42
 * 保证所有的机器都执行完开奖流程，才通知hermes，用锁来控制
 * 1.调用内部开奖的接口时增加锁
 * 2.每个venus开奖完成时检查锁，根据数量匹配，如果数量不匹配，5秒试一次，20秒后还没有，调用hermes
 */
@Component
public class DrawPrizeLockRedisService {

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeLockRedisService.class);

    public static Map<String, Runnable> checkReqHermesLockMap = new ConcurrentHashMap<>();

    private ExecutorService HTTP_THREAD_POOL = ThreadTaskPoolFactory.httpCoreThreadTaskPool;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private VenusRefreshConfig venusRefreshConfig;
    @Resource
    private VenusOutServerConsumer venusOutServerConsumer;

    /**
     * 设置调用内部开奖的接口时增加锁
     */
    public long setVenusDrawLock(Long lotteryId, Long pcode) {
        try {
            String key = RedisConstants.getVenusDrawLockKey(lotteryId, pcode);
            long result = redisTemplate.boundValueOps(key).increment(1);
            redisTemplate.expire(key, RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
            logger.info("--> set draw prize lock , lotteryId: {}, pcode: {}, result: {}", lotteryId, pcode, result);
            return result;
        } catch (Exception e) {
            logger.error("--> set draw prize lock error, lotteryId:{}, pcode:{}", lotteryId, pcode, e);
        }
        return -1L;
    }

    /**
     * 获取调用内部开奖的接口时增加锁
     */
    public long getVenusDrawLock(Long lotteryId, Long pcode) {
        try {
            String key = RedisConstants.getVenusDrawLockKey(lotteryId, pcode);
            Long result = (Long) redisTemplate.execute(new RedisCallback() {
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] _key = key.getBytes();
                    byte[] value = connection.get(_key);
                    if (value != null) {
                        return new Long(new String(value));
                    }
                    return null;
                }
            });
            logger.info("--> get draw prize lock , lotteryId: {}, pcode: {}, result: {}", lotteryId, pcode, result);
            return result == null ? 0 : result;
        } catch (Exception e) {
            logger.error("--> get draw prize lock error, lotteryId:{}, pcode:{}", lotteryId, pcode, e);
        }
        return -1L;
    }

    /**
     * 开奖完成后调用hermes的数量
     * 与开始执行开奖时生成的锁的数量做匹配，启动新线程监听完成开奖的数量
     */
    public long setDrawSucReqHermesLock(Long lotteryId, Long pcode) {
        try {
            String key = RedisConstants.getDrawSucReqHermesLockKey(lotteryId, pcode);
            long result = redisTemplate.boundValueOps(key).increment(1);
            redisTemplate.expire(key, RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
            logger.info("--> set draw suc req hermes lock , lotteryId: {}, pcode: {}, result: {}", lotteryId, pcode, result);
            return result;
        } catch (Exception e) {
            logger.error("--> set draw suc req hermes lock error, lotteryId:{}, pcode:{}", lotteryId, pcode, e);
        }
        return -1L;
    }

    /**
     * 开奖完成后调用hermes的数量
     * 与开始执行开奖时生成的锁的数量做匹配，启动新线程监听完成开奖的数量
     */
    public long getDrawSucReqHermesLock(Long lotteryId, Long pcode) {
        try {
            String key = RedisConstants.getDrawSucReqHermesLockKey(lotteryId, pcode);
            Long result = (Long) redisTemplate.execute(new RedisCallback() {
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] _key = key.getBytes();
                    byte[] value = connection.get(_key);
                    if (value != null) {
                        return new Long(new String(value));
                    }
                    return null;
                }
            });
            logger.info("--> get draw suc req hermes lock , lotteryId: {}, pcode: {}, result: {}", lotteryId, pcode, result);
            return result == null ? 0 : result;
        } catch (Exception e) {
            logger.error("--> get draw suc req hermes lock error, lotteryId:{}, pcode:{}", lotteryId, pcode, e);
        }
        return -1L;
    }

    public CheckReqHermesLock getCheckReqHermesLock(Long lotteryId, Long pcode, int procStatus, int prizeStatus, boolean repairEnter) {
        return new CheckReqHermesLock(lotteryId, pcode, procStatus, prizeStatus, repairEnter);
    }

    /**
     * 当venus执行完开奖流程后，开启此线程，循环拿取执行完的开奖数量，
     */
    public class CheckReqHermesLock implements Runnable {
        private Long lotteryId;
        private Long pcode;
        private int procStatus;
        private int prizeStatus;
        private boolean repairEnter;

        public CheckReqHermesLock(Long lotteryId, Long pcode, int procStatus, int prizeStatus, boolean repairEnter) {
            this.lotteryId = lotteryId;
            this.pcode = pcode;
            this.prizeStatus = prizeStatus;
            this.procStatus = procStatus;
            this.repairEnter= repairEnter;
        }


        @Override
        public void run() {
            boolean flag = false;
            for (int i = 0; i < 6; i++) {
                if (getVenusDrawLock(lotteryId, pcode) <= getDrawSucReqHermesLock(lotteryId, pcode)) {
                    flag = true;
                    break;
                } else {
                    if (i >= 4) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (flag) {
                logger.info("--> all venus draw prize completed, lotteryId:{}, pcode:{}", lotteryId, pcode);
            } else {
                logger.warn("--> some venus draw prize completed, lotteryId:{}, pcode:{}", lotteryId, pcode);
            }
            String[] reqDrawHostSplit = venusRefreshConfig.getHermesReqDrawHosts().split(",");
            for (String host : reqDrawHostSplit) {
                HTTP_THREAD_POOL.execute(new Runnable() {
                    @Override
                    public void run() {
                        String result = venusOutServerConsumer.hermesDrawPrizeSucHandler(host, pcode, lotteryId, procStatus, prizeStatus, repairEnter);
                        if (result == null) {
                            logger.error("-->  send hermes draw suc notice failed,result is null, host:{}, pcode:{}, lotteryId:{}, repairEnter:{}", host, pcode, lotteryId, repairEnter);
                        } else {
                            logger.info("--> send hermes draw suc notice completed, host:{}, pcode:{}, lotteryId:{} ,result :{}, repairEnter:{}", host, pcode, lotteryId, result, repairEnter);
                        }
                    }
                });
            }
            checkReqHermesLockMap.remove(pcode + "_" + lotteryId);
        }
    }
}
