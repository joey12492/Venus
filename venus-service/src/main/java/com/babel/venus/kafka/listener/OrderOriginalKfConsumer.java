//package com.babel.venus.kafka.listener;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.babel.venus.assemble.UserOrderAssemService;
//import com.babel.venus.po.UserOrder;
//import com.babel.venus.service.UserOrderService;
//import io.swagger.annotations.Api;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.Optional;
//
///**
// * User: joey
// * Date: 2017/9/6
// * Time: 16:03
// * 消费彩票订单kafka数据，插入mongo
// */
//@Service
//public class OrderOriginalKfConsumer {
//
//    private Logger logger = LoggerFactory.getLogger(OrderOriginalKfConsumer.class);
//
//    @Resource
//    private UserOrderAssemService userOrderAssemService;
//
//    @Resource
//    private UserOrderService userOrderService;
//
//    @KafkaListener(topics = {"lottery_order"}, group = "lottery_group")
//    public void lotteryOrderListen(ConsumerRecord<?, ?> record) {
//        procData(record);
//    }
//
//    /**
//     * @param record
//     */
//    private void procData(ConsumerRecord<?, ?> record) {
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                UserOrder order = JSON.parseObject(record.value().toString(), UserOrder.class);
//                if (!userOrderService.save(order)) {
//                    logger.error("save user order error : " + JSON.toJSONString(order));
//                }
//            }
//        } catch (Exception e) {
//            logger.error("", e);
//        }
//    }
//
//
//}
