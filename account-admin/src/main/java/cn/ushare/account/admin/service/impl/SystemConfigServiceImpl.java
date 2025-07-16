package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.mapper.SystemConfigMapper;
import cn.ushare.account.admin.service.SystemCmdService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.dto.AdDomainConfigParam;
import cn.ushare.account.dto.ApiAuthConfigParam;
import cn.ushare.account.dto.LogBackupConfigParam;
import cn.ushare.account.dto.SystemTimeSyncReq;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SystemConfig;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Value("${enviroment}")
    String enviroment;

    @Autowired
    SystemConfigMapper systemConfigMapper;
    @Autowired
    SystemCmdService cmdService;
    @Autowired
    LicenceCache licenceCache;

    @Override
    public Page<SystemConfig> getList(Page<SystemConfig> page, QueryWrapper wrapper) {
        return page.setRecords(systemConfigMapper.getList(page, wrapper));
    }

    /**
     * 修改，参数格式：
     */
    @Override
    public BaseResult updateByCode(String code, String value) {
        systemConfigMapper.updateByCode(code, value);
        return new BaseResult();
    }

    /**
     * 修改，参数格式：
     * {
     *  "ACCOUNT-AUTH-IP": "192.168.0.1",
     *  "ACCOUNT-AUTH-METHOD": 1,
     * }
     */
//    @Override
//    public BaseResult updateByMap(Map<String, String> param) {
//        for (Map.Entry<String, String> entry : param.entrySet()) {
//            systemConfigMapper.updateByCode(entry.getKey(), entry.getValue());
//        }
//        return new BaseResult();
//    }

    /**
     * 查询code对应的value
     */
    @Override
    public String getByCode(String code) {
        return systemConfigMapper.getByCode(code);
    }

    @Override
    public BaseResult updateApiAuthConfig(ApiAuthConfigParam apiAuthConfig) {
        updateByCode("ACCOUNT-AUTH-METHOD", apiAuthConfig.getApiMethod() + "");
        if(StringUtils.isNotBlank(apiAuthConfig.getUrl())) {
            updateByCode("API-AUTH-URL", apiAuthConfig.getUrl());
        }
        if(StringUtils.isNotBlank(apiAuthConfig.getReqParam())) {
            updateByCode("API-AUTH-PARAM", apiAuthConfig.getReqParam());
        }
        if(StringUtils.isNotBlank(apiAuthConfig.getRespParam())) {
            updateByCode("API-AUTH-RET", apiAuthConfig.getRespParam());
        }
        updateByCode("AD-DOMAIN-STATUS", apiAuthConfig.getAdMethod() + "");
        return new BaseResult();
    }

    @Override
    public BaseResult getLogBakupConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", systemConfigMapper.getByCode("LOG-BAKUP-STATUS"));
        map.put("days", systemConfigMapper.getByCode("LOG-BAKUP-SAVE-DAYS"));
        map.put("url", systemConfigMapper.getByCode("LOG-BAKUP-CLOUD-URL"));
        return new BaseResult(map);
    }

    @Override
    public BaseResult updateLogBakupConfig(LogBackupConfigParam param) {
        if (param.getStatus().equals("1")) {
            if (null == licenceCache.getLicenceInfo()) {
                return new BaseResult("0", "请先升级授权后再配置日志备份", null);
            }
        }
        systemConfigMapper.updateByCode("LOG-BAKUP-STATUS", param.getStatus());
        systemConfigMapper.updateByCode("LOG-BAKUP-SAVE-DAYS", param.getDays());
        systemConfigMapper.updateByCode("LOG-BAKUP-CLOUD-URL", param.getUrl());
        return new BaseResult();
    }

    @Override
    public List<Map<String, String>> getByLike(String code) {
        return systemConfigMapper.getByLike(code);
    }

    /**
     * 重启服务器
     */
    @Override
    public BaseResult rebootSystem() {
        cmdService.rebootSystem();
        return new BaseResult("1", "服务器重启中，稍后请重新登录", null);
    }

    /**
     * 重启软件
     */
    @Override
    public BaseResult rebootSoftware() {
        if (enviroment.equals("dev")) {
            return new BaseResult("0", "调试模式下不支持重启", null);// 开发模式下，使用内嵌tomcat，没有部署模式下的重启脚本
        }
        BaseResult result = cmdService.rebootTomcat();
        if (result.getReturnCode().equals("0")) {
            return result;
        }
        return new BaseResult("1", "软件重启中，稍后请重新登录", null);
    }

    /**
     * 查询AD域配置
     */
    @Override
    public BaseResult getAdDomainConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("ip", systemConfigMapper.getByCode("AD-DOMAIN-IP"));
        map.put("name", systemConfigMapper.getByCode("AD-DOMAIN-DOMAIN-NAME"));
        map.put("port", systemConfigMapper.getByCode("AD-DOMAIN-PORT"));
        map.put("dn", systemConfigMapper.getByCode("AD-DOMAIN-DN"));
        map.put("ssl", systemConfigMapper.getByCode("AD-DOMAIN-SSL"));
        map.put("adOrLdap", systemConfigMapper.getByCode("AD-OR-LDAP"));
        return new BaseResult(map);
    }

    /**
     * 更新AD域配置
     */
    @Override
    public BaseResult updateAdDomainConfig(AdDomainConfigParam param) {
        systemConfigMapper.updateByCode("AD-DOMAIN-IP", param.getIp());
        systemConfigMapper.updateByCode("AD-DOMAIN-DOMAIN-NAME", param.getName());
        systemConfigMapper.updateByCode("AD-DOMAIN-PORT", param.getPort());
        systemConfigMapper.updateByCode("AD-DOMAIN-DN", param.getDn());
        systemConfigMapper.updateByCode("AD-DOMAIN-SSL", param.getSsl());
        systemConfigMapper.updateByCode("AD-OR-LDAP", param.getAdOrLdap());
        return new BaseResult();
    }

    /**
     * 更新服务器同步时间配置
     */
    @Override
    public BaseResult setSyncTime(SystemTimeSyncReq param) {
        // 如果是自动同步，请求时间服务器接口，获取时间
        if (param.getSync().equals("1")) {
            BaseResult result = getBeijinTime();
            if (result.getReturnCode().equals("0")) {
                return new BaseResult("0", "在线时间获取失败，请检查接口地址", null);
            }
            param.setDatetime((String) result.getData());
        }

        // 更新服务器时间，格式“2019-10-10 12:00:00”
        cmdService.syncTime(param.getDatetime());

        // 更新数据库
        updateByCode("SERVER-TIME-ADJUST", param.getSync());
        updateByCode("SERVER-TIME-ADJUST-URL", param.getUrl());
        return new BaseResult();
    }

    /**
     * 查询服务器同步时间配置
     */
    @Override
    public BaseResult getServerTimeAdjustConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("sync", systemConfigMapper.getByCode("SERVER-TIME-ADJUST"));
        map.put("url", systemConfigMapper.getByCode("SERVER-TIME-ADJUST-URL"));
        return new BaseResult(map);
    }

    private BaseResult getBeijinTime() {
        // 获取请求地址
        String reqUrl = systemConfigMapper.getByCode("SERVER-TIME-ADJUST-URL");

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            post = new HttpPost(reqUrl);
            post.setHeader("Content-type", "application/json; charset=utf-8");
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("请求出错: " + statusCode);
                return new BaseResult("0", "在线时间查询接口状态码异常：" + statusCode, null);
            } else {
                String resultStr = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug("在线时间查询返回：" + resultStr);
                if (StringUtil.isBlank(resultStr)) {
                    return new BaseResult("0", "在线时间查询返回为空", null);
                } else {
                    // 接口地址：http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp
                    // 数据格式：{"api":"mtop.common.getTimestamp","v":"*","ret":["SUCCESS::接口调用成功"],"data":{"t":"1560753064995"}}
                    Map<String, Object> resultMap = (Map<String, Object>) JsonObjUtils.json2map(resultStr);
                    if (resultMap.get("ret") == null) {
                        return new BaseResult("0", "在线时间查询返回参数错误", null);
                    }

                    JSON dataJson = (JSON) resultMap.get("data");
                    Map<String, Object> dataMap = (Map<String, Object>) JsonObjUtils.json2map(dataJson.toJSONString());
                    String milliSecondStr = (String) dataMap.get("t");
                    Date date = new Date(Long.valueOf(milliSecondStr));
                    SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    datetimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    String dateStr = datetimeFormat.format(date);
                    log.debug("online time " + dateStr);
                    return new BaseResult(dateStr);
                }
                //log.debug("resultStr " + resultStr);
            }
        } catch (Exception e) {
            log.error("在线时间查询 Error Exception=", e);
            return new BaseResult("0", "在线时间查询接口异常：" + e.getMessage(), null);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    @Override
    public BaseResult uploadDbBackup() {
        return null;
    }
}
