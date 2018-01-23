package com.babel.venus.feign;

import com.babel.ares.model.LotteryPO;
import com.babel.ares.server.AresServer;
import com.babel.ares.vo.OddsVO;
import com.babel.common.core.data.RetData;
import com.babel.venus.client.AuthorizedFeignClient;
import com.codahale.metrics.annotation.Timed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * User: joey
 * Date: 2017/10/3
 * Time: 11:11
 */
@AuthorizedFeignClient(name = AresServer.server_name)
public interface ConfigFeignClient {

    /**
     * 获取赔率列表
     * @param cid
     * @param lotteryId
     * @return
     */
    @GetMapping(AresServer.ODDS)
    RetData<OddsVO> getOdds(
            @RequestParam(value = "oddsId", required = false) Long cid,
            @RequestParam(value = "lotteryId", required = false) Long lotteryId,
            @RequestParam(value = "payoffGroupId", required = false) Long payoffGroupId
    );

    /**
     * 获取彩种列表
     * @param sideType
     * @return
     */
    @GetMapping(AresServer.LOTTERYS)
    RetData<List<LotteryPO>> getLotterys(@RequestParam(value = "sideType", required = false) Integer sideType);
}
