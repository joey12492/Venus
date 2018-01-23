package com.babel.venus;

import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.storage.mongo.UserOrderMongoService;
import com.babel.venus.vo.BaseQueryVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * User: joey
 * Date: 2017/10/19
 * Time: 10:37
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VenusApp.class)
public class MuliMongoDatabaseTest {

    @Resource
    private MongoTemplate mongoTemplate1;
    @Resource
    private MongoTemplate mongoTemplate2;
    @Resource
    private UserOrderMongoService userOrderMongoService;

    @Test
    public void TestSave() {
        BaseQueryVo vo = new BaseQueryVo();
        vo.setStartTime(System.currentTimeMillis());
        vo.setEndTime(System.currentTimeMillis());
        Query query = new Query();
        System.out.println(mongoTemplate1.findOne(query, UserOrderPO.class));

    }
}
