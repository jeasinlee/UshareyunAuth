package cn.ushare.account.exception;

import cn.ushare.account.entity.BaseErrResult;
import cn.ushare.account.util.StringUtil;

public class CommonError {

	public CommonError()
    {
    }
	 
	
	/**
	 * 抛出异常
	 * @param code 异常编码
	 * @param message 异常消息
	 */
	public static void CommonErr(BaseErrResult errResult){
		throw new CommonException(errResult.getCode(),errResult.getMessage());
	}
	
	/**
	 * 抛出异常
	 * @param code 异常编码
	 * @param message 异常消息（带参数s%）
	 * @param str 参数
	 */
	public static void CommonErr(BaseErrResult errResult,String str){
		String message = replaceMessage(errResult.getMessage(), str);
		throw new CommonException(errResult.getCode(),message);
	}
	
	/**
	 * 抛出异常
	 * @param code 异常编码
	 * @param message 异常消息（带参数s%）
	 * @param str1 参数1
	 * @param str2 参数2
	 */
	public static void CommonErr(BaseErrResult errResult ,String str1,String str2){
		String message = replaceMessage(errResult.getMessage(), str1);
		message = replaceMessage(message, str2); 
		throw new CommonException(errResult.getCode(),message);
	}
	
	/**
	 * @param code 异常编码
	 * @param message 异常消息（带参数s%）
	 * @param str1 参数1
	 * @param str2 参数2
	 * @param str3 参数3
	 */
	public static void CommonErr(BaseErrResult errResult,String str1,String str2,String str3){
		String message = replaceMessage(errResult.getMessage(), str1);
		message = replaceMessage(message, str2); 
		message = replaceMessage(message, str3); 
		throw new CommonException(errResult.getCode(),message);
	}
	
	private static String replaceMessage(String message, String replace)
    {
        message = StringUtil.replaceOnce(message, "%s", replace);
        return message;
    }


	 
	
}
