package com.babel.venus.assemble;

import com.alibaba.fastjson.JSON;
import com.babel.api.riskmanagement.entity.RiskCheckResult;
import com.babel.ares.analysis.api.model.PrizeNumberPO;
import com.babel.common.lottery.AcType;
import com.babel.common.lottery.DrawPrizeStatus;
import com.babel.common.lottery.MoneyMode;
import com.babel.common.lottery.OrderStatus;
import com.babel.forseti_order.dto.MemberStatusOrderDTO;
import com.babel.forseti_order.dto.UserOrderStatusDTO;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.config.VenusRefreshConfig;
import com.babel.venus.constants.RedisConstants;
import com.babel.venus.enums.ProcessStatus;
import com.babel.venus.enums.WhetherType;
import com.babel.venus.service.UserOrderService;
import com.babel.venus.service.feign.VenusOutServerConsumer;
import com.babel.venus.storage.redis.CommonRedisService;
import com.babel.venus.storage.redis.DrawPrizeLockRedisService;
import com.babel.venus.util.PayoffUtil;
import com.babel.venus.util.ThreadTaskPoolFactory;
import com.babel.venus.util.VenusUtil;
import com.babel.venus.vo.BaseQueryVo;
import com.babel.venus.vo.UserOrderPayoffVo;
import com.babel.venus.vo.UserOrderVo;
import com.bc.lottery.draw.service.LotteryDrawHandle;
import com.bc.lottery.draw.service.impl.LotteryDrawServiceImpl;
import com.bc.lottery.entity.LotteryType;
import com.bc.lottery.pour.service.LotteryPourHandle;
import com.bc.lottery.pour.service.impl.ShishicaiPourServiceImpl;
import com.bc.lottery.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * User: joey
 * Date: 2017/9/4
 * Time: 16:51
 */
@Service
public class UserOrderAssemService {


    private static final Logger logger = LoggerFactory.getLogger(UserOrderAssemService.class);

    private ExecutorService THREAD_POOL = ThreadTaskPoolFactory.coreThreadTaskPool;

    private ExecutorService HTTP_THREAD_POOL = ThreadTaskPoolFactory.httpCoreThreadTaskPool;

    private ExecutorService CHECK_REQ_HERMES_LOCK = ThreadTaskPoolFactory.checkReqHermesLock;
    @Resource
    private VenusRefreshConfig venusRefreshConfig;
    @Resource
    private VenusOutServerConsumer venusOutServerConsumer;

    @Resource
    private UserOrderService userOrderService;

    @Resource
    private CommonRedisService commonRedisService;

    //    @Resource(name = "kafkaTemplate")
//    private KafkaTemplate<Integer, String> kafkaTemplate;

    private LotteryDrawHandle lotteryDrawHandle = new LotteryDrawServiceImpl();

    private LotteryPourHandle lotteryPourHandle = new ShishicaiPourServiceImpl();
    @Resource
    private DrawPrizeLockRedisService drawPrizeLockRedisService;
    @Resource
    private PayoffUtil payoffUtil;
    @Resource
    private VenusUtil venusUtil;


    /**
     * 手动开奖
     */
    public boolean manualDrawLottery(Long pcode, Long lotteryId, String winNums) {
        List<PrizeNumberPO> prizeNumberPOs = venusOutServerConsumer.getListByIssue(pcode, null, null, null, lotteryId);
        if (prizeNumberPOs == null || prizeNumberPOs.size() == 0) {
            logger.error("--> analysis get prizeNumberPOs is null , pcode:{}, lotteryId:{}, winNums:{}", pcode, lotteryId, winNums);
            return false;
        }
//        //开奖主流程

//        //设置执行开奖流程的程序数量
//        drawPrizeLockRedisService.setVenusDrawLock(lotteryId, pcode);
//        logger.info("--> start official lottery, lotteryId : " + lotteryId);
//        drawLotteryCore(pcode, lotteryId, winNums, prizeNumberPOs.get(0).getPrizeStatus(), false, null);

        long dualLotteryId = lotteryId;
        //设置执行开奖流程的程序数量
        drawPrizeLockRedisService.setVenusDrawLock(dualLotteryId, pcode);
        logger.info("--> start dual lottery, lotteryId : " + dualLotteryId);
        drawLotteryCore(pcode, dualLotteryId, winNums, prizeNumberPOs.get(0).getPrizeStatus(), false, null);
        return true;
    }

    /**
     * 消息总线，接收开奖消息，然后循环调用需要请求的服务
     *
     * @param pCode
     * @param code
     * @param lotteryCId
     * @param drawPrizeStatus
     */
    public boolean drawLotteryReq(Long pCode, String code, Long lotteryCId, Integer drawPrizeStatus) {
        if (StringUtils.isBlank(venusRefreshConfig.getVenusReqDrawHosts())) {
            logger.error("--> no reqDrawHosts");
            return false;
        }
        String[] reqDrawHostSplit = venusRefreshConfig.getVenusReqDrawHosts().split(",");
        for (String host : reqDrawHostSplit) {
            HTTP_THREAD_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = venusOutServerConsumer.drawPrizeInternal(host, pCode, code, lotteryCId, drawPrizeStatus);
                        if (result == null) {
                            logger.error("--> venus req venus drawPrizeInternal false,result is null, host:{}, pCode:{}, code :{}, lotteryCid:{}, drawPrizeStatus:{}", host, pCode, code, lotteryCId, drawPrizeStatus);
                        } else {
                            logger.info("--> venus req venus drawPrizeInternal complete, host:{}, pCode:{}, code :{}, lotteryCid:{}, drawPrizeStatus:{}, result:{}", host, pCode, code, lotteryCId, drawPrizeStatus, result);
                        }
                    } catch (Exception e) {
                        logger.error("--> venus req venus drawLotteryReq error, pCode:{}, code:{}", pCode, code, e);
                    }
                }
            });
        }
        return true;
    }

    /**
     * 执行开奖流程
     * //todo 此处要改为通过spring cloud 总线来处理，保证所有的机器都能拿到消息
     */
    public boolean drawLottery(Long pCode, String code, Long lotteryCId, Integer drawPrizeStatus, boolean repairEnter) {
        if (StringUtils.isEmpty(code) && lotteryCId == null) {
            logger.error("--> code and lotteryCId can't be all null");
            return false;
        }
        List<PrizeNumberPO> prizeNumberPOs = venusOutServerConsumer.getListByIssue(pCode, code, lotteryCId, drawPrizeStatus, null);
        if (prizeNumberPOs == null || prizeNumberPOs.size() == 0) {
            logger.error("--> analysis get prizeNumberPOs is null ");
            return false;
        }
        String winNum = prizeNumberPOs.get(0).getNumbers();
        if (StringUtils.isEmpty(winNum)) {
            logger.error("--> there isn't winNum, winNum : {}", winNum);
            return false;
        }
        for (PrizeNumberPO lotteryPO : prizeNumberPOs) {
            //todo 此处暂时注释官彩
//            //设置执行开奖流程的程序数量
//            drawPrizeLockRedisService.setVenusDrawLock(lotteryPO.getLotteryId(), pCode);

//            logger.info("--> start official lottery, lotteryId : " + lotteryPO.getLotteryId());
//            drawLotteryCore(pCode, lotteryPO.getLotteryId(), winNum, drawPrizeStatus, repairEnter, prizeNumberPOs.get(0).getPrizePublishTime());
            if (lotteryPO.getLotteryId() % 2 == 0) {

                long dualLotteryId = lotteryPO.getLotteryId();
                logger.info("--> start dual lottery, lotteryId : " + dualLotteryId);
                drawPrizeLockRedisService.setVenusDrawLock(dualLotteryId, pCode);
                drawLotteryCore(pCode, dualLotteryId, winNum, drawPrizeStatus, repairEnter, prizeNumberPOs.get(0).getPrizePublishTime());
            }
        }
        return true;
    }

    /**
     * 开奖
     *
     * @param pCode
     * @param lotteryId
     * @param winNum
     */
    private void drawLotteryCore(Long pCode, Long lotteryId, String winNum, Integer prizeStatus, boolean repairEnter, Long drawPrizeTime) {
        Long pdate= venusUtil.getPdate(lotteryId, pCode);
        //执行本期下注成功未开奖的追单数据
        if (commonRedisService.getChaseDrawWinLock(pCode, lotteryId) < 2) {
            procCurrentChasePCodeBetSuc(pCode, lotteryId, winNum, pdate.intValue());
        }
        UserOrderVo vo = new UserOrderVo(pCode, lotteryId);
        Set<String> playIdKeys = userOrderService.getCurrentPCodePlayIdKeys(vo);
        if (playIdKeys == null || playIdKeys.size() == 0) {
            logger.info("--> current playIdKeys is null, pCode : {}, lotteryId :{}", vo.getPcode(), vo.getLotteryId());
        } else {
            for (String playIdKey : playIdKeys) {
                int count = 0;
                List<String> orderIds = userOrderService.batchPop(playIdKey);
                vo.setOrderStatus(OrderStatus.bet_success.code());
                vo.setIfChase(WhetherType.yes.code());
                while (orderIds != null && orderIds.size() > 0 && count++ < RedisConstants.BATCH_POP_PROC_TIME) {
                    List<UserOrderPO> orders = userOrderService.batchGetCacheOrders(lotteryId, pdate, orderIds);
                    if (orders != null && orders.size() > 0) {
                        flushMultData(orders, winNum, lotteryId, pCode, prizeStatus, repairEnter, drawPrizeTime);
                    }
                    orderIds = userOrderService.batchPop(playIdKey);
                }
            }
        }
        //开奖监控状态,analysis
        venusOutServerConsumer.updatePrizeNumber(pCode, lotteryId, DrawPrizeStatus.DRAW_SUCCESS.code());
        //订单处理完调用风控
        venusOutServerConsumer.disposeForOnePcodeEnd(pdate.intValue(), lotteryId, pCode);
        //todo 发送http通知，执行etl,注意要考虑多机器如何处理，要保证所有机器都执行完才开始写中奖完成的数据
        sendDrawSucNotice(pCode, lotteryId, ProcessStatus.success.status(), prizeStatus, repairEnter);

    }


    /**
     * 开奖当期下注成功并且是追号的数据
     */
    private void procCurrentChasePCodeBetSuc(long pCode, long lotteryId, String winNum, Integer pdate) {
        //todo 1、获取数据库中当期的追单、待开奖的数据，执行开奖流程，只有一台机器执行，需要redis锁,此处可能需要单条处理
        BaseQueryVo _vo = new BaseQueryVo(pCode, WhetherType.yes.code(), OrderStatus.bet_success.code(), pdate, 1);
        _vo.setLotteryId(lotteryId);
        List<UserOrderPO> currentCodeOrders = userOrderService.queryMongoOrders(_vo);
        if (currentCodeOrders != null && currentCodeOrders.size() > 0) {
            currentCodeOrders.forEach((order) -> {
                order.setBetContentProc(lotteryPourHandle.getLotteryListByType(lotteryId, order.getPlayId(), order.getBetContent()));
                flushOneData(order, winNum);
            });
        }
    }

    /**
     * 剔除无效的追单数据，并且将有效订单执行开奖流程
     *
     * @param repairEnter 号码验证失败需要补录的逻辑，执行撤回开奖时间后的订单
     */
    private void flushMultData(List<UserOrderPO> orders, String winNum, Long lotteryId, Long pCode, Integer prizeStatues, boolean repairEnter, Long drawPrizeTime) {

        List<UserOrderPO> currentCodeOrders = new ArrayList<>();
        List<UserOrderPO> chaseCodeOrders = new ArrayList<>();
        List<UserOrderPO> invalidOrders = new ArrayList<>();
        List<UserOrderPO> repealOrders = new ArrayList<>();  //撤单
        //此处过滤的订单，无效的订单，非当期的追单数据
        orders.forEach((order -> {
            try {
                order.setWinNumber(winNum);
                order.setModifyTime(System.currentTimeMillis());
                if (order.getOrderStatus() == OrderStatus.bet_success.code()) {
                    if (repairEnter) {
                        if (order.getBetTime() > drawPrizeTime) {
                            order.setOrderStatus(OrderStatus.system_withdrawals.code());
                            repealOrders.add(order);
                        } else {
                            if (order.getPcode().equals(pCode)) {
                                currentCodeOrders.add(order);
                            } else {
                                chaseCodeOrders.add(order);
                            }
                        }
                    } else {
                        if (order.getPcode().equals(pCode)) {
                            currentCodeOrders.add(order);
                        } else {
                            chaseCodeOrders.add(order);
                        }
                    }
                } else {
                    invalidOrders.add(order);
                }
            } catch (Exception e) {
                logger.error("--> order flush error, order:{}", JSON.toJSONString(order), e);
            }
        }));
        /*
         * 将追单数据和无效的订单写入数据库，通过子线程处理，需要根据pdate来分表
         *
         */
        if (chaseCodeOrders.size() > 0) {
            logger.info("--> proc chaseCodeOrders , size : {}", chaseCodeOrders.size());
            procDbOrder(chaseCodeOrders);
        }
        if (invalidOrders.size() > 0) {
            logger.info("--> proc invalidOrders, size : {}", invalidOrders.size());
            procMongoOrder(invalidOrders);
            procDbOrder(invalidOrders);
        }
        if (repairEnter && repealOrders.size() > 0) {
            logger.info("--> proc repealOrders, size : {}", repealOrders.size());
            procMongoOrder(repealOrders);
            procDbOrder(repealOrders);
        }
        logger.info("--> flush order , currentCodeOrders, size :{}", currentCodeOrders.size());

        /*
         * 当期开奖的订单
         */
        if (currentCodeOrders.size() > 0) {
            LotteryType lotteryType = LotteryType.parseType(lotteryId, currentCodeOrders.get(0).getPlayId());
            //etl获取中奖结果
            logger.info("--> calculate open prize result, lotteryId:{}, lotteryType :{}, winNum :{}", lotteryId, lotteryType, winNum);
            lotteryDrawHandle.getBatchBoundsInfoOfLottery(lotteryType, winNum, currentCodeOrders);

            List<UserOrderPO> winOrders = new ArrayList<>();
            List<UserOrderPO> noWinOrders = new ArrayList<>();
            //执行派彩
            for (UserOrderPO order : currentCodeOrders) {
                //判断是否中奖，添加中奖金额
                payoffUtil.calcWinPayoff(order, lotteryType);
                //设置有效投注额
                order.setValidBetAmount(order.getBetAmount());
                if (order.getOrderStatus() == OrderStatus.prize_win.code()) {
                    winOrders.add(order);
                } else {
                    noWinOrders.add(order);
                }
                //计算返点数据
                order.setReforwardPoint(payoffUtil.getReforwardPoint(order.getPlatInfoId(), order.getAcType(), order.getPlayId(), order.getLotteryId(), order.getBetAmount()));
                //中奖停追逻辑
                //todo 官方彩开启
//                chaseStop(order);
            }
            //中奖订单处理
            if (winOrders.size() > 0) {
                //1.调用风控，批量处理中奖的数据
                Map<String, RiskCheckResult> bingoOrderRisk = venusOutServerConsumer.disposeMulBingoOrder(JSON.toJSONString(winOrders));
                if (bingoOrderRisk != null && bingoOrderRisk.size() > 0) {
                    for (UserOrderPO order : winOrders) {
                        if (!bingoOrderRisk.get(order.getOrderId()).isValid()) {
                            //设置有效投注额
                            order.setValidBetAmount(0L);
                            order.setOrderStatus(OrderStatus.exception_deal.code());
                        }
                    }
                }
                //2.保存mongo处理后的订单
                userOrderService.batchSaveProcOrder(winOrders);
                //打印所有的订单id，为了查看为什么订单重复
                printProcOrderIds(lotteryId, pCode, winOrders, null);
                //3.修改forseti
                updateForsetiOrder(currentCodeOrders, lotteryId, pCode);
                logger.info("--> {}-彩种,{}-期,{}-玩法, 中奖数据:{}", lotteryId, pCode, currentCodeOrders.get(0).getPlayId(), JSON.toJSONString(winOrders));
            }
            //未中奖订单处理
            if (noWinOrders.size() > 0) {
                //1.调用风控批量处理未中奖的数据
                venusOutServerConsumer.disposeMulNotWinOrder(JSON.toJSONString(noWinOrders));
                //2.保存mongo处理后的订单
                userOrderService.batchSaveProcOrder(noWinOrders);
                //打印所有的订单id，为了查看为什么订单重复
                printProcOrderIds(lotteryId, pCode, noWinOrders, null);
                //3.修改forseti
                updateForsetiOrder(noWinOrders, lotteryId, pCode);
            }
            //开奖号码录入失败，重新执行开奖逻辑，系统撤单数据处理
            if (repairEnter && repealOrders.size() > 0) {
                //1.保存mongo处理后的订单
                userOrderService.batchSaveProcOrder(repealOrders);
                //打印所有的订单id，为了查看为什么订单重复
                printProcOrderIds(lotteryId, pCode, repealOrders, null);
                //2.修改forseti
                updateForsetiOrder(repealOrders, lotteryId, pCode);
            }
            //todo 开奖完的数据写入mongo,此处增加一个逻辑，如果是已经开过奖的数据，将order_id写入redis list中，由hermes根据id去拿取数据，
            if (prizeStatues.intValue() == DrawPrizeStatus.DRAW_SUCCESS.code()) {
                logger.info("--> lottery has drawed success, draw again, lottery:{}, pcode:{}", lotteryId, pCode);
                List<String> procOrderIds = new ArrayList<>();
                for (UserOrderPO winId : winOrders) {
                    procOrderIds.add(winId.getOrderId());
                }
                for (UserOrderPO noWinId : noWinOrders) {
                    procOrderIds.add(noWinId.getOrderId());
                }
                //打印所有的订单id，为了查看为什么订单重复
                printProcOrderIds(lotteryId, pCode, noWinOrders, null);
                //2.修改forseti
                updateForsetiOrder(repealOrders, lotteryId, pCode);
                if (procOrderIds.size() > 0) {
                    if (!userOrderService.batchSaveProcOrderCache(pCode, lotteryId, procOrderIds)) {
                        logger.error("--> user order batchSaveProcOrderCache error, pCode:{}, lotteryId:{}", pCode, lotteryId);
                    }
                }
            }
        }
    }

//    public static void main(String[] args) {
//        LotteryPourHandle lotteryPourHandle = new ShishicaiPourServiceImpl();
//        String winNumber ="28,40,18,39,12,35,36";
//        long playId=1141190;
//        long lotteryId=10;
//        String betContent="2,5";
//        LotteryType lotteryType = LotteryType.parseType(lotteryId, playId);
//        System.out.println(lotteryType);
//        //etl获取中奖结果
//        LotteryDrawHandle lotteryDrawHandle = new LotteryDrawServiceImpl();
//        List<UserOrderPO> list=new ArrayList<>();
//        UserOrderPO po = new UserOrderPO();
//        po.setPlayId(playId);
//        po.setWinNumber(winNumber);
//        po.setBetContent(betContent);
//        po.setLotteryId(lotteryId);
//        po.setMultiple(1);
//        po.setMoneyMode(String.valueOf(MoneyMode.YUAN.code()));
//        po.setOrderId(System.currentTimeMillis()+"");
//        po.setOrderStatus(OrderStatus.bet_success.code());
//        po.setBetContentProc(lotteryPourHandle.getLotteryListByType(lotteryId, playId, po.getBetContent()));
////        po.setIsZodiacYear(0);
//        list.add(po);
//        System.out.println(JSON.toJSONString(list));
//        lotteryDrawHandle.getBatchBoundsInfoOfLottery(lotteryType, winNumber, list);
//        System.out.println(JSON.toJSONString(list));
//
//    }

    public void flushOneData(UserOrderPO order, Map<String, UserOrderPO> chaseOrders, String winNum) {
        if (chaseOrders != null && chaseOrders.size() > 0) {
            if (chaseOrders.get(order.getOrderId()) != null) {
                return;
            }
        }
        flushOneData(order, winNum);
    }


    private void flushOneData(UserOrderPO order, String winNum) {
        order.setWinNumber(winNum);
        //调用开奖流程，修改订单的派奖状态
        lotteryDrawHandle.getBoundsInfoOfLottery(winNum, order);
        //执行派奖流程，执行完以后调用风控，风控处理完数据写入mongo，发送通知，开始拉取mongo数据，修改数据，写数据入库，涉及数据表
        //执行派彩
        //基本所有的订单是通过判断一等奖是否大于0 来判断是否中奖，个别如5/4/3和值 以及其他的几个玩法是通过遍历所有的中奖结果来判断是否中奖
        LotteryType lotteryType = LotteryType.parseType(order.getLotteryId(), order.getPlayId());
        //判断是否中奖，添加中奖金额
        payoffUtil.calcWinPayoff(order, lotteryType);
        if (order.getOrderStatus() == OrderStatus.prize_win.code()) {
            RiskCheckResult riskResult = venusOutServerConsumer.disposeBingoOrder(JSON.toJSONString(order));
            if (riskResult != null) {
                if (!riskResult.isValid()) {
                    order.setOrderStatus(OrderStatus.exception_deal.code());
                }
            }
        } else {
            venusOutServerConsumer.disposeNotWinOrder(JSON.toJSONString(order));
        }
        chaseStop(order);
        //todo 插入mongo
        userOrderService.save(order);
    }

    /**
     * 批量插入订单数据
     */
    private void procDbOrder(List<UserOrderPO> orderPOs) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = userOrderService.batchSaveDb(orderPOs);
                    logger.info("--> userOrderPo batchSaveDb ,size :{}, result : {}", orderPOs.size(), flag);
                } catch (Exception e) {
                    logger.error("--> procDbOrder error :: ", e);
                }
            }
        });
    }

    /**
     * 修改mongo中订单状态，适用于无效订单处理
     */
    private void procMongoOrder(List<UserOrderPO> pos) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (UserOrderPO po : pos) {
                        if (!userOrderService.updateMongoOrder(po)) {
                            logger.error("--> update mongo order failed, po :{}", JSON.toJSONString(po));
                        }
                    }
                } catch (Exception e) {
                    logger.error("--> procMongoOrder error: ",e);
                }
            }
        });
    }


    /**
     * 处理中奖停追逻辑
     */
    private void chaseStop(UserOrderPO order) {
        //todo 如果是追号并且是追中即停，需要走追中即停流程，撤单返款，注意处理返款流程
        if (order.getIfChase() == WhetherType.yes.code() && order.getChaseWinStop() == WhetherType.yes.code()) {
            if (order.getChaseSeq() < order.getChaseCount()) {
                BaseQueryVo vo = new BaseQueryVo();
                vo.setParentOrderId(order.getParentOrderId());
                vo.setPcode(order.getPcode());
                vo.setOrderStatus(OrderStatus.prize_win_stop_chase.code());
                vo.setWinNumber(order.getWinNumber());
                vo.setLotteryId(order.getLotteryId());
                Long pdate = venusUtil.getPdate(order.getLotteryId(), order.getPcode());
                vo.setPdate(pdate.intValue());
                if (userOrderService.updateChaseWinStopOrders(vo)) {
                    List<UserOrderPO> orderPOs = userOrderService.queryMongoChaseWinStopOrders(vo);
                    if (orderPOs.size() > 0) {
                        //将中奖停追的数据入库，同时修改redis缓存,forseti的订单
                        userOrderService.batchSaveProcOrder(orderPOs);
                        userOrderService.updateCacheOrders(orderPOs);
                        for (UserOrderPO po : orderPOs) {
                            //修改forseti的订单状态
                            updateForsetiOrder(po);
                        }
                    }
                } else {
                    List<UserOrderPO> orderPOs = userOrderService.queryMongoChaseWinStopOrders(vo);
                    if (orderPOs.size() > 0) {
                        //将中奖停追的数据入库，同时修改redis缓存
                        for (UserOrderPO po : orderPOs) {
                            po.setOrderStatus(OrderStatus.prize_win_stop_chase.code());
                            //修改forseti的订单状态
                            updateForsetiOrder(po);
                        }
                        userOrderService.batchSaveProcOrder(orderPOs);
                        userOrderService.updateCacheOrders(orderPOs);
                    }
                }
            }
        }
    }


    /**
     * 发送http消息，通知hermes派奖已经执行完毕，开始执行派彩后的数据
     *
     * @param pcode
     * @param procStatus
     */
    private void sendDrawSucNotice(long pcode, long lotteryId, int procStatus, int prizeStatus, boolean repairEnter) {
        if (StringUtils.isBlank(venusRefreshConfig.getHermesReqDrawHosts())) {
            logger.error("--> no hermesReqDrawHosts");
            return;
        }
        drawPrizeLockRedisService.setDrawSucReqHermesLock(lotteryId, pcode);
        String mapKey = pcode + "_" + lotteryId;
        if (DrawPrizeLockRedisService.checkReqHermesLockMap.get(mapKey) == null) {
            Runnable checkReqHermesLock = drawPrizeLockRedisService.getCheckReqHermesLock(lotteryId, pcode, procStatus, prizeStatus, repairEnter);
            DrawPrizeLockRedisService.checkReqHermesLockMap.put(mapKey, checkReqHermesLock);
            CHECK_REQ_HERMES_LOCK.execute(checkReqHermesLock);
        }
    }

    /**
     * 修改forseti中的订单状态
     *
     * @param po
     */
    private void updateForsetiOrder(UserOrderPO po) {
        List<UserOrderPO> pos = new ArrayList<>();
        pos.add(po);
        updateForsetiOrder(pos, po.getLotteryId(), po.getPcode());
    }

    /**
     * 修改forseti的订单的状态
     *
     * @param userOrderPOs
     * @param lotteryId
     * @param pCode
     */
    private void updateForsetiOrder(List<UserOrderPO> userOrderPOs, Long lotteryId, Long pCode) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserOrderStatusDTO statusDTO = new UserOrderStatusDTO();
                    statusDTO.setLotteryId(lotteryId);
                    statusDTO.setPcode(pCode);
                    List<MemberStatusOrderDTO> orderDTOs = new ArrayList<>();
                    for (UserOrderPO po : userOrderPOs) {
                        MemberStatusOrderDTO orderDTO = new MemberStatusOrderDTO();
                        orderDTO.setMemberId(po.getMemberId());
                        orderDTO.setOrderStatus(po.getOrderStatus());
                        orderDTO.setParentOrderId(po.getParentOrderId());
                        List<String> orderIds = new ArrayList<>();
                        orderIds.add(po.getOrderId());
                        orderDTO.setOrderIds(orderIds);
                        orderDTOs.add(orderDTO);
                    }
                    statusDTO.setList(orderDTOs);
                    venusOutServerConsumer.updateOrderStatus(statusDTO);
                } catch (Exception e) {
                    logger.error("--> thread updateForsetiOrder error, thread:{}, lotteryId:{}, pcode:{}", Thread.currentThread().getName(), lotteryId, pCode);
                }
            }
        });
    }

    /**
     * 直接获取订单派彩结果，测试使用，不执行其他流程
     *
     * @param orders
     * @param lotteryType
     * @return
     */
    public Map<String, Long> manualPayoff(List<UserOrderPayoffVo> orders, LotteryType lotteryType, Long playId, Long lotteryId, Long platInfoId, Integer acType) {
        Map<String, Long> payoffResult = new HashMap<>();
        for (UserOrderPayoffVo vo : orders) {
            UserOrderPO po = new UserOrderPO();
            po.setMultiple(vo.getMultiple());
            po.setPlayId(playId);
            po.setLotteryId(lotteryId);
            po.setPlatInfoId(platInfoId);
            po.setMoneyMode(vo.getMoneyMode());
            po.setFirstPrizeNum(vo.getFirstPrizeNum());
            po.setSecondPrizeNum(vo.getSecondPrizeNum());
            po.setThirdPrizeNum(vo.getThirdPrizeNum());
            po.setForthPrizeNum(vo.getForthPrizeNum());
            po.setFifthPrizeNum(vo.getFifthPrizeNum());
            po.setIsTied(vo.getIsTied());
            po.setAcType(acType);
            po.setOrderStatus(OrderStatus.bet_success.code());
            po.setIsZodiacYear(vo.getIsZodiacYear());
            payoffUtil.calcWinPayoff(po, lotteryType);
            payoffResult.put(vo.getOrderId(), po.getPayoff());
        }
        return payoffResult;
    }

    /**
     * 打印开奖完成的订单id，为了测试订单重复问题
     *
     * @param lotteryId
     * @param pcode
     * @param pos
     * @param ids
     */
    private void printProcOrderIds(long lotteryId, long pcode, List<UserOrderPO> pos, List<String> ids) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (pos != null) {
                        List<String> orderIds = new ArrayList<>();
                        for (UserOrderPO po : pos) {
                            orderIds.add(po.getOrderId());
                        }
                        logger.info("--> print proc pos orderIds, lotteryId:{}, pcode:{}, ids:{}", lotteryId, pcode, JSON.toJSONString(orderIds));
                    }
                    if (ids != null && ids.size() > 0) {
                        logger.info("--> print proc ids orderIds, lotteryId:{}, pcode:{}, ids:{}", lotteryId, pcode, JSON.toJSONString(ids));
                    }
                } catch (Exception e) {
                    logger.error("--> pring proc ids orderIds error, lotteryId:{}, pcode:{}", lotteryId, pcode);
                }
            }
        });

    }



    /**
     * 修复未执行开奖流程的订单
     * @param pCode
     * @param lotteryId
     */
    public boolean repairNoDrawOrders(Long pCode, String code, Integer pdate, Long lotteryId, Integer page, Integer size) {
        BaseQueryVo queryVo = new BaseQueryVo();
        queryVo.setLotteryId(lotteryId);
        queryVo.setPcode(pCode);
        queryVo.setIfChase(WhetherType.no.code());
        queryVo.setPdate(pdate);
        queryVo.setOrderStatus(OrderStatus.bet_success.code());
        queryVo.setPage(page, size);
        List<UserOrderPO> orderPOs = userOrderService.queryMongoOrders(queryVo);
        List<UserOrderPO> noProcOrders = new ArrayList<>();
        List<String> noProcOrderIds = new ArrayList<>();
        List<String> incompleteOrders=new ArrayList<>();
        if (orderPOs != null && orderPOs.size() > 0) {
            List<String> orderIds=new ArrayList<>();
            for (UserOrderPO po : orderPOs) {
                po.setBetContentProc(lotteryPourHandle.getLotteryListByType(po.getLotteryId(), po.getPlayId(), po.getBetContent()));
                orderIds.add(po.getOrderId());
            }
            queryVo.setOrderStatus(null);
            queryVo.setOrderIds(orderIds);
            List<UserOrderPO> procOrders = userOrderService.queryMongoProcOrders(queryVo);
            if (procOrders != null && procOrders.size() > 0) {
                for (UserOrderPO po : procOrders) {
                    boolean exist = false;
                    for (UserOrderPO po1 : orderPOs) {
                        if (po.getOrderId().equals(po1.getOrderId())) {
                            incompleteOrders.add(po1.getOrderId());
                            exist = true;
                        }
                    }
                    if (!exist) {
                        noProcOrders.add(po);
                        noProcOrderIds.add(po.getOrderId());
                    }
                }
            } else {
                noProcOrders = orderPOs;
                for (UserOrderPO po : orderPOs) {
                    noProcOrderIds.add(po.getOrderId());
                }
            }
        }
        if (noProcOrders.size() > 0) {
            List<PrizeNumberPO> prizeNumberPOs = venusOutServerConsumer.getListByIssue(pCode, code, lotteryId, null, null);
            if (prizeNumberPOs == null || prizeNumberPOs.size() == 0) {
                logger.error("--> analysis get prizeNumberPOs is null ");
                return false;
            }
            String winNum = prizeNumberPOs.get(0).getNumbers();
            if (StringUtils.isEmpty(winNum)) {
                logger.error("--> there isn't winNum, winNum : {}", winNum);
                return false;
            }
            flushMultData(noProcOrders, winNum, lotteryId, pCode, DrawPrizeStatus.UN_DRAW.code(), false, null);
            //订单处理完调用风控
            //todo 异常处理的模块是否调用风控待确定
//            venusOutServerConsumer.disposeForOnePcodeEnd(OrderUtils.getPdate(pCode).intValue(), lotteryId, pCode);
        }
        return venusOutServerConsumer.repairNoDrawOrders(pCode, pdate, lotteryId, noProcOrderIds, incompleteOrders);
    }

}
