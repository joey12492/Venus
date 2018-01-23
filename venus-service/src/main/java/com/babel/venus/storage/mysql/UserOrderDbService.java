package com.babel.venus.storage.mysql;

import com.babel.common.lottery.OrderStatus;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.dao.db.UserOrderMapper;
import com.babel.venus.enums.WhetherType;
import com.babel.venus.vo.UserOrderVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * User: joey
 * Date: 2017/9/4
 * Time: 20:36
 */
//todo 暂时去掉分库分表功能
@Service
public class UserOrderDbService {

    @Resource
    private UserOrderMapper userOrderMapper;

    /**
     * 获取当期无效的追单数据
     * @param vo
     * @return
     */
    public List<UserOrderPO> findOrders(UserOrderVo vo) {
        return userOrderMapper.findOrders(vo, null);
    }

    public boolean batchInsert(List<UserOrderPO> orderPOs) {
        return userOrderMapper.batchInsert(orderPOs, null) > 0;
//        return userOrderMapper.batchInsert(orderPOs, String.valueOf(orderPOs.get(0).getPcode()).substring(0, 8)) > 0;
    }

}
