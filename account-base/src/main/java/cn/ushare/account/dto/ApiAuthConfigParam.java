package cn.ushare.account.dto;

import lombok.Data;

@Data
public class ApiAuthConfigParam {

    private static final long serialVersionUID = 1L;
    
    private Integer apiMethod;
    
    private String url;
    
    private String reqParam;
    
    private String respParam;

    private Integer adMethod;
    
}
