package com.babel.venus.service.impl;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.service.UserOrderService;
import com.babel.venus.storage.mongo.UserOrderMongoService;
import com.babel.venus.storage.mysql.UserOrderDbService;
import com.babel.venus.storage.redis.UserOrderRedisService;
import com.babel.venus.vo.BaseQueryVo;
import com.babel.venus.vo.UserOrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: joey
 * Date: 2017/9/4
 * Time: 16:43
 */
@Service
public class UserOrderServiceImpl implements UserOrderService {

    private static final Logger logger = LoggerFactory.getLogger(UserOrderServiceImpl.class);

    @Resource
    private UserOrderRedisService userOrderRedisService;
    @Resource
    private UserOrderDbService userOrderDbService;
    @Resource
    private UserOrderMongoService userOrderMongoService;


    @Override
    public Set<String> getCurrentPCodePlayIdKeys(UserOrderVo vo) {
        return userOrderRedisService.getCurrentPCodePlayIdKeys(vo);
    }

    @Override
    public List<String> batchPop(String key) {
        return userOrderRedisService.batchPop(key);
    }

    @Override
    public List<UserOrderPO> batchGetCacheOrders(Long lotteryId, Long pdate, List<String> orderIds) {
        return userOrderRedisService.batchGetCacheOrders(lotteryId, pdate, orderIds);
    }

    @Override
    public Map<String, UserOrderPO> findCurrentChase(UserOrderVo vo) {
        List<UserOrderPO> userOrders = userOrderDbService.findOrders(vo);
        if (userOrders != null && userOrders.size() > 0) {
            Map<String, UserOrderPO> map = new HashMap<>();
            userOrders.forEach((uo) -> {
                map.put(uo.getOrderId(), uo);
            });
            return map;
        }
        return null;
    }

    @Override
    public List<UserOrderPO> findCurrentPCodeOrders(UserOrderVo vo) {
        return userOrderDbService.findOrders(vo);
    }

    @Override
    public boolean save(UserOrderPO order) {
        return userOrderMongoService.saveOrder(order);
    }

    @Override
    public boolean batchSaveProcOrder(List<UserOrderPO> orders) {
        return userOrderMongoService.batchSaveProcOrder(orders);
    }

    @Override
    public boolean batchSaveProcOrderCache(Long pCode, Long lotteryId, List<String> orderIds) {
        return userOrderRedisService.batchSaveProcOrderCache(pCode, lotteryId, orderIds);
    }

    @Override
    public boolean updateCacheOrders(List<UserOrderPO> orderPOs) {
        return userOrderRedisService.updateOrders(orderPOs);
    }

    /**
     * 1、订单入库采用分库分表策略，
     * 2、订单要入两份库，客端一份，平台一份，客端采用以用户分库时间分表，平台采用以平台分库时间分表
     * @param orderPOs
     * @return
     */
    @Override
    public boolean batchSaveDb(List<UserOrderPO> orderPOs) {
        return userOrderDbService.batchInsert(orderPOs);
    }


    @Override
    public List<String> queryNoDrawOrderIds(BaseQueryVo vo, Pageable pageable) {
        return userOrderMongoService.queryNoDrawOrderIds(vo, pageable);
    }

    @Override
    public List<UserOrderPO> queryMongoProcOrders(BaseQueryVo vo) {
        return userOrderMongoService.queryProcOrders(vo);
    }

    @Override
    public List<UserOrderPO> queryMongoOrders(BaseQueryVo vo) {
        return userOrderMongoService.queryOrders(vo);
    }

    @Override
    public List<UserOrderPO> queryMongoChaseWinStopOrders(BaseQueryVo vo) {
        return userOrderMongoService.queryMongoChaseWinStopOrders(vo);
    }

    @Override
    public boolean updateChaseWinStopOrders(BaseQueryVo vo) {
        return userOrderMongoService.updateChaseWinStopOrders(vo);
    }

    @Override
    public boolean updateMongoOrder(UserOrderPO po) {
        return userOrderMongoService.updateOrder(po);
    }

}
