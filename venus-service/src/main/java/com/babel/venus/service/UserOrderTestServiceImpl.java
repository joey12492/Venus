package com.babel.venus.service;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.storage.redis.UserOrderRedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserOrderTestServiceImpl {

    @Resource
    private UserOrderRedisService userOrderRedisService;

    public void setCurrentPCodePlayIds(long playId, long pcode) {
        userOrderRedisService.setCurrentPCodePlayIds(playId, pcode);
    }


    public void setUserOrder(UserOrderPO userOrder) {
        userOrderRedisService.setUserOrder(userOrder);
    }

    /**
     *
     * @return
     */
    public List<UserOrderPO> getOrders(String key) {
        return userOrderRedisService.getOrderList(key);
    }
}
