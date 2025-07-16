package cn.ushare.account.dto;

import lombok.Data;

/**
 * 退出认证参数
 */
@Data
public class AuthLogoutParam {

    private String userIp;

    private String userMac;

    private String userName;

    private String acIp;

    private String nasIp;

    private Integer macPrior;
}
