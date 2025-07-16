package cn.ushare.account.entity;

import lombok.ToString;

/**
 * 统一返回结果类
 */
@ToString
public class BaseResult<T> {

    // 状态码：1成功，其他为失败
    public String returnCode = "1";

    // 成功为success，其他为失败原因
    public String returnMsg = "成功";

    // 数据结果集
    public T data;

    public BaseResult(String returnCode, String returnMsg, T data) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
        this.data = data;
    }
    
    public BaseResult(T data) {
        this.data = data;
    }
    
    public BaseResult() {
        
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}