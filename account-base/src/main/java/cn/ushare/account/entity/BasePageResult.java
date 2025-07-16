package cn.ushare.account.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class BasePageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private BasePage page;
    private List<T> list;

    public BasePageResult(BasePage page, List<T> list) {
        super();
        this.page = page;
        this.list = list;
    }
    
    

}
