package cn.ushare.account.dto;

import lombok.Data;

@Data
public class LogBackupConfigParam {

    private static final long serialVersionUID = 1L;
    
    private String status;
    
    private String days;
    
    private String url;
    
}
