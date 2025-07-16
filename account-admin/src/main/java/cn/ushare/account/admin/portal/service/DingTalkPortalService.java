package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.BrowseTypeUtil;
import cn.ushare.account.util.IpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.request.OapiUserGetuserinfoRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

//钉钉认证相关
/*
 * @author jixiang.li
 * @date 2019-07-02
 * @email jixiang.li@ushareyun.net
 */

@Service
@Transactional
@Slf4j
public class DingTalkPortalService {

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
    @Autowired
    DingTalkConfigService dingTalkConfigService;
    @Autowired
    AuthQrcodeService authQrcodeService;

    //钉钉预登录：临时允许上网
    public BaseResult dingTalkPrelogin(AuthParam authParam) throws Exception {
        log.debug("dingTalkPrelogin authParam " + authParam.toString());

        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        // 认证方式是否开通
        BaseResult result = identityCheckService.isAuthMethodOpen(
                Constant.AuthMethod.DING_TALK_AUTH, authParam.getAcIp(), 1);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultDingTalkAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());

        // 更新authParam的“登录方式”字段，否则后续RadiusService中查询authMethod失败
        authParam.setAuthMethod(Constant.AuthMethod.DING_TALK_AUTH);
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

        DingTalkConfig dingTalkConfig = acService.getDingTalkConfigById(authParam.getAcId());

        HashMap<String, String> reqData = new HashMap<String, String>();
        reqData.put("userName", defaultAccount.getUserName());
        reqData.put("password", defaultAccount.getPassword());
        reqData.put("permitPeriod", permitPeriod + "");// 上网时长
        reqData.put("authMethod", authParam.getAuthMethod().toString());

        reqData.put("gzhName", dingTalkConfig.getName());

        return new BaseResult(reqData);
    }

    //登录的默认Portal账户、密码
    public BaseResult getDingTalkPortalAccount(AuthParam authParam) throws Exception {
        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        BaseResult result = identityCheckService.isAuthMethodOpen(
                Constant.AuthMethod.DING_TALK_AUTH, authParam.getAcIp(), 1);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        // 查询微信登录默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultDingTalkAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 更改入参用户名、密码
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());

        // 更新authParam的“登录方式”字段，否则后续RadiusService中查询authMethod失败
        authParam.setAuthMethod(Constant.AuthMethod.DING_TALK_AUTH);
        authParamService.addOrUpdateByIp(authParam);

        return new BaseResult("1", "成功", authParam);
    }

    public BaseResult getDingTalkUserInfo(String code, HttpServletRequest request) {
        String userIp = IpUtil.getIpAddr(request);
        String accessToken = null;

        // 查询basIp所属ac的配置参数
        QueryWrapper<AuthParam> wrapper = new QueryWrapper();
        wrapper.eq("user_ip", userIp);
        AuthParam authParam = authParamService.getOne(wrapper);
        if (null != authParam) {
            DingTalkConfig dingTalkConfig = acService.getDingTalkConfigById(authParam.getAcId());
            if (null != dingTalkConfig) {
                accessToken = dingTalkConfig.getToken();
            }
        }

        OapiUserGetResponse infoResponse = null;
        try {
            DingTalkClient uidClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
            OapiUserGetuserinfoRequest uidRequest = new OapiUserGetuserinfoRequest();
            uidRequest.setCode(code);
            uidRequest.setHttpMethod("GET");
            OapiUserGetuserinfoResponse uidResponse = uidClient.execute(uidRequest, accessToken);
            String userId = uidResponse.getUserid();

            DingTalkClient infoClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
            OapiUserGetRequest infoRequest = new OapiUserGetRequest();
            infoRequest.setUserid(userId);
            infoRequest.setHttpMethod("GET");
            infoResponse = infoClient.execute(infoRequest, accessToken);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nickName", infoResponse.getNickname());

        return new BaseResult(result);
    }

    //成功回调
    public BaseResult dingTalkCallback(HttpServletRequest request) throws Exception {
        // 解析参数
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = (String) paramNames.nextElement();
            String value = request.getParameter(param);
            log.debug(param + ":" + value);
        }
        String code = request.getParameter("code");
        String userIp = request.getParameter("userIp");
        String acIp = request.getParameter("acIp");

        String frontUrl = hostUrlService.getFrontUrl(request);

        // 查询acIp对应的ac的认证模板
        BaseResult acResult = acService.getInfoByAcIp(acIp);
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }
        Ac ac = (Ac) acResult.getData();

        DingTalkConfig dingTalkConfig = dingTalkConfigService.getById(ac.getDingTalkConfigId());
        if (dingTalkConfig == null) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode("钉钉认证缺少配置", "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", "钉钉认证缺少配置", errUrl);
        }

        OapiSnsGetuserinfoBycodeResponse resp = null;
        try {
            DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/sns/getuserinfo_bycode");
            OapiSnsGetuserinfoBycodeRequest req = new OapiSnsGetuserinfoBycodeRequest();
            req.setTmpAuthCode(code);
            resp = client.execute(req, dingTalkConfig.getAppId(), dingTalkConfig.getAppSecret());
        } catch (ApiException e) {
            e.printStackTrace();
            log.error("error ", e);
        }

        String openId = null;
        String unionId = null;
        String nick = null;
        if (resp != null) {
            log.debug("dingTalk resp " + resp.toString());
            // 失败处理
            if (resp.getErrcode() != 0) {
                String errUrl = frontUrl + "/portal/authEmptyErr";
                errUrl += "?errMsg=" + URLEncoder.encode(resp.getErrmsg(), "utf-8");
                log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
                return new BaseResult("0", resp.getErrmsg(), errUrl);
            }
            openId = resp.getUserInfo().getOpenid();
            unionId = resp.getUserInfo().getUnionid();
            nick = resp.getUserInfo().getNick();
        } else {
            log.debug("dingTalk resp null");
        }

        // 根据userIp查询登录参数记录
        AuthParam authParam = authParamService.getByUserIp(userIp);

        // 登录成功，保存OpenId
        if (authParam != null) {
            AuthRecord authRecord = authRecordService.getTopOne(userIp);
            authRecord.setAuthMethod(authParam.getAuthMethod());
            authRecord.setDingTalkOpenId(openId);
            authRecord.setDingTalkUnionId(unionId);
            authRecord.setDingTalkNick(nick);
            authRecordService.updateById(authRecord);

            AuthUser authUser = new AuthUser();
            authUser.setMac(authRecord.getMac());
            authUser.setDingTalkOpenId(openId);
            authUser.setDingTalkUnionId(unionId);
            authUser.setDingTalkNick(nick);
            authUser.setAuthMethod(authParam.getAuthMethod());
            authUserService.updateByMac(authUser);
        }

        // 删除临时登录记录
        globalCache.removeTempLogin(userIp);

        // 组合重新跳转登录页的参数
        // ac默认二维码认证图片
        QueryWrapper<AuthQrcode> qrcodeQuery = new QueryWrapper();
        qrcodeQuery.eq("ac_id", ac.getId());
        qrcodeQuery.eq("is_default", 1);
        qrcodeQuery.eq("is_valid", 1);
        AuthQrcode authQrcode = authQrcodeService.getOne(qrcodeQuery);
        String qrcodeLoginSn = null;
        if (authQrcode != null) {
            qrcodeLoginSn = authQrcode.getSn();
        }

        // 是否移动端浏览器
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);
        log.debug("getAcParam terminalType " + terminalType);

        // 组合重定向页面url及参数
        AuthTemplate authTemplate = ac.getAuthTemplate();
        String templateUrl;
        if (terminalType != 1) {
            templateUrl = authTemplate.getBaseTemplate().getMobileUrl();// 认证模板页面
        } else {
            templateUrl = authTemplate.getBaseTemplate().getUrl();
        }
        String targetUrl = frontUrl + templateUrl;
        targetUrl += "?id=" + authParam.getId().toString();
        targetUrl += "&brand=" + ac.getBrand().getCode();
        targetUrl += "&callback=1";

        return new BaseResult(targetUrl);
    }

    //钉钉客户端发起的认证请求（钉钉打开并且联网成功，才会调用这个接口）
    public BaseResult dingTalkLogin(HttpServletRequest request) throws Exception {
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
        String targetUrl = frontUrl + "/dingTalkResult";

        // 微信登录成功，保存wxOpenId
        if (null != authParam) {
            AuthRecord authRecord = authRecordService.getTopOne(userIp);
            authRecord.setAuthMethod(authParam.getAuthMethod());
            authRecord.setDingTalkOpenId(openId);
            authRecord.setDingTalkNick(tid);
            authRecord.setDingTalkUnionId(tid);
            authRecordService.updateById(authRecord);

            AuthUser authUser = new AuthUser();
            authUser.setMac(authRecord.getMac());
            authUser.setDingTalkOpenId(openId);
            authUser.setDingTalkNick(tid);
            authUser.setDingTalkUnionId(tid);
            authUser.setAuthMethod(authParam.getAuthMethod());
            authUserService.updateByMac(authUser);

            // 删除临时登录记录
            globalCache.removeTempLogin(userIp);

            DingTalkConfig dingTalkConfig = acService.getDingTalkConfigById(authParam.getAcId());
            if (null != dingTalkConfig) {
                //更新token
                DefaultDingTalkClient tokenClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
                OapiGettokenRequest tokenRequest = new OapiGettokenRequest();
                tokenRequest.setAppkey(dingTalkConfig.getAppId());
                tokenRequest.setAppsecret(dingTalkConfig.getAppSecret());
                tokenRequest.setHttpMethod("GET");
                OapiGettokenResponse response = tokenClient.execute(tokenRequest);

                dingTalkConfig.setToken(response.getAccessToken());
                dingTalkConfigService.saveOrUpdate(dingTalkConfig);

                targetUrl += "?gzhName=" + URLEncoder.encode(dingTalkConfig.getName(), "utf-8");
                targetUrl += "&corpId=" + dingTalkConfig.getCorpId();
            } else {
                targetUrl += "?isError=1";
            }
        } else {
            targetUrl += "?isError=1";
        }

        log.debug("dingTalkResultUrl:" + targetUrl);

        return new BaseResult(targetUrl);
    }

}
