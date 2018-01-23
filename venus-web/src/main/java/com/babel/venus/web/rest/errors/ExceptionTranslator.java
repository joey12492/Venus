package com.babel.venus.web.rest.errors;

import com.alibaba.fastjson.JSON;
import com.babel.common.core.data.RetData;
import com.babel.common.core.exception.*;
import com.babel.common.core.util.CommUtil;
import com.babel.common.web.context.AppContext;
import com.babel.venus.aop.logging.VenusLogAspect;
import com.babel.venus.exception.VenusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
public class ExceptionTranslator {

    private final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorVM processConcurrencyError(ConcurrencyFailureException ex) {
        return new ErrorVM(ErrorConstants.ERR_CONCURRENCY_FAILURE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        ErrorVM dto = new ErrorVM(ErrorConstants.ERR_VALIDATION);
        for (FieldError fieldError : fieldErrors) {
            dto.add(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode());
        }
        return dto;
    }

    @ExceptionHandler(CustomParameterizedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParameterizedErrorVM processParameterizedValidationError(CustomParameterizedException ex) {
        return ex.getErrorVM();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorVM processAccessDeniedException(AccessDeniedException e) {
        return new ErrorVM(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorVM processMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return new ErrorVM(ErrorConstants.ERR_METHOD_NOT_SUPPORTED, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> processException(HttpServletRequest request, Exception ex) {
    	log.debug("---processException--req="+this.getRequestMap(request));
        if (log.isDebugEnabled()) {
            log.warn("An unexpected error occurred: {}", ex.getMessage(), ex);
        } else {
            log.error("An unexpected error occurred: {}", ex.getMessage());
        }
        int httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Map<String, Object> errObj = getErrObj(VenusException.SYSTEM_CODE, httpStatus, "Internal server error!", "服务器内部错误!");
        VenusLogAspect.errLog(request, httpStatus, JSON.toJSONString(errObj));
        return errObj;
    }

    @ExceptionHandler(VenusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> procVenusException(HttpServletRequest request, VenusException ex) {
        int httpStatus = HttpStatus.OK.value();
        Map<String, Object> errObj = getErrObj(ex.getCode(), httpStatus, ex.getEnMsg(), ex.getCnMsg());
        VenusLogAspect.errLog(request, httpStatus, JSON.toJSONString(errObj));
        return errObj;
    }

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ResponseEntity<RetData> processBaseException(HttpServletRequest request, BaseException ex){
    	RetData retData=RetData.createByBaseException(ex);
    	log.error("---processBaseException--req="+this.getRequestMap(request)+"\n error=" + ex.getMessage());
    	if(ex instanceof InputErrException
    			||ex instanceof InputNullException
    			||ex instanceof NoPermissionException
    			||ex instanceof PasswordInvalidException){
    		 return new ResponseEntity<>(retData, HttpStatus.BAD_REQUEST);
    	}
    	else{
    		return new ResponseEntity<>(retData, HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    }


    private Map<String, String> getParams(Map<String, String[]> reqParams){
    	Set<String> keys=reqParams.keySet();
    	Map<String, String> result=new HashMap<>();
    	String vString=null;
    	for(String key:keys){
    		vString=CommUtil.concat(reqParams.get(key), ",");
    		vString=CommUtil.getStringLimit(vString, 50);//参数自动截短
    		result.put(key, vString);
    	}
    	return result;
    }
    private Map<String, Object> getRequestMap(HttpServletRequest req){
    	Map<String, Object> reqMap=new HashMap<String, Object>();
    	if(req==null){
    		reqMap.put("request", null);
    		return reqMap;
    	}
    	reqMap.put("sessionId", req.getSession().getId());
    	 //获得request 相关信息
        String contextpath=req.getContextPath();
//        reqMap.put("contextpath", contextpath);
        String characterEncoding = req.getCharacterEncoding();
        reqMap.put("characterEncoding", characterEncoding);
        String contentType = req.getContentType();
        reqMap.put("contentType", contentType);
        String method = req.getMethod();
        reqMap.put("method", method);
        reqMap.put("parameterMap", getParams(req.getParameterMap()));
        String protocol = req.getProtocol();
        reqMap.put("protocol", protocol);
        String serverName = req.getServerName();
        reqMap.put("serverName", serverName);
//        Cookie[] cookies = req.getCookies();
//        reqMap.put("cookies", cookies);
        String servletPath = req.getServletPath();
        reqMap.put("servletPath", servletPath);
        reqMap.put("remoteAddr", AppContext.getIpAddr(req));
        String requestURI = req.getRequestURI();
        reqMap.put("requestURI", requestURI);

        return reqMap;

    }

    private Map<String, Object> getErrObj(int code, int httpCode, String enMsg, String cnMsg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("httpCode", httpCode);
        result.put("enMsg", enMsg);
        result.put("cnMsg", cnMsg);
        result.put("err", "FAILED");
        return result;
    }
}
