package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2022-04-08
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountUserModifyReq {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String oldPwd;   //原密码

    private String replacePwd;  //新密码

}
