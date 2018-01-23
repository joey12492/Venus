package com.babel.venus.feign;

import com.babel.common.core.data.RetData;
import com.babel.forseti.constant.ForsetiServer;
import com.babel.forseti_order.dto.PeriodDataDTO;
import com.babel.forseti_order.dto.UserOrderStatusDTO;
import com.babel.venus.client.AuthorizedFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@AuthorizedFeignClient(name = "forseti")
public interface ForsetiFeignClient {


    @GetMapping(value = ForsetiServer.PERIOD_DATA_NEWLY, name = "获取最近三期奖期列表")
    RetData<List<PeriodDataDTO>> getPriodDataNewly(
            @RequestParam(value = "lotteryId", required = false) Long lotteryId,
            @RequestParam(value = "maxUpdateTime", required = false) Long maxUpdateTime);

    @PostMapping(path = ForsetiServer.ORDERS_UPDATE_ORDER_STATUS, name = "修改订单状态")
    RetData updateOrderStatus(@RequestBody UserOrderStatusDTO statusOrder);


    @GetMapping(value = ForsetiServer.PERIOD_DATA_PCODE, name = "根据期数获取pdate")
    RetData<Long> getPriodDataPcode(
            @RequestParam(value = "lotteryId", required = false) Long lotteryId,
            @RequestParam(value = "pcode", required = false) Long pcode);

    @GetMapping(value = ForsetiServer.LOTTERY_TYPES, name = "彩种类型查询")
    RetData<Map<Long, Integer>> getLotteryTypes();
}
