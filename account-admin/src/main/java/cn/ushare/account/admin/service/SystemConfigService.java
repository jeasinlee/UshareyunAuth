package cn.ushare.account.admin.service;

import cn.ushare.account.dto.AdDomainConfigParam;
import cn.ushare.account.dto.ApiAuthConfigParam;
import cn.ushare.account.dto.LogBackupConfigParam;
import cn.ushare.account.dto.SystemTimeSyncReq;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SystemConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SystemConfigService extends IService<SystemConfig> {

    BaseResult getServerTimeAdjustConfig();

    BaseResult setSyncTime(SystemTimeSyncReq param);

    BaseResult rebootSystem();

    BaseResult rebootSoftware();

    Page<SystemConfig> getList(Page<SystemConfig> page, QueryWrapper wrapper);

    List<Map<String, String>> getByLike(String code);

    BaseResult updateApiAuthConfig(ApiAuthConfigParam apiAuthConfig);

    BaseResult getLogBakupConfig();

    BaseResult updateLogBakupConfig(LogBackupConfigParam param);

    BaseResult uploadDbBackup();

    BaseResult getAdDomainConfig();

    BaseResult updateAdDomainConfig(AdDomainConfigParam param);

    BaseResult updateByCode(String code, String value);

//    BaseResult updateByMap(Map<String, String> param);

    String getByCode(String code);
}
