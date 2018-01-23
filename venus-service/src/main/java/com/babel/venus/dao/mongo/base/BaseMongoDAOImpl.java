package com.babel.venus.dao.mongo.base;

import com.babel.venus.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;


public abstract class BaseMongoDAOImpl<T> implements BaseMongoDAO<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseMongoDAOImpl.class);

    @Override
    public List<T> find(Query query) {
        return getMongoTemplate().find(query, this.getEntityClass());
    }

    public List<T> find(Query query, String collectionName) {
        return getMongoTemplate().find(query, this.getEntityClass(), collectionName);
    }

    @Override
    public T findOne(Query query) {
        return getMongoTemplate().findOne(query, this.getEntityClass());
    }

    @Override
    public T update(Query query, Update update) {
        return getMongoTemplate().findAndModify(query, update, this.getEntityClass());
    }

    @Override
    public T update(Query query, Update update, String collName) {
        return getMongoTemplate().findAndModify(query, update, this.getEntityClass(), collName);
    }

    @Override
    public T save(T entity) {
        getMongoTemplate().insert(entity);
        return entity;
    }

    @Override
    public <X> boolean save(Collection<X> objects, String collectionName) {
        try {
            getMongoTemplate().insert(objects, collectionName);
            return true;
        } catch (Exception e) {
            logger.error("--> mongoDao save error, collectionName : "+ collectionName,e);
        }
        return false;
    }

    @Override
    public T save(T entity, String collectionName) {
        getMongoTemplate().insert(entity, collectionName);
        return entity;
    }
    @Override
    public boolean batchSave(List<T> entities, String collectionName) {
        try {
            getMongoTemplate().insert(entities, collectionName);
            return true;
        } catch (Exception e) {
            logger.error("--> batchSave error, collectionName : " + collectionName, e);
        }
        return false;
    }


    @Override
    public T findById(String id) {
        return getMongoTemplate().findById(id, this.getEntityClass());
    }

    @Override
    public T findById(String id, String collectionName) {
        return getMongoTemplate().findById(id, this.getEntityClass(), collectionName);
    }

    @Override
    public long count(Query query) {
        return getMongoTemplate().count(query, this.getEntityClass());
    }

    /**
     * 获取需要操作的实体类class
     *
     * @return
     */
    private Class<T> getEntityClass() {
        return ReflectionUtils.getSuperClassGenricType(getClass());
    }

    public abstract MongoTemplate getMongoTemplate();
}
