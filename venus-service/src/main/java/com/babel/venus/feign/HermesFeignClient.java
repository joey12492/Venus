package com.babel.venus.feign;

import com.babel.common.core.data.RetData;
import com.babel.hermes.commons.HermesServer;
import com.babel.venus.client.AuthorizedFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * User: joey
 * Date: 2018/1/1
 * Time: 12:16
 */
@AuthorizedFeignClient(name = HermesServer.SERVER_NAME)
public interface HermesFeignClient {


    @PostMapping(value = "/apis/repair/no_draw_orders", name = "手动修复未执行开奖流程的订单，并入库")
    RetData<Boolean> repairNoDrawOrders(@RequestParam(name = "pcode") Long pcode,
                                        @RequestParam(name = "pdate") Integer pdate,
                                        @RequestParam(name = "lotteryId") Long lotteryId,
                                        @RequestParam(name = "noProcOrderIds") List<String> noProcOrderIds, //未执行开奖的订单id
                                        @RequestParam(name = "incompleteOrders") List<String> incompleteOrders
    );
}
