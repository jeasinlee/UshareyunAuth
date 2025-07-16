package cn.ushare.account.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP日志记录，注解
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface  SystemLogTag {

	/*
	 * 操作说明
	 */
	String description()  default "";

	/*
	 * 模块信息
	 */
	String moduleName() default "";
}
