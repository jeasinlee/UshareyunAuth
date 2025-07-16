package cn.ushare.account.admin.config;

import java.util.Date;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component
public class DbTimeHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        Object createTime = getFieldValByName("createTime", metaObject); 
        //System.out.println("createTime=" + createTime);
        if (createTime == null) {
            setFieldValByName("createTime", new Date(), metaObject); 
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", new Date(), metaObject); 
    }
    
}
