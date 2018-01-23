package com.babel.venus.util;

import com.alibaba.fastjson.JSONObject;
import com.babel.common.core.util.HttpClientUtil;
import com.babel.common.core.util.ResponseData;
import com.babel.hermes.commons.HermesServer;
import com.babel.venus.constants.VenusServer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.IllegalFormatFlagsException;
import java.util.Map;

/**
 * Created by zjh on 2016/12/9.
 */
@Component
public class AssembleHttpUtils {

    protected static final Logger log = Logger.getLogger(AssembleHttpUtils.class);


    public enum SendReq {

        VENUS_DRAW_PRIZE_INTERNAL(VenusServer.DRAW_PRIZE_INTERNAL, HttpMethod.POST,
                ImmutableList.of("pcode", "code", "lotteryCId", "drawPrizeStatus"), "内部开奖接口"),
        HERMES_DRAW_PRIZE_SUC_HANDLER(HermesServer.DRAW_PRIZE_SUC_HANDLER, HttpMethod.POST,
                ImmutableList.of("pcode", "lotteryId", "procStatus", "prizeStatus", "repairEnter"), "开奖后的数据etl");

        protected String url;
        protected HttpMethod method;
        protected String paramStr;
        protected ImmutableList<String> params;
        protected ImmutableList<String> headers;
        protected String description;


        SendReq(String url, HttpMethod method, String paramStr, ImmutableList<String> headers, String description) {
            this.url = url;
            this.method = method;
            this.paramStr = paramStr;
            this.headers = headers;
            this.description = description;
        }

        SendReq(String url, HttpMethod method, ImmutableList<String> params, String description) {
            this.url = url;
            this.method = method;
            this.params = params;
            this.description = description;
        }

        public enum HttpMethod {
            POST,
            GET;
        }
    }




    public ResponseData execRequest(String serverUrl, SendReq req, Object[] args, Object[] argHeaders) {

        Map<String, Object> params = convertParams(req.params, args);
        Map<String, Object> headers = convertParams(req.headers, argHeaders);
        ResponseData result;
        String url = serverUrl + req.url;
        try {
            switch (req.method) {
                case POST:
                    result = HttpClientUtil.buildPost(url, params, headers);
                    break;
                default:
                    result = HttpClientUtil.buildGet(url, params, headers);
                    break;
            }
            return result;
        } catch (Exception e) {
            log.error(req.method + " request robot error, url: " + url + " params: " + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    public ResponseData execRequest(String serverUrl, SendReq req, Object[] args) {
        Map<String, Object> params = convertParams(req.params, args);
        ResponseData result;
        String url = serverUrl + req.url;
        try {
            switch (req.method) {
                case POST:
                    result = HttpClientUtil.buildPost(url, params, null);
                    break;
                default:
                    result = HttpClientUtil.buildGet(url, params, null);
                    break;
            }
            return result;
        } catch (Exception e) {
            log.error(req.method + " request robot error, url: " + url + " params: " + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    public ResponseData execRequest(String serverUrl, SendReq req, String paramStr, Object[] argHeaders) {
        Map<String, Object> headers = convertParams(req.headers, argHeaders);
        ResponseData result = null;
        String url = serverUrl + req.url;
        try {
            switch (req.method) {
                case POST:
                    result = HttpClientUtil.buildPost(url, paramStr, headers);
                    break;
            }

            return result;
        } catch (Exception e) {
            log.error(req.method + " request robot error, url: " + url + " params: " + paramStr, e);
        }
        return null;
    }


    private static Map<String, Object> convertParams(ImmutableList<String> keys, Object[] values) {
        if (keys.size() == values.length) {
            Map<String, Object> params = Maps.newHashMap();
            for (int i = 0; i < keys.size(); i++) {
                if (values[i] != null) {
                    params.put(keys.get(i), values[i]);
                }
            }
            return params;
        } else {
            throw new IllegalFormatFlagsException("Request key's size does not match with values's size ,keys : " + keys.toString() + ", values : " + JSONObject.toJSONString(values));
        }
    }

}
