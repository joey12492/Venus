package com.babel.venus.service.feign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.babel.common.lottery.StatusType;
import com.babel.venus.feign.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.babel.account.po.PlatLottery;
import com.babel.api.riskmanagement.entity.RiskCheckResult;
import com.babel.ares.analysis.api.model.PrizeNumberPO;
import com.babel.ares.analysis.api.model.PrizeNumberVO;
import com.babel.ares.model.OddsItemPO;
import com.babel.ares.vo.OddsVO;
import com.babel.common.core.data.RetData;
import com.babel.common.core.util.ResponseData;
import com.babel.forseti_order.dto.UserOrderStatusDTO;
import com.babel.loky.logs.RecentlyErrors;
import com.babel.venus.util.AssembleHttpUtils;


@Component
public class VenusOutServerConsumer {

    private static final Logger log = LoggerFactory.getLogger(VenusOutServerConsumer.class);

    @Autowired
    private PlatInfoFeignClient platInfoFeignClient;
    @Autowired
    private ConfigFeignClient configFeignClient;
    @Autowired
    private ForsetiFeignClient forsetiFeignClient;
    @Autowired
    private RiskFeignClient riskFeignClient;
    @Autowired
    private AnalysisFeignClient analysisFeignClient;
    @Resource
    private AssembleHttpUtils assembleHttpUtils;
    @Resource
    private HermesFeignClient hermesFeignClient;

    /**
     * 平台商赔率列表
     *
     * @param platInfoId
     * @param lotteryId
     * @return
     */
    public List<PlatLottery> lotteryList(Long platInfoId, Long lotteryId) {
        try {
            log.info("--> req account lotteryList, platInfoId :{} ,lotteryId:{}", platInfoId, lotteryId);
            RetData<List<PlatLottery>> resp = platInfoFeignClient.lotteryList(platInfoId, lotteryId, StatusType.ENABLED.code());
            return resp.getData();
        } catch (Exception e) {
            log.error("--> req account lotteryList error, platInfoId:{}, lotteryId:{}", platInfoId, lotteryId, e);
        }
        return null;
    }

    /**
     * 获取所有的赔率信息
     *
     * @return
     */
    public List<OddsItemPO> queryOddsItemsList(Long oddsId, Long lotteryId, Long payoffGroupId) {
        try {

            RetData<OddsVO> oddsVORetData = configFeignClient.getOdds(oddsId, lotteryId, payoffGroupId);
            if (oddsVORetData.getData() != null && oddsVORetData.getData().getItemPO() != null) {
                log.info("--> req config queryOddsItemsList,oddsId :{} ,lotteryId:{}, payoffGroupId:{},  result :{}", oddsId, lotteryId, payoffGroupId, JSON.toJSONString(oddsVORetData.getData().getItemPO()));
                return oddsVORetData.getData().getItemPO();
            }
            log.info("--> req config queryOddsItemsList, oddsId :{} ,lotteryId:{}, payoffGroupId:{}", oddsId, lotteryId, payoffGroupId);
            return null;
        } catch (Exception e) {
            log.error("--> req config queryOddsItemsList error, oddsId:{}, lotteryId:{}, payoffGroupId:{} ", oddsId, lotteryId, payoffGroupId, e);
            RecentlyErrors.logExp(this.getClass(), "--> req-config-queryOddsItemsList", String.format("oddsId:%d, lotteryId:%d, payoffGroupId:%d", oddsId, lotteryId, payoffGroupId));
        }
        return null;
    }

    /**
     * 修改订单状态
     *
     * @param statusOrder
     * @return
     */
    public void updateOrderStatus(UserOrderStatusDTO statusOrder) {
        log.info("--> req forseti updateOrderStatus ,params:{} ", JSON.toJSON(statusOrder));
        try {
            RetData retData = forsetiFeignClient.updateOrderStatus(statusOrder);
            if (!retData.getErr().equals("SUCCESS")) {
                log.error("--> forseti updateOrderStatus error, result: {}, param:{}", JSON.toJSONString(retData), JSON.toJSONString(statusOrder));
            } else {
                log.info("--> forseti updateOrderStatus suc,lotteryId:{},pcode:{}, result:{} ", statusOrder.getLotteryId(), statusOrder.getPcode(), JSON.toJSON(retData));
            }

        } catch (Exception e) {
            log.error("--> forseti updateOrderStatus error, params:{}, ", JSON.toJSONString(statusOrder), e);
            RecentlyErrors.logExp(this.getClass(), "req-forseti-updateOrderStatus", JSON.toJSONString(statusOrder));
        }
    }


    /**
     * 处理单个中奖
     *
     * @param order
     * @return
     */
    public RiskCheckResult disposeBingoOrder(String order) {
        try {
            RetData<RiskCheckResult> retData = riskFeignClient.disposeBingoOrder(order);
            if (!retData.getErr().equals("SUCCESS")) {
                log.error("--> risk disposeBingoOrder failed, result: {}, param:{}", JSON.toJSONString(retData), JSON.toJSONString(order));
            } else {
                return retData.getData();
            }
        } catch (Exception e) {
            log.error("--> risk disposeBingoOrder error, params:{}, ", JSON.toJSONString(order), e);
            RecentlyErrors.logExp(this.getClass(), "req-risk-disposeBingoOrder", order);
        }
        return null;

    }

    /**
     * 处理单个未中奖
     *
     * @param order
     * @return
     */
    public RiskCheckResult disposeNotWinOrder(String order) {
        try {
            RetData<RiskCheckResult> retData = riskFeignClient.disposeNotWinOrder(order);
            if (!retData.getErr().equals("SUCCESS")) {
                log.error("--> risk disposeNotWinOrder failed, result: {}, param:{}", JSON.toJSONString(retData), JSON.toJSONString(order));
            } else {
                return retData.getData();
            }
        } catch (Exception e) {
            log.error("--> risk disposeNotWinOrder error, params:{}, ", JSON.toJSONString(order), e);
            RecentlyErrors.logExp(this.getClass(), "req-risk-disposeNotWinOrder", order);
        }
        return null;
    }


    /**
     * 调用风控系统测试多个中奖订单
     *
     * @param orderListString
     * @return
     */
    public Map<String, RiskCheckResult> disposeMulBingoOrder(String orderListString) {
        try {
            RetData<Map<String, RiskCheckResult>> result = riskFeignClient.disposeMulBingoOrder(orderListString);
            if (!result.getErr().equals(RetData.SUCCESS)) {
                return new HashMap<>();
            }
            return result.getData();
        } catch (Exception e) {
            log.error("--> req risk disposeMulBingoOrder error,", e);
            RecentlyErrors.logExp(this.getClass(), "req-risk-disposeMulBingoOrder", orderListString);
        }
        return null;
    }

    /**
     * 处理多个未中奖
     *
     * @param orderListString
     */
    public void disposeMulNotWinOrder(String orderListString) {
        try {
            riskFeignClient.disposeMulNotWinOrder(orderListString);
        } catch (Exception e) {
            log.error("--> risk disposeMulNotWinOrder error, params:{}, ", orderListString, e);
            RecentlyErrors.logExp(this.getClass(), "req-risk-disposeMulNotWinOrder", orderListString);
        }
    }


    /**
     * 当一期开完奖以后调用
     *
     * @param pdate
     * @param lotteryId
     * @param pcode
     * @return
     */
    public void disposeForOnePcodeEnd(int pdate, long lotteryId, long pcode) {
        log.info("--> req risk disposeForOnePcodeEnd ,params, pdate:{}, lotteryId:{}, pcode:{} ", pdate, lotteryId, pcode);
        try {
            RetData retData = riskFeignClient.disposeForOnePcodeEnd(pdate, lotteryId, pcode);
            if (!retData.getErr().equals("SUCCESS")) {
                log.error("--> risk disposeForOnePcodeEnd failed, result: {}, params, pdate:{}, lotteryId:{}, pcode:{} ", JSON.toJSONString(retData), pdate, lotteryId, pcode);
            }
        } catch (Exception e) {
            log.error("--> forseti updateOrderStatus error, params, pdate:{}, lotteryId:{}, pcode:{} ", pdate, lotteryId, pcode, e);
            RecentlyErrors.logExp(this.getClass(), "req-risk-disposeForOnePcodeEnd", String.format("pdate:%d, lotteryId:%d, pcode:%d", pdate, lotteryId, pcode));
        }
    }


    /**
     * 获取奖源
     *
     * @param pcode       期数
     * @param code        彩种缩写名称
     * @param prizeStatus 彩种开奖状态
     * @return
     */
    public List<PrizeNumberPO> getListByIssue(Long pcode, String code, Long lotteryCId, Integer prizeStatus, Long lotteryId) {
        try {
            PrizeNumberVO vo = new PrizeNumberVO();
            List<String> issues = new ArrayList<>();
            issues.add(pcode.toString());
            vo.setIssueList(issues);
            if (StringUtils.isNotBlank(code)) {
                vo.setCode(code);
            }
            if (lotteryCId != null) {
                vo.setCid(lotteryCId);
            }
            if (lotteryId != null) {
                vo.setLotteryId(lotteryId);
            }
            if (prizeStatus != null) {
                vo.setPrizeStatus(prizeStatus);
            }
            List<PrizeNumberPO> list = analysisFeignClient.getListByIssue(vo);
            log.info("--> analysis req getListByIssue success, param, pcode :{}, code :{}, result :{}", pcode, code, JSON.toJSONString(list));
            return list;
        } catch (Exception e) {
            log.error("--> analysis req getListByIssue error, param, pcode :{}, code :{}", pcode, code, e);
            RecentlyErrors.logExp(this.getClass(), "req-analysis-getListByIssue", String.format("pcode:%d, code:%s, lotteryCId:%d, prizeStatus:%d", pcode, code, lotteryCId, prizeStatus));
        }
        return null;
    }

    /**
     * 修改奖源的开奖状态
     *
     * @param
     * @return
     */
    public boolean updatePrizeNumber(Long pcode, Long lotteryId, Integer prizeStatus) {
        try {
            PrizeNumberPO po = new PrizeNumberPO();
            po.setIssue(pcode.toString());
            po.setLotteryId(lotteryId);
            po.setPrizeStatus(prizeStatus);
            int result = analysisFeignClient.updatePrizeNumber(po);
            log.info("--> analysis req updatePrizeNumber success ,param , pcode :{}, lotteryId:{} , result:{}", pcode, lotteryId, result);
            return result > 0;
        } catch (Exception e) {
            log.error("--> analysis req updatePrizeNumber error ,param , pcode :{}, lotteryId:{} ", pcode, lotteryId, e);
            RecentlyErrors.logExp(this.getClass(), "req-analysis-updatePrizeNumber", String.format("pcode:%d,  lotteryId:%d, prizeStatus:%d", pcode, lotteryId, prizeStatus));
        }
        return false;
    }


    public String drawPrizeInternal(String host, Long pCode, String code, Long lotteryCId, Integer drawPrizeStatus) {
        Object[] objects = new Object[]{pCode, code, lotteryCId, drawPrizeStatus};
        ResponseData result = assembleHttpUtils.execRequest(host, AssembleHttpUtils.SendReq.VENUS_DRAW_PRIZE_INTERNAL, objects);
        if (!result.isFlag()) {
            return null;
        }
        return result.getResult();
    }

    /**
     * 开奖完成后调用hermes执行etl
     *
     * @param pcode
     * @param lotteryId
     * @param procStatus
     * @param prizeStatus
     * @return
     */
    public String hermesDrawPrizeSucHandler(String host, Long pcode, Long lotteryId, Integer procStatus, Integer prizeStatus, boolean repairEnter) {
        Object[] objects = new Object[]{pcode, lotteryId, procStatus, prizeStatus, repairEnter};
        ResponseData result = assembleHttpUtils.execRequest(host.trim(), AssembleHttpUtils.SendReq.HERMES_DRAW_PRIZE_SUC_HANDLER, objects);
        if (!result.isFlag()) {
            return null;
        }
        return result.getResult();
    }

    public boolean repairNoDrawOrders(Long pcode, Integer pdate, Long lotteryId, List<String> noProcOrderIds, List<String> incompleteOrders) {
        try {
            if (noProcOrderIds.size() == 0 && incompleteOrders.size() == 0) {
                return false;
            }
            RetData<Boolean> retData = hermesFeignClient.repairNoDrawOrders(pcode, pdate, lotteryId, noProcOrderIds, incompleteOrders);
            return retData.getData();
        } catch (Exception e) {
            log.error("--> hermes repairNoDrawOrders error: noProceOrderIds:{}, incompleteOrderIds:{}", noProcOrderIds, incompleteOrders, e);
        }
        return false;
    }

    public Long getPdateByPCode(Long lotteryId, Long pcode) {
        try {
            RetData<Long> retData = forsetiFeignClient.getPriodDataPcode(lotteryId, pcode);
            return retData.getData();
        } catch (Exception e) {
            log.error("--> forseti getPriodDataPcode error, lotteryId:{}, pcode:{} ", lotteryId, pcode, e);
        }
        return 0L;
    }

    public Map<Long, Integer> getLotteryTypes() {
        try {
            RetData<Map<Long, Integer>> retData = forsetiFeignClient.getLotteryTypes();
            log.info("--> forseti getLotteryTypes result :{}",JSON.toJSONString(retData.getData()));
            if (retData.getData() != null) {
                return retData.getData();
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("--> forseti getLotteryTypes error::",e);
        }
        return new HashMap<>();
    }

    public Long getPlatInfoByTrialId(Long trialPlatId) {
        try {
            RetData<Long> retData = platInfoFeignClient.getPlatInfoByTrialId(trialPlatId);
            log.info("--> platInfo getPlatInfoByTrialId, param:{}, result :{}", trialPlatId ,retData.getData());
            return retData.getData();
        } catch (Exception e) {
            log.error("--> platinfo getPlatInfoByTrialId error, trialPlatId:{}", trialPlatId, e);
        }
        return null;
    }

}
