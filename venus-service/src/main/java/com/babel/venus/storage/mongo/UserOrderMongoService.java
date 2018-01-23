package com.babel.venus.storage.mongo;

import com.alibaba.fastjson.JSON;
import com.babel.common.lottery.OrderStatus;
import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.constants.MongoCollectionFlag;
import com.babel.venus.constants.MongoCollections;
import com.babel.venus.dao.mongo.UserOrderMongoDao;
import com.babel.venus.util.VenusUtil;
import com.babel.venus.vo.BaseQueryVo;
import com.babel.venus.vo.UserOrderVo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: joey
 * Date: 2017/9/5
 * Time: 1:22
 */
@Service
public class UserOrderMongoService {

    private static final Logger logger = LoggerFactory.getLogger(UserOrderMongoService.class);

    @Resource
    private UserOrderMongoDao userOrderMongoDao;
    @Resource
    private VenusUtil venusUtil;

    /**
     * 保存订单
     *
     * @param order
     * @return
     */
    public boolean saveOrder(UserOrderPO order) {
        try {
            String collName = MongoCollectionFlag.getDateName(MongoCollectionFlag.PROC_SUC.collName(MongoCollections.UserOrderPO.name()), String.valueOf(venusUtil.getPdate(order.getLotteryId(), order.getPcode())));
            return userOrderMongoDao.save(order, collName) != null;
        } catch (Exception e) {
            logger.error("--> userOrderMongo save order error : " + JSON.toJSONString(order), e);
        }
        return false;
    }

    /**
     * 保存处理过的订单
     *
     * @param orders
     * @return
     */
    public boolean batchSaveProcOrder(List<UserOrderPO> orders) {
        try {
            String collName =  MongoCollectionFlag.getDateName(MongoCollectionFlag.PROC_SUC.collName(MongoCollections.UserOrderPO.name()), String.valueOf(venusUtil.getPdate(orders.get(0).getLotteryId(), orders.get(0).getPcode())));
            return userOrderMongoDao.batchSave(orders, collName);
        } catch (Exception e) {
            logger.error("--> userOrderMongo save order error , orders.size :" + orders.size(), e);
        }
        return false;
    }

    /**
     * 查询开奖后的订单id列表
     * @param vo
     * @return
     */
    public List<String> queryNoDrawOrderIds(BaseQueryVo vo, Pageable pageable) {
        try {
            if (pageable == null) {
                return null;
            }
            List<String> orderIds = new ArrayList<>();
            BasicDBObject condition = new BasicDBObject();
            condition.append("createTime", new BasicDBObject("$gte", vo.getStartTime()).append("$lt", vo.getEndTime()));

            BasicDBObject keys = new BasicDBObject();
            keys.append("orderId", 1);
            String collName = MongoCollectionFlag.getDateName(MongoCollectionFlag.PROC_SUC.collName(MongoCollections.UserOrderPO.name()), vo.getPdate()+"");
            DBCollection dbCollection = userOrderMongoDao.getMongoTemplate().getCollection(collName);
            Iterator<DBObject> iterator = dbCollection.find(condition, keys).skip(pageable.getOffset()).limit(pageable.getPageSize());
            while (iterator.hasNext()) {
                String orderId = (String) iterator.next().get("orderId");
                orderIds.add(orderId);
            }
            return orderIds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取mongo中处理过的订单列表
     * @param vo
     * @return
     */
    public List<UserOrderPO> queryProcOrders(BaseQueryVo vo) {
        try {
            Query query = new Query();
            if (vo.getStartTime() != null && vo.getEndTime() != null) {
                query.addCriteria(new Criteria("createTime").gte(vo.getStartTime()).lt(vo.getEndTime()));
            }
            if (vo.getOrderIds() != null) {
                query.addCriteria(new Criteria("orderId").in(vo.getOrderIds()));
            }
            if (vo.getLotteryId() != null) {
                query.addCriteria(new Criteria("lotteryId").is(vo.getLotteryId()));
            }
            String collName = MongoCollectionFlag.getDateName(MongoCollectionFlag.PROC_SUC.collName(MongoCollections.UserOrderPO.name()), vo.getPdate()+"");
            return userOrderMongoDao.find(query, collName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取mongo中的原始订单列表
     * @param vo
     * @return
     */
    public List<UserOrderPO> queryOrders(BaseQueryVo vo) {
        try {
            Query query = new Query();
            if (vo.getPcode() != null) {
                query.addCriteria(new Criteria("pcode").is(vo.getPcode()));
            }
            if (vo.getOrderStatus() != null) {
                query.addCriteria(new Criteria("orderStatus").is(vo.getOrderStatus()));
            }
            if (vo.getIfChase() != null) {
                query.addCriteria(new Criteria("ifChase").is(vo.getIfChase()));
            }
            if (vo.getChaseCount() != null) {
                query.addCriteria(new Criteria("chaseCount").gt(vo.getChaseCount()));
            }
            if (vo.getLotteryId() != null) {
                query.addCriteria(new Criteria("lotteryId").is(vo.getLotteryId()));
            }
            if (vo.getPage() != null) {
                query.skip(vo.getStart()).limit(vo.getSize());
            }
            String collName = MongoCollectionFlag.getDateName(MongoCollections.UserOrderPO.name(), vo.getPdate()+"");
            return userOrderMongoDao.find(query, collName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取mongo中的中奖即停的后续订单
     * @param vo
     * @return
     */
    public List<UserOrderPO> queryMongoChaseWinStopOrders(BaseQueryVo vo) {
        try {
            Query query = new Query();
            query.addCriteria(new Criteria("pcode").gt(vo.getPcode()));
            query.addCriteria(new Criteria("orderParentId").is(vo.getParentOrderId()));
            String collName = MongoCollectionFlag.getDateName(MongoCollections.UserOrderPO.name(), vo.getPdate()+"");
            return userOrderMongoDao.find(query, collName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 修改mongo中的中奖即停的后续订单
     * @param vo
     * @return
     */
    public boolean updateChaseWinStopOrders(BaseQueryVo vo) {
        try {
            Query query = new Query();
            query.addCriteria(new Criteria("pcode").gt(vo.getPcode()));
            query.addCriteria(new Criteria("orderParentId").is(vo.getParentOrderId()));
            query.addCriteria(new Criteria("orderStatus").is(OrderStatus.bet_success.code()));
            Update update = new Update();
            update.set("orderStatus", vo.getOrderStatus());
            update.set("winNumber", vo.getWinNumber());
            String collName = MongoCollectionFlag.getDateName(MongoCollections.UserOrderPO.name(), vo.getPdate()+"");
            return userOrderMongoDao.update(query, update, collName)!= null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrder(UserOrderPO po) {
        try {
            Query query = new Query();
            query.addCriteria(new Criteria("orderId").gt(po.getOrderId()));
            Update update = new Update();
            update.set("orderStatus",po.getOrderStatus());
            String collName = MongoCollectionFlag.getDateName(MongoCollections.UserOrderPO.name(), po.getPdate()+"");
            return userOrderMongoDao.update(query, update, collName)!= null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
