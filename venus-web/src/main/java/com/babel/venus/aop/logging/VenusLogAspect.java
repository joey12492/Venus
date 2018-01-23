package com.babel.venus.aop.logging;

import com.alibaba.fastjson.JSON;
import com.babel.common.core.util.*;
import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjh on 2017/1/10.
 */
@Aspect
public class VenusLogAspect {

    private static final Logger logger = Logger.getLogger(VenusLogAspect.class);
    private static final String SPLIT = "\t";

    @Autowired
    private HttpServletResponse response;

    private static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<>();

    private static Map<String, RequestLogRecord> recordMap = new HashMap<>();


    @Pointcut("(execution(public * com.babel.venus.web.rest.*Resource.*(..)))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBeforeReq() {
        RequestIDGenerator reqId = DefaultRequestIdGenerator.getInstance();
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        setRequestLocal(request);
        RequestLogRecord record = new RequestLogRecord();
        record.setDate(new Date());
        record.setRequestId(reqId.nextId());
        record.setReqUri(request.getRequestURI());
        record.setMethod(request.getMethod());
        record.setIp(IPUtil.getRealIpAddr(request));
        record.setParameters(request.getParameterMap());
        record.setUseTime(System.currentTimeMillis());
        recordMap.put(request.getSession().getId(), record);
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReq(Object ret) {
        try {
            // 记录下请求内容
            StringBuffer buf = new StringBuffer();
            RequestLogRecord record = recordMap.get(getRequestLocal().getSession().getId());
            buf.append(DateUtils.format(record.getDate(), "yyyy-MM-dd HH:mm:ss,S"))
                .append(SPLIT);
            buf.append(record.getRequestId())
                .append(SPLIT);
            buf.append(record.getReqUri())
                .append(SPLIT);
            buf.append(record.getMethod())
                .append(SPLIT);
            buf.append(System.currentTimeMillis() - record.getUseTime())
                .append(SPLIT);
            buf.append(response.getStatus())
                .append(SPLIT);
            buf.append(buildParameterString(record.getParameters()))
                .append(SPLIT);
            buf.append(record.getIp())
                .append(SPLIT);
            String resResult = JSON.toJSONString(ret);
            if (resResult.length() > 400) {
                buf.append(resResult).substring(0, 400);
            } else {
                buf.append(resResult);
            }
            // 处理完请求，返回内容
            logger.info(buf.toString());
            recordMap.remove(getRequestLocal().getSession().getId());
        } catch (Exception e) {
            logger.error("print req log error,", e);
        }
    }

    public static void errLog(HttpServletRequest request, int httpStatus, String response) {
        RequestIDGenerator reqId = DefaultRequestIdGenerator.getInstance();
        RequestLogRecord record = new RequestLogRecord();
        record.setRequestId(reqId.nextId());
        record.setReqUri(request.getRequestURI());
        record.setMethod(request.getMethod());
        record.setIp(IPUtil.getRealIpAddr(request));
        record.setParameters(request.getParameterMap());

        // 记录下请求内容
        StringBuilder buf = new StringBuilder();
        buf.append(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,S"))
            .append(SPLIT);
        buf.append(record.getRequestId())
            .append(SPLIT);
        buf.append(record.getReqUri())
            .append(SPLIT);
        buf.append(record.getMethod())
            .append(SPLIT);
        buf.append(0)
            .append(SPLIT);
        buf.append(httpStatus)
            .append(SPLIT);
        buf.append(buildParameterString(record.getParameters()))
            .append(SPLIT);
        buf.append(record.getIp())
            .append(SPLIT)
            .append(response);
        // 处理完请求，返回内容
        logger.error(buf.toString());
    }

//    @Around("execution(public * com.youyun.customer.controller.*.*(..))")
//    public void check(ProceedingJoinPoint point) throws Throwable {
//        System.out.println(response.getStatus());
//        System.out.println(response.getBufferSize());
//        System.out.println(response.getOutputStream().toString());
//        System.out.println(">>>>>>>>>>>>>>>>>>>>> around");
//        MethodSignature signature = (MethodSignature) point.getSignature();
//        //获取被代理方法的类名
//        String className = signature.getDeclaringTypeName();
//        //获取被代理的方法名
//        Method method = signature.getMethod();
//        String methodName = method.getName();
//        //通过方法参数获取到session，拿到用户信息
//        for (Object arg : point.getArgs()) {
//            if (arg instanceof HttpServletResponse) {
////                HttpServletResponse response = point.get;
////                System.out.println(response.getStatus());
////                break;
//            } else if (arg instanceof HttpServletRequest) {
//                // 接收到请求，记录请求内容
//                HttpServletRequest request = (HttpServletRequest) arg;
//                spendTime = System.currentTimeMillis();
//                record = new RequestLogRecord();
//                record.setDate(new Date());
//                record.setRequestId(reqId.nextId());
//                record.setReqUri(request.getRequestURI());
//                record.setMethod(request.getMethod());
//                record.setIp(IPUtils.getRealIpAddr(request));
//                record.setParameters(request.getParameterMap());
//            }
//        }
//    }

    private static String buildParameterString(Map<String, String[]> parameters) {
        if (parameters != null) {
            StringBuilder paramBuf = new StringBuilder();
            for (Map.Entry<String, String[]> e : parameters.entrySet()) {
                String key = e.getKey();
                String[] values = e.getValue();
                for (String value : values) {
                    paramBuf.append(key).append("=").append(URLEncodeUtils.encodeURL(value));
                    paramBuf.append("&");
                }
            }
            if (paramBuf.length() > 0 && paramBuf.charAt(paramBuf.length() - 1) == '&') {
                paramBuf.deleteCharAt(paramBuf.length() - 1);
            }
            return paramBuf.toString();
        }
        return null;
    }


    private HttpServletRequest getRequestLocal() {
        return requestLocal.get();
    }

    private void setRequestLocal(HttpServletRequest request) {
        requestLocal.set(request);
    }

}
