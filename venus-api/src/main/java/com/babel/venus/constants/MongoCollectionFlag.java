package com.babel.venus.constants;

/**
 * User: joey
 * Date: 2017/5/30
 * Time: 15:57
 * 数据插入失败的标识
 */
public enum MongoCollectionFlag {

    MONGO_FAILED("failed"), //失败
    PROC_SUC("proc"),   //开奖成功
    REPAIR("repair");   //需要修复

    private String value;

    MongoCollectionFlag(String value) {
        this.value = value;
    }

    public String collName(String collection) {
        return collection + "_" + value;
    }

    public static String getDateName(String name, String pdate) {
        return name + "_" + pdate;
    }
}
