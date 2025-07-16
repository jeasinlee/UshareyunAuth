package cn.ushare.account.admin.config;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.AfterSale;
import cn.ushare.account.entity.AuthParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Licence授权数据
 */
@Component
@Slf4j
public class LicenceCache {
    
    private ConcurrentHashMap<String, Object> licenceMap = 
            new ConcurrentHashMap<String, Object>();
    
    /**
     * 新增、修改
     */
    public void addOrUpdate(LicenceInfo licenceInfo, List<AfterSale> afterSaleList) {
        log.debug("addOrUpdate " + licenceInfo.toString());
        licenceMap.put("licenceInfo", licenceInfo);
        licenceMap.put("afterSaleList", afterSaleList);
    }
    
    /**
     * 查询Licence
     */
    public LicenceInfo getLicenceInfo() {
        if (null == licenceMap.get("licenceInfo")) {
            return null;
        }
        LicenceInfo obj = (LicenceInfo) licenceMap.get("licenceInfo");
        return obj;
    }
    
    /**
     * 查询AfterSale
     */
    public List<AfterSale> getAfterSaleList() {
        if (licenceMap.get("afterSaleList") == null) {
            return null;
        }
        List<AfterSale> obj = (List<AfterSale>) licenceMap.get("afterSaleList");
        return obj;
    }
    
    /**
     * 删除
     */
    public void removeTempLogin() {
        licenceMap.remove("licenceInfo");
        licenceMap.remove("afterSaleList");
    }
    
}
