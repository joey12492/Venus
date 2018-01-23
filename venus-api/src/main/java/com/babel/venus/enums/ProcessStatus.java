package com.babel.venus.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * User: joey
 * Date: 2017/9/2
 * Time: 20:32
 * 数据操作的状态
 */
public enum ProcessStatus {

    init(0),    //初始化
    failed(13), //失败
    success(100);   //成功

    private static Map<Integer, ProcessStatus> map = new HashMap<>();
    static {
        for (ProcessStatus obj : ProcessStatus.values()) {
            map.put(obj.status(), obj);
        }
    }

    private int status;
    ProcessStatus(int status) {
        this.status = status;
    }

    public int status() {
        return status;
    }

    public static ProcessStatus parse(int status) {
        return map.get(status);
    }
}
