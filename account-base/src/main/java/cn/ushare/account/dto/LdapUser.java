package cn.ushare.account.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LdapUser {
    private String uid;
    private String userPassword;
    private String userCn;
    private String mobile;
    private String mail;
    private Boolean enable;
}
