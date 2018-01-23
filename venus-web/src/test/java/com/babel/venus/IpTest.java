package com.babel.venus;

import com.alibaba.fastjson.JSON;
import com.babel.common.lottery.MoneyMode;
import com.babel.common.lottery.OrderStatus;
import com.babel.forseti_order.model.UserOrderPO;
import com.bc.lottery.draw.service.LotteryDrawHandle;
import com.bc.lottery.draw.service.impl.LotteryDrawServiceImpl;
import com.bc.lottery.entity.LotteryType;
import com.bc.lottery.pour.service.LotteryPourHandle;
import com.bc.lottery.pour.service.impl.ShishicaiPourServiceImpl;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * User: joey
 * Date: 2017/9/18
 * Time: 17:30
 */
public class IpTest {
    @Test
    public void getLocalIp() {
        Enumeration allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            System.out.println(netInterface.getName());
            Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address) {
                    System.out.println("本机的IP = " + ip.getHostAddress());
                }
            }
        }
    }

    public static void main(String[] args) {

        List<UserOrderPO> list = new ArrayList<>();
        LotteryPourHandle lotteryPourHandle = new ShishicaiPourServiceImpl();
        String orderId = "22362018011110855g5t1v4r";
        long lotteryId = 102;
        long playId = 21603;
        String betContent = "总和单";
        int multiple = 200;
        String winNumber = "0,0,4,0,1";
        LotteryType lotteryType = LotteryType.parseType(lotteryId, playId);
        System.out.println(lotteryType);


        UserOrderPO po = new UserOrderPO();
        po.setPlayId(playId);
        po.setWinNumber(winNumber);
        po.setBetContent(betContent);
        po.setLotteryId(lotteryId);
        po.setMultiple(multiple);
        po.setMoneyMode(String.valueOf(MoneyMode.YUAN.code()));
        po.setOrderId(orderId);
        po.setOrderStatus(OrderStatus.bet_success.code());
        po.setBetContentProc(lotteryPourHandle.getLotteryListByType(lotteryId, playId, po.getBetContent()));
//        po.setIsZodiacYear(0);
        list.add(po);

        //etl获取中奖结果
        LotteryDrawHandle lotteryDrawHandle = new LotteryDrawServiceImpl();
        System.out.println(JSON.toJSONString(list));
        lotteryDrawHandle.getBatchBoundsInfoOfLottery(lotteryType, winNumber, list);
        System.out.println(JSON.toJSONString(list));


    }
}
