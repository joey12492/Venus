package com.babel.venus.util;

import com.babel.account.po.PlatLottery;
import com.babel.ares.model.OddsItemPO;
import com.babel.common.lottery.*;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.constants.VenusConstants;
import com.babel.venus.service.feign.VenusOutServerConsumer;
import com.babel.venus.storage.local.LocalCacheManage;
import com.bc.lottery.entity.LotteryMark6DoubleType;
import com.bc.lottery.entity.LotteryType;
import com.bc.lottery.entity.ShishicaiType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.bc.lottery.entity.LotteryMark6DoubleType.*;
import static com.bc.lottery.entity.ShishicaiType.*;

/**
 * User: joey
 * Date: 2018/1/1
 * Time: 14:21
 */
@Component
public class PayoffUtil {

    private static final Logger logger = LoggerFactory.getLogger(PayoffUtil.class);

    @Resource
    private VenusOutServerConsumer venusOutServerConsumer;

    /**
     * 处理订单中奖状态，并设置派奖金额
     *
     * @return
     */
    public void calcWinPayoff(UserOrderPO order, LotteryType lotteryType) {
        //时时彩
        if (lotteryType instanceof ShishicaiType) {
            sscPayoff(order);
        }//六合彩
        else if (lotteryType instanceof LotteryMark6DoubleType) {
            lhcPayoff(order);
        }//其他彩种派彩
        else {
            otherPayoff(order);
        }
    }

    /**
     * 时时彩，也是官方彩派彩流程
     *
     * @param order
     */
    private void sscPayoff(UserOrderPO order) {
        if (order.getIsTied() == IsTied.yes.getCode()) {
            order.setOrderStatus(OrderStatus.tied.code());
            order.setPayoff(order.getBetAmount());
            return;
        }
        ShishicaiType playType = ShishicaiType.parse(order.getPlayId());
        switch (playType) {
            case QIAN_SAN_ZU_XUAN_HE_ZHI://中奖：1、组三和值321，2、组六和值322
            case QIAN_SAN_HUN_HE_ZU_XUAN:
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), QIAN_SAN_ZU_SAN.value(), order.getLotteryId()) * order.getFirstPrizeNum());
                } else if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), QIAN_SAN_ZU_LIU.value(), order.getLotteryId()) * order.getSecondPrizeNum());
                }
                break;
            case ZHONG_SAN_ZU_XUAN_HE_ZHI://中奖:1、组三和值421，2、组六和值422
            case ZHONG_SAN_HUN_HE_ZU_XUAN:
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), ZHONG_SAN_ZU_SAN.value(), order.getLotteryId()) * order.getFirstPrizeNum());
                } else if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), ZHONG_SAN_ZU_LIU.value(), order.getLotteryId()) * order.getSecondPrizeNum());
                }
                break;
            case HOU_SAN_ZU_XUAN_HE_ZHI://中奖：1、组三和值521，2.组六和值522
            case HOU_SAN_HUN_HE_ZU_XUAN:
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_SAN_ZU_SAN.value(), order.getLotteryId()) * order.getFirstPrizeNum());
                } else if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_SAN_ZU_LIU.value(), order.getLotteryId()) * order.getSecondPrizeNum());
                }
                break;
            case WU_XING_ZHI_XUAN_ZU_HE:
                long payoff_5 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), WU_XING_ZHI_XUAN_FU_SHI.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_XING_ZHI_XUAN_FU_SHI.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                if (order.getThirdPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_SAN_FU_SHI.value(), order.getLotteryId()) * order.getThirdPrizeNum();
                }
                if (order.getForthPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_ER_ZHI_XUAN_FU_SHI.value(), order.getLotteryId()) * order.getForthPrizeNum();
                }
                if (order.getFifthPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), YI_XING_DING_WEI_DAN.value(), order.getLotteryId()) * order.getFifthPrizeNum();
                }
                order.setPayoff(payoff_5);
                break;
            case SI_XING_ZHI_XUAN_ZU_HE:
                long payoff_4 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_XING_ZHI_XUAN_FU_SHI.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_SAN_FU_SHI.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                if (order.getThirdPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), HOU_ER_ZHI_XUAN_FU_SHI.value(), order.getLotteryId()) * order.getThirdPrizeNum();
                }
                if (order.getForthPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), YI_XING_DING_WEI_DAN.value(), order.getLotteryId()) * order.getForthPrizeNum();
                }
                order.setPayoff(payoff_4);
                break;
            case YI_XING_DING_WEI_DAN:
                long payoff_1 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    payoff_1 = getPayoff(order.getPlatInfoId(), order.getAcType(), YI_XING_DING_WEI_DAN.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                order.setPayoff(payoff_1);
                break;
            default:
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    order.setPayoff(getPayoff(order.getPlatInfoId(), order.getAcType(), order.getPlayId(), order.getLotteryId()) * order.getFirstPrizeNum());
                }
                break;
        }


        if (order.getOrderStatus() == OrderStatus.bet_success.code()) {
            order.setOrderStatus(OrderStatus.prize_no_win.code());
            order.setPayoff(0L);
        } else if (order.getOrderStatus() == OrderStatus.prize_win.code()) {
            logger.info("--> win order firstPrizeNum : " + order.getFirstPrizeNum());
            //*乘下注倍数
            order.setPayoff(order.getPayoff() * order.getMultiple());
            if (order.getMoneyMode().equals(MoneyMode.FEN.code().toString())) {
                order.setPayoff(order.getPayoff() / 100 / 100);   //赔率配置为毫，插入数据库为分，所以此处再除以100
            } else if (order.getMoneyMode().equals(MoneyMode.JIAO.code().toString())) {
                order.setPayoff(order.getPayoff() / 10 / 100);
            } else {
                order.setPayoff(order.getPayoff() / 100);
            }
            if (order.getPayoff() == 0) {
                logger.error("--> lottery has the winning, but payoff is 0, orderId:{}", order.getOrderId());
            }

        }
    }

    /**
     * 六合彩派彩流程
     */
    private void lhcPayoff(UserOrderPO order) {
        LotteryMark6DoubleType playType = LotteryMark6DoubleType.parse(order.getPlayId());
        switch (playType) {
            case ZHENG_XIAO:
            case ZHENG_XIAO_SHU:
            case ZHENG_XIAO_NIU:
            case ZHENG_XIAO_HU:
            case ZHENG_XIAO_TU:
            case ZHENG_XIAO_LONG:
            case ZHENG_XIAO_SHE:
            case ZHENG_XIAO_MA:
            case ZHENG_XIAO_YANG:
            case ZHENG_XIAO_HOU:
            case ZHENG_XIAO_JI:
            case ZHENG_XIAO_GOU:
            case ZHENG_XIAO_ZHU:
                if (order.getIsZodiacYear() == WhetherType.yes.code()) {
                    long payoff_5 = 0;
                    if (order.getFirstPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_LONG.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                    }
                    if (order.getSecondPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_TU.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                    }
                    if (order.getThirdPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_HU.value(), order.getLotteryId()) * order.getThirdPrizeNum();
                    }
                    if (order.getForthPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_NIU.value(), order.getLotteryId()) * order.getForthPrizeNum();
                    }
                    if (order.getFifthPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_5 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_SHU.value(), order.getLotteryId()) * order.getFifthPrizeNum();
                    }
                    order.setPayoff(payoff_5);
                } else {
                    long payoff_4 = 0;
                    if (order.getFirstPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_HOU.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                    }
                    if (order.getSecondPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_YANG.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                    }
                    if (order.getThirdPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_MA.value(), order.getLotteryId()) * order.getThirdPrizeNum();
                    }
                    if (order.getForthPrizeNum() > 0) {
                        order.setOrderStatus(OrderStatus.prize_win.code());
                        payoff_4 += getPayoff(order.getPlatInfoId(), order.getAcType(), ZHENG_XIAO_SHE.value(), order.getLotteryId()) * order.getForthPrizeNum();
                    }
                    order.setPayoff(payoff_4);
                }
                break;
            case LIAN_MA_SAN_ZHONG_ER:
            case LIAN_MA_SAN_ZHONG_SAN:
                long payoff_3 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    payoff_3 += getPayoff(order.getPlatInfoId(), order.getAcType(), LIAN_MA_SAN_ZHONG_SAN.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    payoff_3 += getPayoff(order.getPlatInfoId(), order.getAcType(), LIAN_MA_SAN_ZHONG_ER.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(payoff_3);
                break;
            case LIAN_MA_ER_ZHONG_ER:
            case LIAN_MA_ER_ZHONG_TE:
                long payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), LIAN_MA_ER_ZHONG_TE.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), LIAN_MA_ER_ZHONG_ER.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(payoff_2);
                break;
            // 连尾
            case ER_LIAN_WEI:
            case ER_LIAN_WEI_0:
            case ER_LIAN_WEI_FEI_0:
                long er_lw_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    er_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), ER_LIAN_WEI_0.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    er_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), ER_LIAN_WEI_FEI_0.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(er_lw_payoff_2);
                break;
            case SAN_LIAN_WEI:
            case SAN_LIAN_WEI_0:
            case SAN_LIAN_WEI_FEI_0:
                long san_lw_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    san_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SAN_LIAN_WEI_0.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    san_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SAN_LIAN_WEI_FEI_0.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(san_lw_payoff_2);
                break;
            case SI_LIAN_WEI:
            case SI_LIAN_WEI_0:
            case SI_LIAN_WEI_FEI_0:
                long si_lw_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    si_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_LIAN_WEI_0.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    si_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_LIAN_WEI_FEI_0.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(si_lw_payoff_2);
                break;
            case WU_LIAN_WEI:
            case WU_LIAN_WEI_0:
            case WU_LIAN_WEI_FEI_0:
                long wu_lw_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    wu_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), WU_LIAN_WEI_0.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    wu_lw_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), WU_LIAN_WEI_FEI_0.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(wu_lw_payoff_2);
                break;


            // 连肖连尾
            case ER_LIAN_XIAO:
            case ER_LIAN_XIAO_BEN_MING:
            case ER_LIAN_XIAO_FEI_BEN_MING:
                long er_lx_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    er_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), ER_LIAN_XIAO_FEI_BEN_MING.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    er_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), ER_LIAN_XIAO_BEN_MING.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(er_lx_payoff_2);
                break;

            case SAN_LIAN_XIAO:
            case SAN_LIAN_XIAO_BEN_MING:
            case SAN_LIAN_XIAO_FEI_BEN_MING:
                long san_lx_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    san_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SAN_LIAN_XIAO_FEI_BEN_MING.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    san_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SAN_LIAN_XIAO_BEN_MING.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(san_lx_payoff_2);
                break;

            case SI_LIAN_XIAO:
            case SI_LIAN_XIAO_BEN_MING:
            case SI_LIAN_XIAO_FEI_BEN_MING:
                long si_lx_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    si_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_LIAN_XIAO_FEI_BEN_MING.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    si_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), SI_LIAN_XIAO_BEN_MING.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(si_lx_payoff_2);
                break;

            case WU_LIAN_XIAO:
            case WU_LIAN_XIAO_BEN_MING:
            case WU_LIAN_XIAO_FEI_BEN_MING:
                long wu_lx_payoff_2 = 0;
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中一等奖
                    wu_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), WU_LIAN_XIAO_FEI_BEN_MING.value(), order.getLotteryId()) * order.getFirstPrizeNum();
                }
                if (order.getSecondPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    //中二等奖
                    wu_lx_payoff_2 += getPayoff(order.getPlatInfoId(), order.getAcType(), WU_LIAN_XIAO_BEN_MING.value(), order.getLotteryId()) * order.getSecondPrizeNum();
                }
                order.setPayoff(wu_lx_payoff_2);
                break;
            default:
                if (order.getFirstPrizeNum() > 0) {
                    order.setOrderStatus(OrderStatus.prize_win.code());
                    long payoff = getPayoff(order.getPlatInfoId(), order.getAcType(), order.getPlayId(), order.getLotteryId()) * order.getFirstPrizeNum();
                    order.setPayoff(payoff);
                }

        }
        if (order.getOrderStatus() == OrderStatus.bet_success.code()) {
            order.setOrderStatus(OrderStatus.prize_no_win.code());
            order.setPayoff(0L);
        } else if (order.getOrderStatus() == OrderStatus.prize_win.code()) {
            //*乘下注倍数
            order.setPayoff(order.getPayoff() * order.getMultiple());
            if (order.getMoneyMode().equals(MoneyMode.FEN.code().toString())) {
                order.setPayoff(order.getPayoff() / 100 / 100);   //赔率配置为毫，插入数据库为分，所以此处再除以100
            } else if (order.getMoneyMode().equals(MoneyMode.JIAO.code().toString())) {
                order.setPayoff(order.getPayoff() / 10 / 100);
            } else {
                order.setPayoff(order.getPayoff() / 100);
            }
            if (order.getPayoff() == 0) {
                logger.error("--> lottery has the winning, but payoff is 0, orderId:{}", order.getOrderId());
            }

        }
    }

    /**
     * 其他彩种派彩流程，双面彩
     *
     * @param order
     */
    private void otherPayoff(UserOrderPO order) {
        if (order.getIsTied() == IsTied.yes.getCode()) {
            order.setOrderStatus(OrderStatus.tied.code());
            order.setPayoff(order.getBetAmount());
            return;
        }
        if (order.getFirstPrizeNum() > 0) {
            order.setOrderStatus(OrderStatus.prize_win.code());
            long payoff = getPayoff(order.getPlatInfoId(), order.getAcType(), order.getPlayId(), order.getLotteryId()) * order.getFirstPrizeNum();
            //*乘下注倍数
            payoff = payoff * order.getMultiple();
            if (order.getMoneyMode().equals(MoneyMode.FEN.code().toString())) {
                payoff = payoff / 100 / 100;   //赔率配置为毫，插入数据库为分，所以此处再除以100, *乘以下注倍数
            } else if (order.getMoneyMode().equals(MoneyMode.JIAO.code().toString())) {
                payoff = payoff / 10 / 100;
            } else {
                payoff = payoff / 100;
            }
            order.setPayoff(payoff);
            if (order.getPayoff() == 0) {
                logger.error("--> lottery has the winning, but payoff is 0, orderId:{}", order.getOrderId());
            }
        } else {
            order.setOrderStatus(OrderStatus.prize_no_win.code());
            order.setPayoff(0L);
        }
    }

    private Long getPayoff(Long payoffGroupId, Long playId) {
        return LocalCacheManage.playPayoffMapCache.getIfPresent(payoffGroupId + VenusConstants.SPLIT_UNDERLINE + playId);
    }

    private Integer getReforwardPoint(Long payoffGroupId, Long playId) {
        return LocalCacheManage.playReforwardMapCache.getIfPresent(payoffGroupId + VenusConstants.SPLIT_UNDERLINE + playId);
    }
    /**
     * 获取中奖后派彩金额
     */
    //todo 此处的获取派奖流程需要完善，如果没有获取到派奖数据是否应该记录
    private long getPayoff(Long platInfoId, Integer acType, Long playId, Long lotteryId) {
        Long realPlatInfoId =getRealPlatId(platInfoId, acType);
        Long payoffGroupId = getPayoffGroup(realPlatInfoId, lotteryId);
        Long payoff = getPayoff(payoffGroupId, playId);
        return payoff == null ? 0L : payoff;
    }

    /**
     * 获取返点数据
     * @param platInfoId
     * @param acType
     * @param playId
     * @param lotteryId
     * @param betAmount
     * @return
     */
    public int getReforwardPoint(Long platInfoId, Integer acType, Long playId, Long lotteryId, Long betAmount) {
        Long realPlatInfoId =getRealPlatId(platInfoId, acType);
        Long payoffGroupId = getPayoffGroup(realPlatInfoId, lotteryId);
        Integer reforwardPoint = getReforwardPoint(payoffGroupId, playId);
        if (reforwardPoint == null) {
            return 0;
        } else {
            return (int) (betAmount * reforwardPoint /1000000);
        }
    }

    private Long getRealPlatId(Long platInfoId, Integer acType) {
        Long realPlatInfoId = platInfoId;
        if (acType == AcType.trial.getCode()) {
            realPlatInfoId = LocalCacheManage.acPlatIdCache.getIfPresent(platInfoId);
            if (realPlatInfoId == null) {
                realPlatInfoId = venusOutServerConsumer.getPlatInfoByTrialId(platInfoId);
                if (realPlatInfoId == null || realPlatInfoId == 0) {
                    logger.error("--> trial plat get real plat failed, platInfoId :{}", platInfoId);
                    return 0L;
                } else {
                    LocalCacheManage.acPlatIdCache.put(platInfoId, realPlatInfoId);
                }
            }
        }
        return realPlatInfoId;
    }

    private long getPayoffGroup(Long platInfoId, Long lotteryId) {
        Long payoffGroup = LocalCacheManage.platPayoffGroupMapCache.getIfPresent(platInfoId + VenusConstants.SPLIT_UNDERLINE + lotteryId);
        if (payoffGroup == null) {
            initPayOffMap(platInfoId, lotteryId);
            payoffGroup = LocalCacheManage.platPayoffGroupMapCache.getIfPresent(platInfoId + VenusConstants.SPLIT_UNDERLINE + lotteryId);
        }
        if (payoffGroup == null) {
            logger.error("--> get local cache payoffGroup is null, platInfoId:{}, lotteryId:{}", platInfoId, lotteryId);
            return 0;
        }
        return payoffGroup;
    }

    /**
     * 初始化派奖金额
     */
    private void initPayOffMap(Long platInfoId, Long lotteryId) {
        //此处查询所有的平台商的赔率信息
        List<PlatLottery> lotteries = venusOutServerConsumer.lotteryList(platInfoId, lotteryId);
        logger.info("--> init payoff map, lotteries:"+ lotteries);
        if (lotteries == null || lotteries.size() == 0) {
            logger.error("--> plat lotteries is null ,platPayoffGroupMap is null");
            return;
        }
        for (PlatLottery lottery : lotteries) {
            LocalCacheManage.platPayoffGroupMapCache.put(lottery.getPlatInfoId() + VenusConstants.SPLIT_UNDERLINE + lottery.getLotteryId(), lottery.getPayoffGroup());
            List<OddsItemPO> _itemPOList = venusOutServerConsumer.queryOddsItemsList(null, lottery.getLotteryId(), lottery.getPayoffGroup());
            if (_itemPOList != null && _itemPOList.size() > 0) {
                for (OddsItemPO po : _itemPOList) {
                    LocalCacheManage.playPayoffMapCache.put(lottery.getPayoffGroup() + VenusConstants.SPLIT_UNDERLINE + po.getPlayId(), po.getPayoff());
                    if (po.getReforwardPoint() != null && po.getReforwardPoint() != 0) {
                        LocalCacheManage.playReforwardMapCache.put(lottery.getPayoffGroup() + VenusConstants.SPLIT_UNDERLINE + po.getPlayId(), po.getReforwardPoint());
                    }
                }
                logger.info("--> LocalCacheManage.playPayoffMapCache : "+LocalCacheManage.playPayoffMapCache.asMap());
                logger.info("--> LocalCacheManage.playReforwardMapCache : "+LocalCacheManage.playReforwardMapCache.asMap());
            } else {
                logger.warn("--> req config odds is null, failed, lotteryId:{}, payoffGroup:{}", lottery.getLotteryId(), lottery.getPayoffGroup());
            }
        }
    }



}
