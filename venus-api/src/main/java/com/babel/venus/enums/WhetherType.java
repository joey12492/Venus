package com.babel.venus.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * User: joey
 * Date: 2017/9/5
 * Time: 16:24
 * 是否
 */
public enum WhetherType {

    yes(1),
    no(0);

    private static Map<Integer, WhetherType> map = new HashMap<>();

    static {
        for (WhetherType oj : WhetherType.values()) {
            map.put(oj.code, oj);
        }
    }

    private int code;

    WhetherType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static WhetherType parse(int code) {
        return map.get(code);
    }


}
