package cn.ushare.account.admin.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@Slf4j
public class CorsInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Authorization,Origin, X-Requested-With, Content-Type, Accept,Access-Token");
        httpServletResponse.setHeader("Access-Control-Allow-Methods","*");
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("X-Powered-By","Ushareyun");

        String method= httpServletRequest.getMethod();
        if (method.equals("OPTIONS")){
            httpServletResponse.setStatus(200);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}