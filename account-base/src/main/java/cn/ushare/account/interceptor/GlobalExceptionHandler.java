package cn.ushare.account.interceptor;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.exception.CommonRuntimeException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局的异常拦截
 */
@RestController
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 通用异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object defaultErrorHandler(HttpServletRequest req, Exception e) {
        BaseResult baseResult = new BaseResult();
        baseResult.setReturnCode("-1");
        if (null != e.getMessage()) {
            baseResult.setReturnMsg(e.getMessage());
        } else {
            baseResult.setReturnMsg(e.toString());
        }
        log.error("异常=", e);
        //log.info("异常=" + JSON.toJSONString(baseResult));
        return baseResult;
    }
    
    @ExceptionHandler(value = CommonRuntimeException.class)
    @ResponseBody
    public Object baseErrorHandler(HttpServletRequest req, Exception e)
            throws Exception {
        BaseResult baseResult = new BaseResult();
        baseResult.setReturnCode(((CommonRuntimeException) e).getCode());
        //baseResult.setReturnMsg("失败");
        baseResult.setReturnMsg(e.getMessage());
        log.error("RESPONSE : ", e);
        return baseResult;
    }
    
    /**
     * 参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Object validExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) {
        BaseResult baseResult = new BaseResult();
        baseResult.setReturnCode("-1");
        StringBuilder sb = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error:fieldErrors){
            //logger.error(error.getField() + ":" + error.getDefaultMessage());
            sb.append(error.getField() + error.getDefaultMessage() + ",");
        }
        baseResult.setReturnMsg(sb.substring(0, sb.length() - 1));
        log.error("异常=", e);
        return baseResult;
    }
    
}