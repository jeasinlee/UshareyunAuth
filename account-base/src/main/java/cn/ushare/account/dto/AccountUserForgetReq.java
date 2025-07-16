package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2022-04-08
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountUserForgetReq {

    private static final long serialVersionUID = 1L;

    private String userName;
    private String mobile;   //手机号
    private String code;   //验证码
    private String replacePwd;  //新密码

}
