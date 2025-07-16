package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.IpUtil;
import cn.ushare.account.util.MacUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;

//微信认证相关
/*
 * @author jixiang.li
 * @date 2019-07-02
 * @email jixiang.li@ushareyun.net
 */

@Service
@Transactional
@Slf4j
public class WxPortalService {

    @Autowired
    LicenceService licenceService;
    @Autowired
    IdentityCheckService identityCheckService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    AuthParamService authParamService;
    @Autowired
    PortalApiService portalApiService;
    @Autowired
    GlobalCache globalCache;
    @Autowired
    AcService acService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    AuthRecordService authRecordService;


    //微信预登录：临时允许上网，返回微信签名
    public BaseResult wxPrelogin(AuthParam authParam, HttpServletRequest request) throws Exception {
        log.debug("wxPrelogin authParam " + authParam.toString());
        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        // 认证方式是否开通
        BaseResult result = identityCheckService.isAuthMethodOpen(
                Constant.AuthMethod.WX_AUTH, authParam.getAcIp(), 1);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultWxAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());

        // 更新authParam的“登录方式”字段，否则后续RadiusService中查询authMethod失败
        authParam.setAuthMethod(Constant.AuthMethod.WX_AUTH);
        authParamService.addOrUpdateByIp(authParam);

        // Portal协议登录，放行设备，调试时也要放行，否则会读不到微信的js
        BaseResult loginResult = portalApiService.portalLogin(authParam);
        if (loginResult.getReturnCode().equals("0")) {
            return loginResult;
        }

        // 加入临时上网记录表
        globalCache.addOrUpdateTempLogin(authParam);

        // 查询上网时长
        BaseResult periodResult = identityCheckService.getPermitPeriod(authParam.getAuthMethod());
        Integer permitPeriod = null;
        if (periodResult.getData() != null) {
            permitPeriod = (Integer) periodResult.getData();
        }

        // 查询AC设备对应的微信门店
        WxConfig wxConfig = acService.getWxConfigById(authParam.getAcId());
        // 生成微信签名
        String appId = wxConfig.getAppId();
        // extend只允许英文和数字，不超过300字符，
        // 也不能传json串（转换成base64的也不行），会造成auth_url调用时参数解析异常
        String extend = authParam.getUserIp();// 传userIp过去，不含冒号的格式
        String timeStamp = System.currentTimeMillis() + "";
        String shopId = wxConfig.getShopId();
//        String authUrl = hostUrlService.getServerUrl(request) + "/huawei/wxLogin";
//        log.debug("authUrl " + authUrl);
        String userMac = authParam.getUserMac();
        String formatUserMac = MacUtil.macFormat(userMac);
        String ssid = authParam.getSsid();
        String rawData = appId + extend + timeStamp + shopId
                + formatUserMac + ssid + wxConfig.getAppSecret();
        String sign = DigestUtils.md5Hex(rawData);
        HashMap<String, String> reqData = new HashMap<String, String>();
        reqData.put("appId", appId);
        reqData.put("extend", extend);
        reqData.put("timeStamp", timeStamp);
        reqData.put("shopId", shopId);
//        reqData.put("authUrl", authUrl);
        reqData.put("userMac", userMac);
        reqData.put("formatUserMac", formatUserMac);
        reqData.put("ssid", ssid);
        reqData.put("sign", sign);
        reqData.put("userName", defaultAccount.getUserName());
        reqData.put("password", defaultAccount.getPassword());
        reqData.put("permitPeriod", permitPeriod + "");// 上网时长
        reqData.put("authMethod", authParam.getAuthMethod().toString());

        reqData.put("gzhName", wxConfig.getName());

        log.debug("return map " + reqData.toString());
        return new BaseResult(reqData);
    }

    //微信端发起的认证请求（微信打开并且联网成功，才会调用这个接口）
    public BaseResult wxLogin(HttpServletRequest request) throws Exception {
        // 解析参数
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = (String) paramNames.nextElement();
            String value = request.getParameter(param);
            log.debug(param + ":" + value);
        }
        String openId = request.getParameter("openId");
        String extend = request.getParameter("extend");
        String tid = request.getParameter("tid");

        // 根据userIp查询登录参数记录
        String userIp = IpUtil.getIpAddr(request);
        AuthParam authParam = authParamService.getByUserIp(userIp);

        String frontUrl = hostUrlService.getFrontUrl(request);
        String targetUrl = frontUrl + "/wxResult";
        // 微信登录成功，保存wxOpenId
        if (authParam != null) {
            AuthRecord authRecord = authRecordService.getTopOne(userIp);
            authRecord.setAuthMethod(authParam.getAuthMethod());
            authRecord.setWxOpenId(openId);
            authRecord.setWxTid(tid);
            authRecordService.updateById(authRecord);

            AuthUser authUser = new AuthUser();
            authUser.setMac(authRecord.getMac());
            authUser.setWxOpenId(openId);
            authUser.setWxTid(tid);
            authUser.setAuthMethod(authParam.getAuthMethod());
            authUserService.updateByMac(authUser);
            // 删除临时登录记录
            globalCache.removeTempLogin(userIp);

            WxConfig wxConfig = acService.getWxConfigById(authParam.getAcId());
            if (null != wxConfig) {
                targetUrl += "?gzhName=" + URLEncoder.encode(wxConfig.getName(), "utf-8");
            } else {
                targetUrl += "?isError=1";
            }
        } else {
            targetUrl += "?isError=1";
        }

        log.debug("wxResultUrl:" + targetUrl);

        return new BaseResult(targetUrl);
    }


    //查询微信登录的默认Portal账户、密码
    public BaseResult getWxPortalAccount(AuthParam authParam, HttpServletRequest request) throws Exception {
        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        BaseResult result = identityCheckService.isAuthMethodOpen(
                Constant.AuthMethod.WX_AUTH, authParam.getAcIp(), 1);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        // 查询微信登录默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultWxAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 更改入参用户名、密码
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());

        // 更新authParam的“登录方式”字段，否则后续RadiusService中查询authMethod失败
        authParam.setAuthMethod(Constant.AuthMethod.WX_AUTH);
        authParamService.addOrUpdateByIp(authParam);

        StringBuilder callbackPath = new StringBuilder(hostUrlService.getServerUrl(request) + "/ruckus/loginCallback");
        callbackPath.append("?uid=" + authParam.getUserName());
        callbackPath.append("&switchip=" + authParam.getAcIp());
        callbackPath.append("&apmac=" + authParam.getApMac());
        callbackPath.append("&mac=" + authParam.getUserMac());
        callbackPath.append("&uip=" + authParam.getUserIp());
        callbackPath.append("&essid=" + authParam.getSsid());
        callbackPath.append("&url=" + authParam.getUserVisitUrl());

        authParam.setCallbackPath(callbackPath.toString());

        return new BaseResult("1", "成功", authParam);
    }
}
