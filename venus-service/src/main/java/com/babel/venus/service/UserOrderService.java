package com.babel.venus.service;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.vo.BaseQueryVo;
import com.babel.venus.vo.UserOrderVo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: joey
 * Date: 2017/9/4
 * Time: 16:43
 */
public interface UserOrderService {

    /**
     * 获取当期所有有数据的玩法key
     * @param vo
     * @return
     */
    Set<String> getCurrentPCodePlayIdKeys(UserOrderVo vo);

    /**
     * 批量获取缓存中未开奖的订单Id
     *
     * @return
     */
    List<String> batchPop(String key);

    /**
     * 批量拿取redis中的订单
     * @param orderIds
     * @return
     */
    List<UserOrderPO> batchGetCacheOrders(Long lotteryId, Long pdate, List<String> orderIds);

    /**
     * 获取当期无效的追单数据
     * @param vo
     * @return
     */
    Map<String, UserOrderPO> findCurrentChase(UserOrderVo vo);

    /**
     * 获取当期追单并且在开奖中的数据,在mongo中获取
     * @return
     */
    List<UserOrderPO> findCurrentPCodeOrders(UserOrderVo vo);

    /**
     * 保存订单
     * @param order
     * @return
     */
    boolean save(UserOrderPO order);

    /**
     * 保存执行完开奖流程的订单
     * @param orders
     * @return
     */
    boolean batchSaveProcOrder(List<UserOrderPO> orders);

    /**
     * 将处理过的订单id批量存入redis中
     * @param orderIds
     * @return
     */
    boolean batchSaveProcOrderCache(Long pCode,Long lotteryId, List<String> orderIds);

    /**
     * 覆盖缓存中的订单信息
     * @param orderPOs
     * @return
     */
    boolean updateCacheOrders(List<UserOrderPO> orderPOs);
    /**
     * 批量入库
     * @param orderPOs
     * @return
     */
    boolean batchSaveDb(List<UserOrderPO> orderPOs);


    /**
     * 查询未开奖的订单id列表
     * @param vo
     * @return
     */
    List<String> queryNoDrawOrderIds(BaseQueryVo vo, Pageable pageable);


    /**
     * 获取mongo中处理后的订单列表
     * @param vo
     * @return
     */
    List<UserOrderPO> queryMongoProcOrders(BaseQueryVo vo);

    /**
     * 获取mongo中的订单列表
     * @param vo
     * @return
     */
    List<UserOrderPO> queryMongoOrders(BaseQueryVo vo);

    /**
     * 获取mongo中中奖停追的订单
     * @param vo
     * @return
     */
    List<UserOrderPO> queryMongoChaseWinStopOrders(BaseQueryVo vo);

    /**
     * 修改mongo中中奖停追的订单状态
     * @param vo
     * @return
     */
    boolean updateChaseWinStopOrders(BaseQueryVo vo);

    /**
     * 修改mongo中订单
     * @param po
     * @return
     */
    boolean updateMongoOrder(UserOrderPO po);

}
