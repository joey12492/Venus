package com.babel.venus.feign;


import com.babel.ares.analysis.api.model.PrizeNumberPO;
import com.babel.ares.analysis.api.model.PrizeNumberVO;
import com.babel.ares.analysis.constants.AresAnalysisServer;
import com.babel.ares.server.AresServer;
import com.babel.venus.client.AuthorizedFeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@AuthorizedFeignClient(name = AresAnalysisServer.SERVER_NAME)
public interface AnalysisFeignClient {

    /**
     * 获取彩种和开奖号码
     * @param prizeNumberVO
     * @return
     */
    @RequestMapping(value = {AresAnalysisServer.PRIZE_NUMBER_GET4VO})
    List<PrizeNumberPO> getListByIssue(@RequestBody PrizeNumberVO prizeNumberVO);

    /**
     * 修改奖源的开奖状态的状态
     * @param prizeNumber
     * @return
     */
    @RequestMapping(value = {AresAnalysisServer.PRIZE_NUMBER_UPDATE})
    int updatePrizeNumber(@RequestBody PrizeNumberPO prizeNumber);
}
