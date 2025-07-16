package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class EmployeeChangePwdReq {

    private static final long serialVersionUID = 1L;

    private String smsCode;// 短信验证码

    private String newPassword;// 新密码

}
