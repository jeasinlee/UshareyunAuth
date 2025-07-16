package cn.ushare.account.admin.config;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.ushare.account.entity.AuthParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GlobalCache {
    
    // 临时放行记录，用于钉钉认证、微信认证前的临时上网，键值对：UserIp -> AuthRecord
    private ConcurrentHashMap<String, CacheObj> tempLoginMap = 
            new ConcurrentHashMap<String, CacheObj>();
    
    @Data
    public class CacheObj {
        private Object CacheValue;
        private Long createTime;
        
        CacheObj(Object cacheValue, Long createTime) {
            CacheValue = cacheValue;
            this.createTime = createTime;
        }
    }
    
    /**
     * 读取tempLoginMap
     */
    public ConcurrentHashMap<String, CacheObj> getTempLoginMap() {
        return tempLoginMap;
    }
    
    /**
     * 新增、修改tempLogin
     */
    public void addOrUpdateTempLogin(AuthParam authParam) {
        log.debug("addOrUpdate " + authParam.toString());
        tempLoginMap.put(authParam.getUserIp(), 
                new CacheObj(authParam, System.currentTimeMillis()));
    }
    
    /**
     * 查询tempLogin
     */
    public AuthParam getTempLogin(String userIp) {
        log.debug("getTempLogin " + userIp);
        if(StringUtils.isNotBlank(userIp)){
            CacheObj obj = tempLoginMap.get(userIp);
            if (obj != null) {
                return (AuthParam) obj.getCacheValue();
            }
        }

        return null;
    }
    
    /**
     * 删除tempLogin
     */
    public void removeTempLogin(String userIp) {
        if (userIp != null) {
            tempLoginMap.remove(userIp);
        }
    }
    
}
