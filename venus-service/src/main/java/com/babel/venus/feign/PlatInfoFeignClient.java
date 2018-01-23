package com.babel.venus.feign;

import com.babel.account.commons.AccountServer;
import com.babel.account.po.PlatLottery;
import com.babel.common.core.data.RetData;
import com.babel.venus.client.AuthorizedFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * User: joey
 * Date: 2017/9/29
 * Time: 15:22
 */
@AuthorizedFeignClient(name = AccountServer.SERVER_NAME)
public interface PlatInfoFeignClient {

    @RequestMapping(value = AccountServer.plat_lottery_odds_list, method = RequestMethod.GET)
    RetData<List<PlatLottery>> lotteryList(
            @RequestParam(name = "platInfoId", required = false) Long platInfoId,
            @RequestParam(name = "lotteryId", required = false) Long lotteryId,
            @RequestParam(name = "status", required = false) Integer status
    );


    @GetMapping(value = AccountServer.get_real_plat_id_by_trial)
    RetData<Long> getPlatInfoByTrialId(
            @RequestParam(name = "trialPlatInfoId") Long trialPlatInfoId
    );
}
