package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.SystemCmdService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.admin.service.AdService;
import cn.ushare.account.dto.*;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SystemConfig;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SystemConfigController", description = "系统配置")
@RestController
@Slf4j
@RequestMapping("/systemConfig")
public class SystemConfigController {

    private final static String moduleName = "系统配置";

    @Autowired
    SessionService sessionService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    SystemCmdService cmdService;
    @Autowired
    @Qualifier("adService")
    AdService adService;

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<SystemConfig> get(@RequestBody String id) {
        SystemConfig systemConfig = systemConfigService.getById(id);
        return new BaseResult("1", "成功", systemConfig);
    }

    /**
     * 根据code查询value
     */
    @ApiOperation(value="根据code查询value", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getByCode", method={RequestMethod.POST})
    public BaseResult<String> getByCode(@RequestBody String code) {
        String value = systemConfigService.getByCode(code);
        return new BaseResult(value);
    }

    /**
     * 认证方式状态查询
     */
    @ApiOperation(value="认证方式状态查询", notes="")
    @SystemLogTag(description="认证方式状态查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getAuthMethodStatus", method={RequestMethod.POST})
    public BaseResult<List<Map<String, String>>> getAuthMethodStatus() throws Exception {
        List<Map<String, String>> list = systemConfigService.getByLike("AUTH-METHOD");
        return new BaseResult(list);
    }

    /**
     * API认证配置查询
     */
    @ApiOperation(value="API认证配置查询", notes="")
    @SystemLogTag(description="API认证配置查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getApiAuthConfig", method={RequestMethod.POST})
    public BaseResult getApiAuthConfig() throws Exception {
        String apiMethod = systemConfigService.getByCode("ACCOUNT-AUTH-METHOD");
        String url = systemConfigService.getByCode("API-AUTH-URL");
        String reqParam = systemConfigService.getByCode("API-AUTH-PARAM");
        String respParam = systemConfigService.getByCode("API-AUTH-RET");
        String adMethod = systemConfigService.getByCode("AD-DOMAIN-STATUS");
        Map<String, Object> map = new HashMap<>();
        map.put("apiMethod", Integer.valueOf(apiMethod));
        map.put("url", url);
        map.put("reqParam", reqParam);
        map.put("respParam", respParam);
        map.put("adMethod", adMethod);
        return new BaseResult(map);
    }

    /**
     * 更新API认证配置
     */
    @ApiOperation(value="更新API认证配置", notes="")
    @SystemLogTag(description="修改API认证配置", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateApiAuthConfig", method={RequestMethod.POST})
    public BaseResult updateApiAuthConfig(@RequestBody String apiAuthConfigJson) throws Exception {
        ApiAuthConfigParam apiAuthConfigParam = JsonObjUtils.json2obj(apiAuthConfigJson, ApiAuthConfigParam.class);
        return systemConfigService.updateApiAuthConfig(apiAuthConfigParam);
    }

    /**
     * AD域配置查询
     */
    @ApiOperation(value="AD域配置查询", notes="")
    @SystemLogTag(description="AD域配置查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getAdDomainConfig", method={RequestMethod.POST})
    public BaseResult<List<Map<String, String>>> getAdDomainConfig() throws Exception {
        return systemConfigService.getAdDomainConfig();
    }

    /**
     * AD域配置更新
     */
    @ApiOperation(value="AD域配置更新", notes="")
    @SystemLogTag(description="修改AD域配置", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateAdDomainConfig", method={RequestMethod.POST})
    public BaseResult updateAdDomainConfig(@RequestBody AdDomainConfigParam param) throws Exception {
        return systemConfigService.updateAdDomainConfig(param);
    }

    /**
     * 服务器时间同步方式查询
     */
    @ApiOperation(value="服务器时间同步方式查询", notes="")
    @SystemLogTag(description="服务器时间同步方式查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getServerTimeAdjustConfig", method={RequestMethod.POST})
    public BaseResult<List<Map<String, String>>> getServerTimeAdjustConfig() throws Exception {
        return systemConfigService.getServerTimeAdjustConfig();
    }

    /**
     * 日志备份配置查询
     */
    @ApiOperation(value="日志备份配置查询", notes="")
    @SystemLogTag(description="日志备份配置查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getLogBakupConfig", method={RequestMethod.POST})
    public BaseResult getLogBakupConfig() throws Exception {
        return systemConfigService.getLogBakupConfig();
    }

    /**
     * 日志备份配置更新
     */
    @ApiOperation(value="日志备份配置更新", notes="")
    @SystemLogTag(description="修改日志备份配置", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateLogBakupConfig", method={RequestMethod.POST})
    public BaseResult updateLogBakupConfig(@RequestBody LogBackupConfigParam param) throws Exception {
        return systemConfigService.updateLogBakupConfig(param);
    }

    /**
     * 更新使用指引标志位
     */
    @ApiOperation(value="修改初次使用指引标志", notes="")
    @SystemLogTag(description="修改初次使用指引标志", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateShowGuideConfig", method={RequestMethod.POST})
    public BaseResult updateShowGuideConfig() throws Exception {
        return systemConfigService.updateByCode("SHOW-USER-GUIDE", "0");
    }

    /**
     * 设置系统时间同步参数
     */
    @ApiOperation(value="修改系统同步时间", notes="")
    @SystemLogTag(description="修改系统同步时间", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/setSyncTime", method={RequestMethod.POST})
    public BaseResult setSyncTime(@RequestBody SystemTimeSyncReq param) throws Exception {
        systemConfigService.setSyncTime(param);
        return new BaseResult("1", "成功", param);
    }

    /**
     * 重启服务器
     */
    @ApiOperation(value="重启服务器", notes="")
    @SystemLogTag(description="重启服务器", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/rebootSystem", method={RequestMethod.POST})
    public BaseResult rebootSystem() throws Exception {
        return systemConfigService.rebootSystem();
    }

    /**
     * 重启软件
     */
    @ApiOperation(value="重启软件", notes="")
    @SystemLogTag(description="重启软件", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/rebootSoftware", method={RequestMethod.POST})
    public BaseResult rebootSoftware() throws Exception {
        return systemConfigService.rebootSoftware();
    }

    /**
     * 获取系统时间
     */
    @ApiOperation(value="获取系统时间", notes="")
    @SystemLogTag(description="获取系统时间", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getSystemTime", method={RequestMethod.POST})
    public BaseResult getSystemTime() throws Exception {
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = datetimeFormat.format(new Date());
        return new BaseResult("1", "成功", time);
    }

    /**
     * 获取系统信息
     */
    @ApiOperation(value="获取系统信息", notes="")
    @SystemLogTag(description="查询系统信息", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getSystemInfo", method={RequestMethod.POST})
    public BaseResult getSystemInfo() throws Exception {
        return cmdService.getSystemInfo();
    }

    /**
     * 查询管理后台登录页企业信息
     */
    @ApiOperation(value="查询管理后台登录页企业信息", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getCompanyInfo")
    public BaseResult getCompanyInfo() throws Exception {
        String logo = systemConfigService.getByCode("OFFICIAL_LOGO");
        String companyName = systemConfigService.getByCode("OFFICIAL_NAME");
        String official = systemConfigService.getByCode("OFFICIAL_WEBSITE");
        String smsLeft = systemConfigService.getByCode("SMS-LEFT");
        String showCode = systemConfigService.getByCode("VALID_CODE");
        String smsCheck = systemConfigService.getByCode("SMS-CHECK");

        SystemConfig redirectConfig = systemConfigService.getOne(new QueryWrapper<SystemConfig>().eq("code", "AUTH-DEFAULT-VISIT-URL"), false);
        Map<String, Object> map = new HashMap<>();
        map.put("logo", logo);
        map.put("companyName", companyName);
        map.put("official", official);
        map.put("smsLeft", smsLeft);
        map.put("smsCheck", smsCheck);
        map.put("redirectUrl", redirectConfig.getValue());
        map.put("redirectUrlValid", redirectConfig.getIsValid());
        map.put("showCode", "1".equals(showCode)?1:0);
        return new BaseResult(map);
    }

    /**
     * 更新管理后台登录页企业信息
     */
    @ApiOperation(value="更新管理后台登录页企业信息", notes="")
    @SystemLogTag(description="修改企业信息", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateCompanyInfo", method={RequestMethod.POST})
    public BaseResult updateCompanyInfo(@RequestBody CompanyInfoReq param) throws Exception {
        if(StringUtils.isNotBlank(param.getLogo())) {
            systemConfigService.updateByCode("OFFICIAL_LOGO", param.getLogo());
        }
        if(StringUtils.isNotBlank(param.getCompanyName())) {
            systemConfigService.updateByCode("OFFICIAL_NAME", param.getCompanyName());
        }
        if(StringUtils.isNotBlank(param.getShowCode())) {
            systemConfigService.updateByCode("VALID_CODE", param.getShowCode());
        }
        if(StringUtils.isNotBlank(param.getRedirectUrl())) {
            SystemConfig redirectConfig = systemConfigService.getOne(new QueryWrapper<SystemConfig>().eq("code", "AUTH-DEFAULT-VISIT-URL"), false);
            redirectConfig.setValue(param.getRedirectUrl());
            redirectConfig.setIsValid(Integer.parseInt(param.getRedirectUrlValid()));
            systemConfigService.saveOrUpdate(redirectConfig);
        }
        if(StringUtils.isNotBlank(param.getOfficial())) {
            systemConfigService.updateByCode("OFFICIAL_WEBSITE", param.getOfficial());
        }
        if(StringUtils.isNotBlank(param.getSmsCheck())) {
            systemConfigService.updateByCode("SMS-CHECK", param.getSmsCheck());
        }

        return new BaseResult();
    }


    @ApiOperation(value="查询手机号是否合法（包含在LDAP账户）", notes="")
    @SystemLogTag(description="校验手机号", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/verify_phone", method={RequestMethod.POST})
    public BaseResult verifyPhone(@RequestBody String strJson) throws Exception {
        JSONObject jsonObject = JSONObject.fromObject(strJson);
        String mobile = jsonObject.optString("mobile", null);
        if(StringUtils.isBlank(mobile)){
            return new BaseResult("-1", "关键参数不存在", null);
        }
        List<LdapUser> user = adService.findUser(mobile);
        boolean check = CollectionUtils.isNotEmpty(user);
        if(check){
            return new BaseResult();
        }
        return new BaseResult("-1", "手机号不合法", null);
    }
}
