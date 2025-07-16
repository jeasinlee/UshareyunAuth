package cn.ushare.account.admin.interceptor;

import cn.ushare.account.admin.service.FuncResourceService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.admin.config.SecurityService;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.FuncResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 管理平台登录鉴权
 */
@Configuration
@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor  {
	
    @Autowired
    FuncResourceService resourceService;
    @Autowired
    SessionService sessionService;
	@Autowired
	private SecurityService securityService;
    
	@Override
	public boolean preHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler) throws Exception { 
		
	    String uri = request.getRequestURI();
		log.debug("interceptor uri " + uri);
	    // 根地址，跳转到dist/index.html
	    if (uri.equals("/")) {
	        RequestDispatcher dispatcher = request.getRequestDispatcher("/static/sys/index.html");
	        dispatcher.forward(request, response);
	        return true;
	    }
		
		// 1. 如果uri包含工程名前缀“/webauth/”，则是部署模式下，从tomcat的8080端口接收的请求，
		// 该请求含有工程名前缀，要去掉前缀，才能跟授权的接口url比较；
		// 2. 如果不含该前缀，则是调试模式下，从内嵌tomcat的9000端口接收的请求，该请求uri可以直接和授权接口url比较
		String tomcatPrefix = "/account/";
		int beginIndex = uri.indexOf(tomcatPrefix);
		if (beginIndex == 0) {
		    uri = uri.substring(tomcatPrefix.length() - 1, uri.length());
		}
	    
	    // uri是否在资源表中
	    QueryWrapper<FuncResource> resourceQuery = new QueryWrapper();
	    resourceQuery.eq("attr", uri);
	    resourceQuery.eq("type", 2);// 1菜单，2按钮
	    resourceQuery.eq("is_valid", 1);
	    List<FuncResource> resourceList = resourceService.list(resourceQuery);
	    if (resourceList.size() == 0) {
	        // 不在资源列表中，则不需要授权，允许通过
	        return true;
	    }
		
        Administrator admin = sessionService.getAdminInfo();
    	if (admin == null) {
    		response.setCharacterEncoding("utf-8");
        	response.setHeader("Accept", "application/json, text/plain, */*");
        	response.setHeader("Content-Type", "application/json;charset=UTF-8");
        	response.getWriter().print(securityService.encrypt("{\"returnCode\":\"999\",\"returnMsg\":\"请登录！\"}"));
    		//log.debug("admin auth refuse uri " + uri);
        	return false;
    	}
    	
    	// 如果id为1，是超级管理员，允许通过
    	if (admin.getId() == 1) {
    	    return true;
    	}
    	
    	// 查询角色权限
    	List<String> uriList = resourceService.getUriListByUserId(admin.getId());
    	boolean isAuthed = false;
    	for (String item : uriList) { 
            if (uri.equals(item)) {
                // uri在授权列表中
                isAuthed = true;
                break;
            }
        }
        if (isAuthed) {
            //用户有该uri授权，允许通过
            return true;
        } else {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Accept", "application/json, text/plain, */*");
            response.setHeader("Content-Type", "application/json;charset=UTF-8");
            response.getWriter().print(securityService.encrypt("{\"returnCode\":\"900\",\"returnMsg\":\"没有权限，请联系管理员！\"}"));
            return false;
        }
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
