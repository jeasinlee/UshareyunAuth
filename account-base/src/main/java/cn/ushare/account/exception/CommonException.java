package cn.ushare.account.exception;


public class CommonException extends CommonRuntimeException {

	 
	
	/**
	 * @param standardResp
	 */
	public CommonException(String code,String message){
		super(message, null, code, null);
	}
	

	/**
	 * Constructors
	 * 
	 * @param cause
	 *            异常接口
	 * @param code
	 *            错误代码
	 */
	public CommonException(Throwable cause, String code) {
		super(code, cause, code, null);
	}

	/**
	 * Constructors
	 * 
	 * @param code
	 *            错误代码
	 * @param values
	 *            一组异常信息待定参数
	 */
	public CommonException(String code, Object[] values) {
		super(code, null, code, values);
	}

	/**
	 * Constructors
	 * 
	 * @param cause
	 *            异常接口
	 * @param code
	 *            错误代码
	 * @param values
	 *            一组异常信息待定参数
	 */
	public CommonException(Throwable cause, String code, Object[] values) {
		super(code, null, code, values);
	}

	private static final long serialVersionUID = -3711290613973933714L;

}
