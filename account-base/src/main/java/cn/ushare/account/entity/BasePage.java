package cn.ushare.account.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class BasePage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Long current;
    private Long size;
    private Long total;
    private Long pages;  
    
    public BasePage(Long current, Long size, Long total, Long pages) {
        super();
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
    }
    
    

}
