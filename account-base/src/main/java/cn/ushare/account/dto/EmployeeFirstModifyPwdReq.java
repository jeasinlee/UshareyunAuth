package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-10-18
 * @email jeasinlee@163.com
 */
@Data
public class EmployeeFirstModifyPwdReq {

    private static final long serialVersionUID = 1L;

    private String userName;// 账户
    private String oldPwd;// 原密码

    private String newPassword;// 新密码

}
