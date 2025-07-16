package cn.ushare.account.admin.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthInterceptorConfigurer implements WebMvcConfigurer {

	// 要加bean注解，否则报错@Autowired为null，因为拦截器加载时间点在springcontext之前，
    //	// 使用bean注解提前加载
    @Autowired
    AdminAuthInterceptor adminAuthInterceptor;
    @Autowired
    CorsInterceptor corsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        registry.addInterceptor(corsInterceptor)
                .addPathPatterns("/**");
        // 多个拦截器组成一个拦截器链        
    	registry.addInterceptor(adminAuthInterceptor)
                .excludePathPatterns("/static/**", "/login") //排除静态资源
    	        .addPathPatterns("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/login").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/main/**").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/portal/index").setViewName("forward:/static/app/index.html");
        registry.addViewController("/portal/mIndex").setViewName("forward:/static/app/mIndex.html");
        registry.addViewController("/m/**").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/charging/**").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/dingTalkResult").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/wxResult").setViewName("forward:/static/sys/index.html");
        registry.addViewController("/firstChangePassword").setViewName("forward:/static/app/account/firstChangePassword.html");
    }

}