package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class UserLoginReq {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String password;

}
