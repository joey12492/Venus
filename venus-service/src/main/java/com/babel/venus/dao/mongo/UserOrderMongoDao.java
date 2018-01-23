package com.babel.venus.dao.mongo;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.dao.mongo.base.BaseMongoDAOImpl;

/**
 * User: joey
 * Date: 2017/9/5
 * Time: 1:24
 */
@Component
public class UserOrderMongoDao extends BaseMongoDAOImpl<UserOrderPO>{

    @Resource
    private MongoTemplate mongoTemplate1;

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate1;
    }

}
