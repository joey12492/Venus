package com.babel.venus.storage.redis;

import com.alibaba.fastjson.JSON;
import com.babel.common.core.util.RedisUtil;
import com.babel.common.redis.RedisConstant;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.constants.RedisConstants;
import com.babel.venus.vo.UserOrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * User: joey
 * Date: 2017/9/4
 * Time: 17:08
 */
//todo 是不是所有的redis第一次轻轻出错后都要重试一次
@Service
public class UserOrderRedisService {

    private static final Logger logger = LoggerFactory.getLogger(UserOrderRedisService.class);

//    private JedisCluster jedis;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 批量获取缓存中未开奖的订单
     */
    public List<String> batchPop(final String key) {
        try {
            List<String> orderIds = new ArrayList<>();
            ListOperations<String, String> operations = redisTemplate.opsForList();
            for (int i = 0; i < RedisConstants.BATCH_POP_NUM; i++) {
                String orderId = operations.rightPop(key);
                if (orderId != null) {
                    orderIds.add(orderId);
                } else {
                    //如果没拿取到，重试一次
                    orderId = operations.rightPop(key);
                    if (orderId != null) {
                        orderIds.add(orderId);
                    } else {
                        break;
                    }
                }
            }
            logger.info("--> user order redis batchPop , key : {}, \n orderIds : {} " , key, JSON.toJSONString(orderIds));
            return orderIds;
        } catch (Exception e) {
            logger.error("--> user order redis batchPop error, key : " + key + ", vo : " + JSON.toJSONString(key), e);
        }
        return null;
    }

    /**
     * 批量获取缓存中订单
     */
    public List<UserOrderPO> batchGetCacheOrders(Long lotteryId, Long pdate, List<String> orderIds) {
        String key = RedisConstant.getKeyUserOrderMap(lotteryId, pdate);
        try {
            List<UserOrderPO> userOrders = redisTemplate.opsForHash().multiGet(key, orderIds);
            logger.info("--> user order redis batchGetCacheOrders, key : {}, order.size :{} ", key, userOrders.size());
            return userOrders;
        } catch (Exception e) {
            logger.error("--> user order redis batchGetCacheOrders error , key : " + key, e);
        }
        return null;
    }

    /**
     * 获取当期所有有数据的玩法key
     * 在forseti中拿取
     */
    public Set<String> getCurrentPCodePlayIdKeys(UserOrderVo vo) {
        String key = RedisConstant.getKeyUserOrderPlayMap(vo.getLotteryId(), vo.getPcode());
        try {
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            Set<String> result = setOperations.members(key);
            logger.info("--> user order redis getCurrentPCodePlayIds, key : {}, playIds:{}", key, JSON.toJSONString(result));
            return result;
        } catch (Exception e) {
            logger.error("--> user order redis getCurrentPCodePlayIds error , key : " + key, e);
        }
        return null;
    }

    /**
     * 修改缓存中的订单
     */
    public boolean updateOrders(List<UserOrderPO> pos) {
        try {
            for (UserOrderPO po : pos) {
                String key = RedisConstant.getKeyUserOrderMap(po.getLotteryId(), po.getPdate());
                redisTemplate.opsForHash().put(key, po.getOrderId(), po);
            }
            logger.info("--> user order redis update: {}, order.size :{} ", pos.size());
            return true;
        } catch (Exception e) {
            logger.error("--> user order redis batchGetCacheOrders error ", e);
        }
        return false;
    }

    /**
     * 将处理过的订单id批量存入redis中
     *
     * @param orderIds
     * @return
     */
    public boolean batchSaveProcOrderCache(Long pcode, Long lotteryId, List<String> orderIds) {
        String key = RedisConstants.getProccessedOrderIdes(pcode, lotteryId);
        try {
            ListOperations<String, String> operations = redisTemplate.opsForList();
            Long result = operations.leftPushAll(key, orderIds);
            redisTemplate.expire(key, RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("--> user order redis batchSaveProcOrderCache error, pcode:{}, lotteryId:{}, orderIds:{}", pcode, lotteryId, JSON.toJSONString(orderIds), e);
        }
        return false;
    }

    /*------------------------------------*/

    /**
     * 测试使用
     */

    /**
     * 设置玩法id列表
     *
     * @param playId
     * @param pcode
     * @return
     */
    public boolean setCurrentPCodePlayIds(long playId, long pcode) {
        try {
            String key = RedisConstants.getCurrentBetIds(pcode);
            SetOperations<String, Long> operations = redisTemplate.opsForSet();
            if (operations.isMember(key, pcode)) {
                return true;
            }
            Long result = operations.add(key, playId);
            redisTemplate.expire(key, RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
            return result != null && result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 添加订单到redis
     *
     * @param order
     * @return
     */
    public boolean setUserOrder(UserOrderPO order) {
        try {
            String key = RedisConstants.getCurrentOrders(order.getPcode(), order.getPlayId());
            ListOperations<String, UserOrderPO> operations = redisTemplate.opsForList();
            Long result = operations.leftPush(key, order);
            redisTemplate.expire(key, RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
            return result != null && result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取某玩法下的订单100条
     *
     * @param key
     * @return
     */
    public List<UserOrderPO> getOrderList(String key) {
        try {
            ListOperations<String, UserOrderPO> operations = redisTemplate.opsForList();
            List<UserOrderPO> orders = operations.range(key, 0, 99);
            return orders;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
