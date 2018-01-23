package com.babel.venus.service;

import com.alibaba.fastjson.JSON;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.enums.WhetherType;
import com.babel.venus.storage.redis.UserOrderRedisService;
import com.babel.venus.vo.UserOrderVo;
import com.bc.lottery.draw.service.impl.ShishicaiDrawServiceImpl;
import com.bc.lottery.entity.LotteryType;
import com.bc.lottery.entity.ShishicaiType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: joey
 * Date: 2017/9/11
 * Time: 19:47
 * 注单测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:springSource/spring-applications.xml"})
public class LotPourTest {

    @Resource
    private UserOrderService userOrderService;

    @Resource
    private UserOrderTestServiceImpl userOrderTestService;

    @Resource
    private UserOrderRedisService userOrderRedisService;


    @Test
    public void addOrder() {
//        LotteryOrderTest lotteryOrderTest = new LotteryOrderTest();
//        for (int k = 0; k<300;k++) {
//            long start = System.currentTimeMillis();
//            LotteryType betType = ShishicaiType.WU_XING_ZHI_XUAN_FU_SHI;
//            List<List<String>> betList = lotteryOrderTest.getBetNumbersByType(betType);
//            System.out.println("getBetNum spend time : " + (System.currentTimeMillis() - start));
//            long playId = 1001;
//            long pcode = 20170918003L;
//            String betContent = "";
//            for (List<String> list : betList ) {
//                String shortBetContent = "";
//                for (String betNum : list) {
//                    shortBetContent += betNum;
//                }
//                betContent += shortBetContent + ",";
//            }
//            assemOrder(playId, pcode, betContent);
//            System.out.println("spend time : " + (System.currentTimeMillis() - start));
//        }
    }


    private void assemOrder(Long playId, Long pcode, String betContent) {
        //五星直选复式
        long time = System.currentTimeMillis();
        UserOrderPO order = new UserOrderPO();

        order.setPlatInfoId(10001L);

        order.setOwnerInfo("10001");
        order.setParentOrderId(time + "");
        order.setOrderId(time + "");
        order.setMemberId(105055L);
        order.setLotteryId(1L);
        order.setPlayId(111L);
        order.setPcode(pcode);
        order.setBetTime(time);
        order.setReforwardPoint(50);
        order.setBetMode(10000L);
        order.setBetCount(10);
        order.setBetAmount(2000L);
        order.setBetContent(betContent);
        order.setMultiple(2);
        order.setMoneyMode("1");
        order.setIfChase(WhetherType.no.code());
        order.setChaseCount(-1);
        order.setChaseWinStop(2);
        order.setWinPayRate(5);
        order.setOrderStatus(1);
        order.setSource(1);
        order.setRemark("本地测试");
        order.setCreateTime(time);
        order.setCreateUser(1000L);
        order.setModifyTime(time);
        order.setModifyUser(1000L);
        order.setCancelFee(0L);
        order.setWinNumber("53415");
        order.setPayoff(0L);

        String[] betNums = order.getBetContent().split(",");
        List<List<String>> lists = new ArrayList<>();
        for (String betNum : betNums) {
            List<String> simNum = new ArrayList<>();
            for (int j = 0; j < betNum.length(); j++) {
                simNum.add(betNum.substring(j, j + 1));
            }
            lists.add(simNum);
        }
        order.setBetContentProc(lists);
        String jo = JSON.toJSONString(order);
        System.out.println(jo);

//        userOrderTestService.setCurrentPCodePlayIds(order.getPlayId(), order.getPcode());
//        userOrderTestService.setUserOrder(order);
    }

    /**
     * 获取某个玩法下的100条订单数据
     */
    @Test
    public void getOrders() {
        UserOrderVo vo = new UserOrderVo();
        vo.setPcode(20170911002L);
        Set<String> playIds = userOrderRedisService.getCurrentPCodePlayIdKeys(vo);
        playIds.forEach((playid) -> {
            System.out.println("playId ----" + playid);
            List<UserOrderPO> orders = userOrderTestService.getOrders(playid);
            System.out.printf(JSON.toJSONString(orders));

        });
    }


    /**
     * 获取某一期所有有注单的玩法
     */
    @Test
    public void getPlayIds() {
        UserOrderVo vo = new UserOrderVo(20170919077L, 1L);
        Set<String> playIds = userOrderRedisService.getCurrentPCodePlayIdKeys(vo);
        System.out.println(JSON.toJSON(playIds));
    }

    /**
     * U
     * 测试开奖功能
     */
    @Test
    public void drawOrder() {

        String winNum = "53415";

        long lotteryId = 1L;

        UserOrderVo vo = new UserOrderVo(20170919077L, lotteryId);
        Set<String> playIds = userOrderRedisService.getCurrentPCodePlayIdKeys(vo);
        playIds.forEach((playid) -> {
            System.out.println("playId ----" + playid);
            List<UserOrderPO> orders = userOrderTestService.getOrders(playid);
            if (orders != null && orders.size() > 0) {
                ShishicaiDrawServiceImpl shishicaiService = new ShishicaiDrawServiceImpl();
                LotteryType lotteryType = LotteryType.parseType(lotteryId, orders.get(0).getPlayId());
                orders = shishicaiService.getBatchBoundsInfoOfLottery(lotteryType, winNum, orders);
                orders.forEach((boundsInfo -> {
                    System.out.println(JSON.toJSONString(boundsInfo));
                }));

            }
        });


    }

    @Test
    public void pdateTest() {
        System.out.println(String.valueOf(20170921022L).substring(0, 8));
    }
}
