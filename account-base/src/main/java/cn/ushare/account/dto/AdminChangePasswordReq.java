package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AdminChangePasswordReq {

    private static final long serialVersionUID = 1L;

    private String oldPassword;

    private String newPassword;

}
