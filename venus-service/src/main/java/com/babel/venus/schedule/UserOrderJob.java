package com.babel.venus.schedule;

import com.babel.common.core.util.DateUtils;
import com.babel.common.lottery.OrderStatus;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.assemble.UserOrderAssemService;
import com.babel.venus.constants.RedisConstants;
import com.babel.venus.constants.VenusConstants;
import com.babel.venus.enums.WhetherType;
import com.babel.venus.service.UserOrderService;
import com.babel.venus.util.VenusUtil;
import com.babel.venus.vo.BaseQueryVo;
import com.babel.venus.vo.UserOrderVo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: joey
 * Date: 2017/9/18
 * Time: 17:02
 * 订单的定时处理任务，修复缓存中未处理的订单
 */
@Component
public class UserOrderJob {

    @Resource
    private UserOrderAssemService userOrderAssemService;
    @Resource
    private UserOrderService userOrderService;
    @Resource
    private VenusUtil venusUtil;


    /**
     * 执行未处理的缓存中的数据
     */
    @Scheduled(fixedRate = VenusConstants.three_minutes)
    public void cacheRepairJob() {
        //todo 从缓存中检查最近一期未pop的开奖数据，重新执行开奖流程
//        userOrderAssemService.drawLottery();
    }

    /**
     * 处理mongo中未执行开奖流程的数据,一般是前一个小时
     * 多台机器执行需要控制好页码，如果是单台机器执行需要控制锁
     */
//    @Scheduled(fixedRate = VenusConstants.three_minutes)
    //需要时在启用
//    @Deprecated
//    public void mongoNoDrawJob() {
//        try {
//            Date date = new Date();
//            String hhDate = DateUtils.format(date, "yyyyMMddHH");
//            Date endDate = null;
//            try {
//                endDate = DateUtils.parse(hhDate, "yyyyMMddHH");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Calendar startDate = Calendar.getInstance();
//            startDate.setTime(endDate);
//            startDate.add(Calendar.HOUR, -1);
//            BaseQueryVo vo = new BaseQueryVo(startDate.getTimeInMillis(), endDate.getTime(), DateUtils.getPdate(startDate.getTime()));
//            Pageable pageable = new PageRequest(0, RedisConstants.BATCH_POP_NUM);
//            List<String> orderIds = userOrderService.queryNoDrawOrderIds(vo, pageable);
//            while (orderIds != null && orderIds.size() > 0) {
//                vo.setOrderIds(orderIds);
//                List<UserOrderPO> userOrders = userOrderService.queryMongoProcOrders(vo);
//                //todo 执行开奖流程,需要根据期号获取到开奖的号码
//                String winNum = "";
//                userOrders.forEach((order -> {
//                    UserOrderVo orderVo = new UserOrderVo(order.getPcode());
//                    orderVo.setOrderStatus(OrderStatus.bet_success.code());
//                    orderVo.setIfChase(WhetherType.yes.code());
//                    Map<String, UserOrderPO> chaseOrders = userOrderService.findCurrentChase(orderVo);
//                    userOrderAssemService.flushOneData(order, chaseOrders, winNum);
//                }));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }





}
