package com.babel.venus.test;

import com.alibaba.fastjson.JSON;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.enums.WhetherType;
import com.babel.venus.service.UserOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    @Test
    public void addOrder() {
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
        order.setPcode(20170911001L);
        order.setBetTime(time);
        order.setReforwardPoint(50);
        order.setBetMode(10000L);
        order.setBetCount(10);
        order.setBetAmount(2000L);
        order.setBetContent("235,368,46,1234,567");
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
        System.out.println(jo.getBytes().length);

    }
}
