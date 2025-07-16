package cn.ushare.account.log;

import com.alibaba.fastjson.JSON;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SystemLog;
import cn.ushare.account.mapper.LogMapper;
import cn.ushare.account.util.StringUtil;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 日志切面，要在application中@ComponentScan这个包，才会生效
 */
@Aspect
@Component
@Order(-5)
@Slf4j
public class LogAspect {

    @Autowired
    LogMapper logMapper;

    Long startTime;
    
    /**
     * 第一个 * 任意修饰符及任意返回值，第二个 * 任意包名，第三个 * 任意类，
     * 第四个 * web包或者子包，第五个 * 任意方法，..匹配任意数量的参数.
     */
    //@Pointcut("execution(public * cn.ushare.webauth.**.controller..*.*(..))")
    @Pointcut("execution(public * cn.ushare.account..*.controller..*.*(..))")//webauth..*表示webauth及其子类
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        startTime = System.currentTimeMillis();
        try {
            // 接收到请求，记录请求内容
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String beanName = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            String uri = request.getRequestURI();
            String remoteAddr = getIpAddr(request);
            String method = request.getMethod();
            String contentType = request.getContentType();
            String params = "";
            if(contentType==null){
                contentType="";
            }
            if ("POST".equals(method) && contentType.contains("json")) {
                Object[] paramsArray = joinPoint.getArgs();
                params = argsArrayToString(paramsArray);
            } else {
                Map<?, ?> paramsMap = (Map<?, ?>) request.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                params = paramsMap.toString();
            }
//            log.info("请求=" + uri + "; 类=" + beanName 
//                    + "; 接口=" + methodName + "; 参数=" + params 
//                    + "; IP=" + remoteAddr);
        } catch (Exception e) {
            log.error("***操作请求日志记录失败doBefore()***", e);
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Throwable {
        log.info("返回=" + JSON.toJSONString(ret));
        if (ret instanceof BaseResult) {
            JSONObject object = JSONObject.fromObject(ret);
            String successFlag = object.get("returnCode").toString();
            insertLog(joinPoint, successFlag, JSON.toJSONString(ret));
        }
    }
    
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime = System.currentTimeMillis();
        // 执行被监听接口
        Object object = joinPoint.proceed();
        
        // 记录日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) {
            return object;
        }
        HttpServletRequest request = attributes.getRequest();
        String beanName = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String uri = request.getRequestURI();
        String remoteAddr = getIpAddr(request);
        String method = request.getMethod();
        String contentType = request.getContentType();
        String params = "";
        if (contentType == null) {
            contentType="";
        }
        if ("POST".equals(method) && contentType.contains("json")) {
            Object[] paramsArray = joinPoint.getArgs();
            params = argsArrayToString(paramsArray);
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            params = paramsMap.toString();
        }
        float duration = System.currentTimeMillis() - startTime;
        log.info("请求=" + uri + "; 类=" + beanName 
                + "; 接口=" + methodName + "; 参数=" + params 
                + "; IP=" + remoteAddr
                + "; 耗时=" + duration + "毫秒");
        return object;
    }
    
    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Exception {
        insertLog(joinPoint, "0", e.getMessage());
        log.error("Error Exception=", e);
    }
    
    void insertLog(JoinPoint joinPoint, String resultCode, String resultData) throws Exception {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();
        String url = request.getRequestURL().toString();
        String remoteAddr = getIpAddr(request);
        String method = request.getMethod();
        String contentType = request.getContentType();
        String params = "";
        if (contentType == null) {
            contentType = "";
        }
        if ("POST".equals(method) && contentType.contains("json")) {
            Object[] paramsArray = joinPoint.getArgs();
            params = argsArrayToString(paramsArray);
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            params = paramsMap.toString();
        }
        
        //Long userId = 0L;
        //String userName = "";
        //request.getSession().getAttribute("adminId");
        String description = getMethodDescription(joinPoint);
        String moduleName = getMethodModuleName(joinPoint);
        
        //日志入库
        if(StringUtil.isNotBlank(moduleName)){
            SystemLog syslog = new SystemLog();
            //syslog.setUserId(userId);                 
            //syslog.setUserName(userName);
            syslog.setIp(remoteAddr);
            syslog.setApi(uri);
            syslog.setParam(params);
            syslog.setResultCode(resultCode);
            if (resultData != null && resultData.length() > 2048) {
                resultData = resultData.substring(0, 2047);
            }
            syslog.setResultData(resultData);
            syslog.setLevel(1);// 1debug，2info，3warn，4error
            syslog.setModule(moduleName + "-" + description);            
            int spendTime = (int) (System.currentTimeMillis() - startTime);
            syslog.setDuration(spendTime);
            logMapper.add(syslog);
        }
    }
    
    /**
     * 请求参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (int i = 0; i < paramsArray.length; i++) {
                Object jsonObj = JSON.toJSON(paramsArray[i]);
                params += jsonObj.toString() + " ";
            }
        }
        return params.trim();
    }

    /**
     * 获取方法的操作描述
     */
    private String getMethodDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        String description = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    if(method.getAnnotation(SystemLogTag.class) != null){
                        description = method.getAnnotation(SystemLogTag.class).description();
                        break;
                    }
                }
            }
        }
        return description;
    }

    /**
     * 获取方法的模块
     */
    private String getMethodModuleName(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        String moduleName = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    if(method.getAnnotation(SystemLogTag.class) != null){
                        moduleName = method.getAnnotation(SystemLogTag.class).moduleName();
                        break;
                    }
                }
            }
        }
        return moduleName;
    }
    
    // 获取客户端IP
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}