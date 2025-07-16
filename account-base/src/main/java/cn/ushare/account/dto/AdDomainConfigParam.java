package cn.ushare.account.dto;

import lombok.Data;

@Data
public class AdDomainConfigParam {

    private static final long serialVersionUID = 1L;
    
    private String ip;
    
    private String name;
    
    private String port;
    
    private String dn;
    
    private String ssl;
    
    private String adOrLdap;
    
}
