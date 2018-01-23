package com.babel.venus.web.rest;

import javax.annotation.Resource;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.vo.UserOrderPayoffVo;
import com.bc.lottery.entity.LotteryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.babel.common.core.data.RetData;
import com.babel.common.lottery.ReqResultStatus;
import com.babel.venus.assemble.UserOrderAssemService;
import com.babel.venus.constants.VenusServer;
import com.codahale.metrics.annotation.Timed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LotteryResource {


    private static final Logger logger = LoggerFactory.getLogger(LotteryResource.class);

    @Resource
    private UserOrderAssemService userOrderAssemService;


    /**
     * @return
     */
    @PostMapping(path = VenusServer.DRAW_PRIZE_BUS, name = "执行开奖流程,开奖消息总线", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<RetData<Integer>> drawLotteryReq(
        @RequestParam(name = "pcode") Long pcode,
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "lotteryCId", required = false) Long lotteryCId,
        @RequestParam(name = "drawPrizeStatus", required = false, defaultValue = "0") Integer drawPrizeStatus //
    ) {
        boolean flag = userOrderAssemService.drawLotteryReq(pcode, code, lotteryCId, drawPrizeStatus);
        RetData<Integer> retData = new RetData<>(flag ? ReqResultStatus.success.status() : ReqResultStatus.failed.status());
        return new ResponseEntity<>(retData, HttpStatus.OK);
    }

    /**
     * @return
     */
    @PostMapping(path = VenusServer.DRAW_PRIZE_INTERNAL, name = "执行开奖流程,供内部服务调用，实现多个服务同时处理", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Timed
    public ResponseEntity<RetData<Boolean>> drawLotteryInternal(
        @RequestParam(name = "pcode") Long pcode,
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "lotteryCId", required = false) Long lotteryCId,
        @RequestParam(name = "drawPrizeStatus", required = false, defaultValue = "0") Integer drawPrizeStatus //
    ) {
        boolean flag = userOrderAssemService.drawLottery(pcode, code, lotteryCId, drawPrizeStatus, false);
        RetData<Boolean> retData = new RetData<>(flag);
        return new ResponseEntity<>(retData, HttpStatus.OK);
    }


    /**
     * @return
     */
    @PostMapping(path = VenusServer.MANUAL_DRAW_PRIZE, name = "手动执行开奖流程", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Timed
    public ResponseEntity<RetData<Integer>> manualDrawLottery(
        @RequestParam(name = "pcode") Long pcode,
        @RequestParam(name = "lotteryId") Long lotteryId,
        @RequestParam(name = "winNums", required = false, defaultValue = "") String winNums //
    ) {
        boolean flag = userOrderAssemService.manualDrawLottery(pcode, lotteryId, winNums);
        RetData<Integer> retData = new RetData<>(flag ? ReqResultStatus.success.status() : ReqResultStatus.failed.status());
        return new ResponseEntity<>(retData, HttpStatus.OK);
    }

    @PostMapping(path = VenusServer.REPAIR_DRAW_PRIZE, name = "号码验证失败需要补录，执行开奖", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RetData<Boolean> repairDrawPrize(
        @RequestParam(name = "pcode") Long pcode,
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "lotteryCId", required = false) Long lotteryCId,
        @RequestParam(name = "drawPrizeStatus", required = false, defaultValue = "0") Integer drawPrizeStatus
    ) {
        boolean flag = userOrderAssemService.drawLottery(pcode, code, lotteryCId, drawPrizeStatus, true);
        RetData<Boolean> retData = new RetData<>(flag);
        return retData;
    }


    @PostMapping(value = "/apis/lottery/repair_no_draw_orders",name = "修复未执行开奖流程的订单")
    public RetData<Boolean> repairNoDrawOrders(
        @RequestParam(name = "pcode") Long pcode,
        @RequestParam(name = "code") String code,
        @RequestParam(name = "pdate") Integer pdate,
        @RequestParam(name = "lotteryId") Long lotteryId,
        @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(name = "size", required = false, defaultValue = "200") Integer size
    ) {
        boolean flag =userOrderAssemService.repairNoDrawOrders(pcode, code, pdate, lotteryId, page, size);
        return new RetData<>(flag);
    }


}
