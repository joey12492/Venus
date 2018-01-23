//package com.babel.venus.schedule;
//
//import com.babel.common.core.util.DateUtils;
//import com.babel.forseti.entity.PeriodDataPO;
//import com.babel.forseti_order.dto.PeriodDataDTO;
//import com.babel.venus.assemble.UserOrderAssemService;
//import com.babel.venus.constants.VenusConstants;
//import com.babel.venus.service.feign.VenusOutServerConsumer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
///**
// * User: joey
// * Date: 2017/10/5
// * Time: 17:03
// * 定时开奖
// */
//@Component
//public class DrawWinJob {
//
//    private static final Logger log = LoggerFactory.getLogger(DrawWinJob.class);
//    @Resource
//    private VenusOutServerConsumer venusOutServerConsumer;
//    @Resource
//    private UserOrderAssemService userOrderAssemService;
//
//    /**
//     * 模拟定时获取奖源，定时开奖
//     */
//    @Scheduled(fixedRate = VenusConstants.three_minutes)
//    public void drawPrize() {
//        long timestamp = System.currentTimeMillis();
//        List<PeriodDataDTO> list = venusOutServerConsumer.getPriodDataNewly(1L, null);
//        if (list != null && list.size() > 0) {
//            for (PeriodDataDTO dto : list) {
//                if (timestamp > dto.getStartTime() && timestamp < dto.getEndTime()) {
//                    long endTime = dto.getEndTime() + 150_000;
//                    boolean flag = true;
//                    while (flag) {
//                        try {
//                            Thread.sleep(20000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        long currentTime = System.currentTimeMillis();
//                        if (currentTime > endTime) {
//                            flag = false;
//                            log.info("start draw prize , pcode :{}, issue:{} , currentTime :{},  endTime:{}", dto.getPcode(), "cq_ssc", DateUtils.format(new Date(currentTime),DateUtils.SDF_FORMAT_DATETIME), DateUtils.format(new Date(endTime), DateUtils.SDF_FORMAT_DATETIME));
//                            userOrderAssemService.drawLottery(dto.getPcode(), "cq_ssc", null);
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//
//
//
//}
