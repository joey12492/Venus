package com.babel.venus.exception;

import com.babel.common.core.exception.BaseException;

/**
 * User: joey
 * Date: 2017/9/26
 * Time: 19:42
 */
public class VenusException extends BaseException {

    public static final int SYSTEM_CODE = 12;

    //状态值
    private int code;

    //英文描述
    private String enMsg;

    //原因(前端展示用)
    private String cnMsg;


    private VenusException(int code, String enMsg, String cnMsg) {
        super(code, enMsg, cnMsg);
        this.code = Integer.parseInt((SYSTEM_CODE + "" + code));
        this.enMsg = enMsg;
        this.cnMsg = cnMsg;
    }

    /**
     * 未获取到平台商的赔率信息
     */
    public static final VenusException PLAT_LOTTERY_IS_NULL = new VenusException(1, "no plat odds info", "未获取到平台商的赔率信息");

    public static final VenusException NO_LOTTERY_TYPES = new VenusException(2, "no lottery types!", "未获取到彩种类型列表!");

    public static final VenusException NO_LOTTERY_TYPE = new VenusException(3, "no lottery type!", "未获取到彩种类型!");

    public static final VenusException INVALID_PDATE = new VenusException(4, "invalid pdate!", "无效的pdate!");




    @Override
    public String getErr() {
        return "FAILED";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getEnMsg() {
        return enMsg;
    }

    @Override
    public String getCnMsg() {
        return cnMsg;
    }
}
