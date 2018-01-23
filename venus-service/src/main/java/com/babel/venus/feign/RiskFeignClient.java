package com.babel.venus.feign;

import com.babel.api.riskmanagement.entity.RiskCheckResult;
import com.babel.api.riskmanagement.remote.service.RiskManagementServer;
import com.babel.common.core.data.RetData;
import com.babel.venus.client.AuthorizedFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User: joey
 * Date: 2017/9/19
 * Time: 10:43
 */
@AuthorizedFeignClient(name = "riskmanagementweb")
public interface RiskFeignClient {


    @RequestMapping(name = "处理单个中奖", value = RiskManagementServer.ORDER_DISPOSE_SINGLE_BINGO_ORDER,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public RetData<RiskCheckResult> disposeBingoOrder(@RequestBody String order);

    @RequestMapping(name = "处理单个未中奖", value = RiskManagementServer.ORDER_DISPOSE_SINGLE_NOTWIN_ORDER,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    RetData<RiskCheckResult> disposeNotWinOrder(@RequestBody String order);


    @RequestMapping(name = "处理多个中奖", value = RiskManagementServer.ORDER_DISPOSE_MUL_BINGO_ORDER,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    RetData<Map<String, RiskCheckResult>> disposeMulBingoOrder(@RequestBody String orderListString);


    @RequestMapping(name = "处理多个未中奖", value = RiskManagementServer.ORDER_DISPOSE_MUL_NOTWIN_ORDER,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    void disposeMulNotWinOrder(@RequestBody String orderListString);


    @RequestMapping(name = "当一期开完奖以后调用", value = RiskManagementServer.DISPOSE_FOR_ONE_PCODE_END,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    RetData<Map<String, Object>> disposeForOnePcodeEnd(
            @RequestParam(name = "pdate") int pdate,
            @RequestParam(name = "lotteryId") long lotteryId,
            @RequestParam(name = "pcode") long pcode
    );

}
