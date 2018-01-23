package com.babel.venus.web.rest;

import com.babel.common.core.data.RetData;
import com.babel.venus.assemble.UserOrderAssemService;
import com.babel.venus.constants.VenusServer;
import com.babel.venus.vo.UserOrderPayoffVo;
import com.bc.lottery.entity.LotteryType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * User: joey
 * Date: 2017/11/27
 * Time: 10:41
 */
@RestController
@RequestMapping("/")
public class InternalTestResource {


    @Resource
    private UserOrderAssemService userOrderAssemService;

    @PostMapping(path = VenusServer.SINGLE_ORDER_PAYOFF, name = "只执行派奖流程，不执行其他，内部测试使用", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RetData<Map<String, Long>> manualPayoff(
        @RequestBody List<UserOrderPayoffVo> orders,
        @RequestParam(name = "lotteryId") Long lotteryId,
        @RequestParam(name = "playId") Long playId,
        @RequestParam(name = "platInfoId") Long platInfoId,
        @RequestParam(name = "acType", required = false, defaultValue = "1") Integer acType

    ) {
        LotteryType lotteryType = LotteryType.parseType(lotteryId, playId);
        Map<String, Long> payoffResult = userOrderAssemService.manualPayoff(orders, lotteryType, playId, lotteryId, platInfoId, acType);
        return new RetData<>(payoffResult);
    }
}
