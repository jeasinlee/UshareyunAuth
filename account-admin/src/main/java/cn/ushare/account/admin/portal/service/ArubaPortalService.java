package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.mapper.AuthQrcodeMapper;
import cn.ushare.account.admin.radius.service.RadiusCoaService;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.BrowseTypeUtil;
import cn.ushare.account.util.IpUtil;
import cn.ushare.account.util.MacUtil;
import cn.ushare.account.util.StringUtil;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portal服务
 *
 * @author jixiang.li
 * @email jixiang.li@ushareyun.net
 * @since 2019-03-18
 */
//不要加@Transactional，因为radiusService认证成功后，会用多线程更新相同的表，造成死锁
@Service
@Slf4j
public class ArubaPortalService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AcService acService;
    @Autowired
    IdentityCheckService identityCheckService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    GlobalCache globalCache;
    @Autowired
    AuthQrcodeMapper authQrcodeMapper;
    @Autowired
    AuthTemplateService authTemplateService;
    @Autowired
    AuthBaseTemplateService authBaseTemplateService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    WxConfigService wxConfigService;
    @Autowired
    WhiteListService whiteListService;
    @Autowired
    SsidService ssidService;
    @Autowired
    DingTalkConfigService dingTalkConfigService;
    @Autowired
    AuthParamService authParamService;
    @Autowired
    RadiusCoaService radiusCoaService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    ApService apService;
    @Autowired
    ActionManageService actionManageService;

    @Autowired
    AuthQrcodeService authQrcodeService;
    @Value("${aruba.config.loginUrl}")
    String loginUrl;
    @Value("${aruba.config.logoutUrl}")
    String logoutUrl;

    private static final String DEBUG_TAG = "aruba_service:";

    /**
     * 获取ac请求参数
     *
     * @return：认证页地址
     */
    public BaseResult getAcParam(HttpServletRequest request) throws Exception {
        log.debug("method " + request.getMethod());
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = (String) paramNames.nextElement();
            String value = request.getParameter(param);
            log.debug(DEBUG_TAG , param + ":" + value);
        }
        String cmdCode = request.getParameter("cmd");
        String acIp = request.getParameter("switchip");
        String apMac = request.getParameter("apname");
        if (StringUtil.isNotBlank(apMac)) {
            apMac = apMac.replace("-", "");
            apMac = apMac.toUpperCase();
        }
        String userIp = request.getParameter("ip");
        String userMac = request.getParameter("mac");
        if (StringUtil.isNotBlank(userMac)) {
            userMac = PortalUtil.MacFormat1(userMac);
        }
        String acMac = request.getParameter("NI");  //ac-mac
        String userVisitUrl = request.getParameter("url");
        SystemConfig redirectConfig = systemConfigService.getOne(new QueryWrapper<SystemConfig>().eq("code", "AUTH-DEFAULT-VISIT-URL"));
        if(null!=redirectConfig && redirectConfig.getIsValid()==1){
            userVisitUrl = redirectConfig.getValue();
        }

        String ssid = request.getParameter("essid");
        // 认证失败时，重新跳转到首页，会带失败原因
        String res = request.getParameter("res");
        String msg = request.getParameter("msg");
        if (res != null && res.equals("failed")) {
            log.error("ruckus login failed: " + msg);
        }
        String apIp = null;

        /* ac设备提供的接口，在Portal认证成功后调用，通知ac认证成功
         * 非proxy模式参数：username，password，
         * proxy模式参数：proxy，uip，client_mac，username，password，url，
         * 该值为ruckus固定接口，轻易不修改
         */

        // 如果没有任何参数，则是前端用户直接访问该接口（非ac重定向），查询用户登录状态并返回
        if (StringUtil.isBlank(userIp) && StringUtil.isBlank(userMac)) {
            return identityCheckService.getLoginStatus(request, loginUrl, logoutUrl);
        }

        String frontUrl = hostUrlService.getFrontUrl(request);
        log.debug("frontUrl " + URLDecoder.decode(frontUrl, "utf-8"));

        // 查询basIp对应的ac的认证模板
        BaseResult acResult = acService.getInfoByAcIp(acIp);
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }
        Ac ac = (Ac) acResult.getData();

        // 保存ssid参数
        ssidService.save(ac.getId(), ssid);
        // 保存ap参数
        Ap ap = new Ap();
        ap.setAcId(ac.getId());
        ap.setMac(apMac);
        apService.addOrUpdate(ap);

        // 是否移动端浏览器
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);
        log.debug("getAcParam terminalType " + terminalType);

        // 缓存请求参数，用于“员工授权认证”、“ruckus登录回调”时查询用户登录参数
        AuthParam authParam = new AuthParam();
        authParam.setAcIp(acIp);
        authParam.setAcId(ac.getId());
        authParam.setUserIp(userIp);
        authParam.setUserMac(userMac);
        authParam.setApIp(apIp);
        authParam.setApMac(apMac);
        authParam.setUserVisitUrl(userVisitUrl);
        authParam.setSsid(ssid);
        authParam.setTerminalType(terminalType);
        authParam.setRuckusAcLoginUrl(loginUrl);
        authParam.setRuckusAcLogoutUrl(logoutUrl);
        Integer authParamId = (Integer) authParamService.addOrUpdateByIp(authParam).getData();

        //查询ssid的认证模板和认证方法
        Map<String, Object> ssidMap = new HashMap<>();
        ssidMap.put("name", ssid);
        ssidMap.put("ac_id", ac.getId());
        Ssid ssidModel = (Ssid) ssidService.getInfoByName(ssidMap).data;

        // 组合重定向页面url及参数
        AuthTemplate authTemplate;
        if(null!=ssidModel) {
            authTemplate = ssidModel.getAuthTemplate();

            authParam.setIsEmployee(ssidModel.getIsEmployee());
            authParamService.saveOrUpdate(authParam);
        }else{
            authTemplate = ac.getAuthTemplate();
        }
        String templateUrl;
        if (terminalType != 1) {
            templateUrl = authTemplate.getBaseTemplate().getMobileUrl();// 认证模板页面
        } else {
            templateUrl = authTemplate.getBaseTemplate().getUrl();
        }

        String targetUrl = frontUrl + templateUrl;
        targetUrl += "?id=" + authParamId;
        targetUrl += "&brand=" + ac.getBrand().getCode();

        log.debug(DEBUG_TAG + "targetUrl " + targetUrl);
        log.debug(DEBUG_TAG + "targetUrlDecode " + URLDecoder.decode(targetUrl, "utf-8"));
        return new BaseResult(targetUrl);
    }

    /**
     * 获获取认证参数
     * @param param : {'authParamId':'1','isCallback':'1'}
     * @return
     */
    public BaseResult getAuthParam(Map param) throws Exception{
        Map<String, Object> resMap = new HashMap<>();
        boolean callback = param.containsKey("isCallback") && ("1".equals(param.get("isCallback").toString()) || "2".equals(param.get("isCallback").toString()));
        String employee = param.getOrDefault("employee", "0").toString();
        String check = param.getOrDefault("check", "0").toString();
        AuthParam authParam = authParamService.getById(param.get("authParamId").toString());

        String frontUrl = hostUrlService.getFrontUrl(request);
        BaseResult acResult = acService.getInfoByAcIp(authParam.getAcIp());
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }
        Ac ac = (Ac) acResult.getData();

        //查询ssid的认证模板和认证方法
        Map<String, Object> ssidMap = new HashMap<>();
        ssidMap.put("name", authParam.getSsid());
        ssidMap.put("ac_id", authParam.getAcId());
        ssidMap.put("is_valid", 1);
        Ssid ssidModel = (Ssid) ssidService.getInfoByName(ssidMap).data;
        AuthTemplate authTemplate = ssidModel.getAuthTemplate();

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

        resMap.put("frontUrl", frontUrl);// 要加URLEncoder，否则#会丢失
        resMap.put("serverUrl", hostUrlService.getServerUrl(request));
        resMap.put("acId", ac.getId());
        resMap.put("acAuthMethod", ssidModel.getAuthMethod());
        resMap.put("authTemplateId", authTemplate.getId());
        resMap.put("userIp", authParam.getUserIp());
        resMap.put("userMac", authParam.getUserMac());
        resMap.put("formatUserMac", MacUtil.macFormat(authParam.getUserMac()));
        resMap.put("userVisitUrl", authParam.getUserVisitUrl());
        resMap.put("brandCode" ,ac.getBrand().getCode());// ac设备品牌编码

        resMap.put("ruckusAcLoginUrl", loginUrl);
        resMap.put("ruckusAcLogoutUrl",logoutUrl);

        resMap.put("acIp", ac.getIp());
        resMap.put("acMac", authParam.getAcMac());
        resMap.put("ssid", authParam.getSsid());
        resMap.put("apIp", authParam.getApIp());
        resMap.put("apMac",authParam.getApMac());
        resMap.put("qrcodeLoginSn", qrcodeLoginSn);

        resMap.put("employee", Integer.parseInt(employee));
        resMap.put("check", Integer.parseInt(check));

        String showCode = systemConfigService.getByCode("VALID_CODE");
        resMap.put("showCode", Integer.parseInt(showCode));
        String smsCheck = systemConfigService.getByCode("SMS-CHECK");
        resMap.put("smsCheck", Integer.parseInt(smsCheck));

        //ruckus/aruba/cisco回调刷新
        if(callback){
            resMap.put("needCheckOnline", 1);
            resMap.put("showResult", 1);
            // 查询上网时长
            BaseResult periodResult = identityCheckService.getPermitPeriod(authParam.getAuthMethod());
            Integer permitPeriod = null;
            if (periodResult.getData() != null) {
                permitPeriod = (Integer) periodResult.getData();
            }

            resMap.put("permitPeriod", permitPeriod);
            if(authParam.getUserName().equals("portalDefaultWxAccount")){
                // 查询AC设备对应的微信门店
                WxConfig wxConfig = acService.getWxConfigById(ac.getId());

                resMap.put("wxRedirect", 1);
                resMap.put("gzhName", wxConfig.getName());
                resMap.put("description", wxConfig.getDescription());
            }else if(authParam.getUserName().equals("portalDefaultDingTalkAccount")){
                DingTalkConfig dingTalkConfig = acService.getDingTalkConfigById(ac.getId());

                resMap.put("dingTalkRedirect", 1);
                resMap.put("gzhName", dingTalkConfig.getName());
//                resMap.put("needShowDingTalkQrcode", 1);
            }
        }

        return new BaseResult(resMap);
    }

    /**
     * Aruba登录
     */
    public BaseResult login(AuthParam authParam) throws Exception {
        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        // 手机号码白名单检查
        if (authParam.getAuthMethod() == Constant.AuthMethod.SMS_AUTH) {
            QueryWrapper<WhiteList> queryWrapper = new QueryWrapper();
            queryWrapper.eq("type", 1);// 1手机，2MAC，3IP
            queryWrapper.eq("value", authParam.getPhone());
            queryWrapper.eq("is_valid", 1);
            WhiteList phoneWhiteList = whiteListService.getOne(queryWrapper);
            if (phoneWhiteList != null) {
                log.debug("用户" + authParam.getPhone() + "手机白名单登录");
                // 读取一键登录默认账户
                identityCheckService.oneKeyCheck(authParam);
                authParamService.addOrUpdateByIp(authParam);
                return new BaseResult(authParam);// 返回用户名密码，用于前端登录
            }
        }

        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 图片验证码是否正确（短信登录方式不需要验证图片验证码，因为在发送短信时已经验证）
            if (authParam.getAuthMethod() == Constant.AuthMethod.ACCOUNT_AUTH
                    || authParam.getAuthMethod() == Constant.AuthMethod.EMPLOYEE_AUTH) {
                if (request.getSession().getAttribute("checkCode") == null) {
                    return new BaseResult("0", "验证码失效", null);
                }
                String sessionCode = (String) request.getSession().getAttribute("checkCode");
                if (StringUtils.isBlank(sessionCode)
                        || !sessionCode.equals(authParam.getCheckCode())) {
                    request.getSession().removeAttribute("checkCode");
                    return new BaseResult("0", "验证码错误", null);
                }
                request.getSession().removeAttribute("checkCode");
            }
        }

        // 浏览器类型
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);
        if (null == authParam.getEmployee() || authParam.getEmployee() != 1) {
            authParam.setTerminalType(terminalType);
        }
        log.debug("getAcParam terminalType " + terminalType);
        log.debug(DEBUG_TAG + "authParam.getUserName() " + authParam.getUserName());

        // 查询数据库，验证用户权限，更新authParam参数
        BaseResult result = identityCheckService.checkAuthAndUpdateParam(authParam);
        // 保存登录参数
        authParamService.addOrUpdateByIp(authParam);

        StringBuilder callbackPath = new StringBuilder(hostUrlService.getServerUrl(request) + "/aruba/loginCallback");
        callbackPath.append("?uid=" + authParam.getUserName());
        callbackPath.append("&switchip=" + authParam.getAcIp());
        callbackPath.append("&apmac=" + authParam.getApMac());
        callbackPath.append("&mac=" + authParam.getUserMac());
        callbackPath.append("&uip=" + authParam.getUserIp());
        callbackPath.append("&essid=" + authParam.getSsid());
        callbackPath.append("&url=" + authParam.getUserVisitUrl());

        authParam.setCallbackPath(callbackPath.toString());
        log.debug(DEBUG_TAG + "login method callbackPath: " + callbackPath.toString());

        result.setData(authParam);// 返回用户名密码，用于前端登录
        return result;
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
                targetUrl += "&description=" + URLEncoder.encode(wxConfig.getDescription(), "utf-8");
                targetUrl += "&keyword=" + URLEncoder.encode(wxConfig.getKeyword(), "utf-8");
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

        StringBuilder callbackPath = new StringBuilder(hostUrlService.getServerUrl(request) + "/aruba/loginCallback");
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

        Map<String, Object> ssidMap = new HashMap<>();
        ssidMap.put("name", authParam.getSsid());
        ssidMap.put("ac_id", authParam.getAcId());
        ssidMap.put("is_valid", 1);
        Ssid ssidModel = (Ssid) ssidService.getInfoByName(ssidMap).data;
        AuthTemplate authTemplate = ssidModel.getAuthTemplate();
        if(null == authTemplate){
            authTemplate = ac.getAuthTemplate();
        }
        // 组合重定向页面url及参数
        String templateUrl;
        if (terminalType != 1) {
            templateUrl = authTemplate.getBaseTemplate().getMobileUrl();// 认证模板页面
        } else {
            templateUrl = authTemplate.getBaseTemplate().getUrl();
        }
        String targetUrl = frontUrl + templateUrl;
        targetUrl += "?id=" + authParam.getId().toString();
        targetUrl += "&brand=" + ac.getBrand().getCode();
        targetUrl += "&callback=2";
        targetUrl += "&nickName=" + URLEncoder.encode(nick, "utf-8");

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


    /**
     * mac白名单登录
     */
    public BaseResult macWhiteListLogin(AuthParam authParam) throws Exception {
        log.debug("authParam " + authParam.toString());

        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        // 查询ac是否开通无感知登录
        BaseResult acResult = acService.getInfoByAcIp(authParam.getAcIp());
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            return acResult;
        }
        Ac ac = (Ac) acResult.getData();
        if (ac.getIsWhitelistEnable() != 1) {
            return new BaseResult("0", "控制器没开通无感知登录", null);
        }

        // 检查是否属于mac白名单
        QueryWrapper<WhiteList> macQuery = new QueryWrapper();
        macQuery.eq("type", 2);// 1手机，2MAC，3IP
        macQuery.eq("value", authParam.getUserMac());
        macQuery.eq("is_valid", 1);
        List<WhiteList> macWhiteList = whiteListService.list(macQuery);
        if (CollectionUtils.isEmpty(macWhiteList)) {
            return new BaseResult("0", "MAC地址不属于白名单", null);
        }

        // 浏览器类型
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);
        authParam.setTerminalType(terminalType);
        log.debug("getAcParam terminalType " + terminalType);

        // 读取默认账户
        identityCheckService.oneKeyCheck(authParam);

        // 请求参数有变化，重新保存请求参数
//        authParam.setAuthMethod(Constant.AuthMethod.ACCOUNT_AUTH);// 借用一键登录方式
        authParamService.addOrUpdateByIp(authParam);

        StringBuilder callbackPath = new StringBuilder(hostUrlService.getServerUrl(request)+"/aruba/loginCallback");
        callbackPath.append("?uid=" + authParam.getUserName());
        callbackPath.append("&switchip=" + authParam.getAcIp());
        callbackPath.append("&apmac=" + authParam.getApMac());
        callbackPath.append("&mac=" + authParam.getUserMac());
        callbackPath.append("&uip=" + authParam.getUserIp());
        callbackPath.append("&essid=" + authParam.getSsid());
        callbackPath.append("&url=" + authParam.getUserVisitUrl());

        authParam.setCallbackPath(callbackPath.toString());
        log.debug(DEBUG_TAG + "macwhiteLogin method callbackPath: " + callbackPath.toString());
        // 查询上网时长
        BaseResult result = new BaseResult();
        result.setData(authParam);// 返回用户名密码，用于前端登录

        return result;
    }

    /**
     * 登录成功回调
     */
    public BaseResult loginCallback(HttpServletRequest request) throws Exception {
        log.debug(DEBUG_TAG + "aruba loginCallback");
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);
        log.debug("getAcParam terminalType " + terminalType + " userAgent " + userAgent);

        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = (String) paramNames.nextElement();
            String value = request.getParameter(param);
            log.debug(DEBUG_TAG + param + ":" + value);
        }
        String userName = request.getParameter("uid");// 登录账户
        String acIp = request.getParameter("switchip");
        String apMac = request.getParameter("apmac");
        if (StringUtil.isNotBlank(apMac)) {
            apMac = apMac.toUpperCase();
        }
        String userMac = request.getParameter("mac");
        if (StringUtil.isNotBlank(userMac)) {
            userMac = PortalUtil.RawMacFormat(userMac);
        }
        String formatUserMac = MacUtil.macFormat(userMac);
        String url = request.getParameter("url");
        String ssid = request.getParameter("essid");
        String vlan = request.getParameter("vlan");
        String uip = request.getParameter("uip");

        if(StringUtils.isBlank(uip)){
            uip = IpUtil.getIpAddr(request);
        }
        // 根据userIp查询登录参数记录
        String userIp = uip;
        AuthParam authParam = authParamService.getByUserIp(userIp);
        userName = authParam.getUserName();
        // 如果是微信预登录、钉钉预登录成功回调，则加入临时记录列表
        if (userName.equals("portalDefaultWxAccount")
                || userName.equals("portalDefaultDingTalkAccount")) {
            // 加入临时上网记录表
            globalCache.addOrUpdateTempLogin(authParam);
        }
        if(StringUtils.isBlank(acIp)){
            acIp = authParam.getAcIp();
        }
        if(StringUtils.isBlank(ssid)){
            ssid = authParam.getSsid();
        }

        // 查询basIp对应的ac的认证模板
        String frontUrl = hostUrlService.getFrontUrl(request);
        log.debug("frontUrl " + frontUrl);
        BaseResult acResult = acService.getInfoByAcIp(acIp);
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }

        // 组合重定向页面url及参数
        Ac ac = (Ac) acResult.getData();
        //查询ssid的认证模板和认证方法
        Map<String, Object> ssidMap = new HashMap<>();
        ssidMap.put("name", ssid);
        ssidMap.put("ac_id", ac.getId());
        Ssid ssidModel = (Ssid) ssidService.getInfoByName(ssidMap).data;

        AuthTemplate authTemplate;
        if(null!=ssidModel) {
            authTemplate = ssidModel.getAuthTemplate();
        }else{
            authTemplate = ac.getAuthTemplate();
        }
        // 是否移动端浏览器
        log.debug("getAcParam terminalType " + terminalType);

        // 组合重定向页面url及参数
        String templateUrl;
        if (terminalType != 1) {
            templateUrl = authTemplate.getBaseTemplate().getMobileUrl();// 认证模板页面
        } else {
            templateUrl = authTemplate.getBaseTemplate().getUrl();
        }

        String callbackUrl = frontUrl + "/" + ac.getBrand().getCode() + "/dingTalkCallback";
        String targetUrl = null;

        // 基本参数
        targetUrl = frontUrl + templateUrl;
        targetUrl += "?id=" + authParam.getId().toString();
        targetUrl += "&brand=" + ac.getBrand().getCode();
        targetUrl += "&callback=1";
        if(authParam.getAuthMethod() == Constant.AuthMethod.DING_TALK_AUTH) {
            DingTalkConfig dingTalkConfig = dingTalkConfigService.getById(ac.getDingTalkConfigId());
            if (dingTalkConfig == null) {// 返回重定向地址：错误提示页面
                String errUrl = frontUrl + "/portal/authEmptyErr";
                errUrl += "?errMsg=" + URLEncoder.encode("钉钉认证缺少配置", "utf-8");
                log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
                return new BaseResult("0", "钉钉认证缺少配置", errUrl);
            }
            targetUrl += "&authUrl=" + dingTalkConfig.getAuthUrl();   //手机端打开页面地址
            targetUrl += "&appId=" + dingTalkConfig.getAppId();
            targetUrl += "&callbackUrl=" + callbackUrl;   //钉钉二维码扫码授权地址
        } else if(authParam.getAuthMethod() == Constant.AuthMethod.WX_AUTH){
            WxConfig wxConfig = wxConfigService.getById(ac.getWxShopConfigId());
            if (wxConfig == null) {// 返回重定向地址：错误提示页面
                String errUrl = frontUrl + "/portal/authEmptyErr";
                errUrl += "?errMsg=" + URLEncoder.encode("微信认证缺少配置", "utf-8");
                log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
                return new BaseResult("0", "微信认证缺少配置", errUrl);
            }
            targetUrl += "&keyword=" + URLEncoder.encode(wxConfig.getKeyword(), "utf-8");
            targetUrl += "&description=" + URLEncoder.encode(wxConfig.getDescription(), "utf-8");
            targetUrl += "&gzhName=" + URLEncoder.encode(wxConfig.getName(), "utf-8");
            targetUrl += "&appId=" + wxConfig.getAppId();
        }

        //查询是否开通行为管理
        BaseResult actionResult = licenceService.checkActionConfig();
        if("1".equals(actionResult.getReturnCode())){
            actionManageService.login(authParam);
        }

        log.debug(DEBUG_TAG + "loginCallback targetUrl " + URLDecoder.decode(targetUrl, "utf-8"));
        return new BaseResult(targetUrl);
    }


    /**
     * 二维码登录
     */
    public BaseResult qrcodeLogin(String qrcodeSn, String userName, String userPhone, String companyName, String userIp) throws Exception {
        String frontUrl = hostUrlService.getFrontUrl(request);
        // 参数空检查
        if (qrcodeSn == null) {
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode("二维码编号缺失", "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", "二维码编号缺失", errUrl);
        }
        // 二维码是否有效
        AuthQrcode authQrcode = authQrcodeMapper.getValidCode(qrcodeSn);
        if (authQrcode == null) {
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode("二维码无效", "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", "二维码无效", errUrl);
        }

        //需要补充用户信息
        if(authQrcode.getSupplyUserInfo()==1){
            if (StringUtils.isAnyBlank(userName, userPhone, companyName)){
                String redirectUrl = frontUrl + "/qrcodeUserinfo?sn=" + qrcodeSn;
                redirectUrl += "&brandCode=aruba";
                log.debug("redirectUrl: " + URLDecoder.decode(redirectUrl, "utf-8"));
                return new BaseResult("0", "需要补充用户信息", redirectUrl);
            }
        }

        // 查询ac参数
        Ac ac = acService.getById(authQrcode.getAcId());
        BaseResult acResult = acService.getInfoByAcIp(ac.getIp());
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }
        ac = (Ac) acResult.getData();

        AuthParam authParam = new AuthParam();
        authParam.setAuthMethod(Constant.AuthMethod.QRCODE_AUTH);
        authParam.setQrcodeSn(qrcodeSn);// 传入qrcodeSn
        authParam.setAcIp(ac.getIp());
        authParam.setAcId(ac.getId());
        authParam.setUserIp(userIp);

        if(authQrcode.getSupplyUserInfo()==1) {
            //更新用户名称
            authParam.setGuestName(userName);
        }
        // 保存authParam
        authParamService.addOrUpdateByIp(authParam);

        BaseResult result = login(authParam);
        if (result.getReturnCode().equals("0")) {
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(result.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", result.getReturnMsg(), errUrl);
        }

        String directLoginUrl = loginUrl + "?user=" + authParam.getUserName()
                + "&password=" + authParam.getPassword() + "&opcode=cp_auth" + "&orig_url=" + ((AuthParam) result.data).getCallbackPath();
        return new BaseResult(directLoginUrl);
    }


    /**
     * 退出（使用Radius命令）
     */
    public BaseResult logoutByRadius(AuthLogoutParam authLogoutParam) {
        log.debug("authLogoutParam " + authLogoutParam.toString());
        String acIp = authLogoutParam.getAcIp();
        String userIp = authLogoutParam.getUserIp();
        String userMac = authLogoutParam.getUserMac();

        // 只能根据userIp查acct-session-id，不能根据userMac查，因为二维码认证时，不经过ac重定向，userMac为空，记录为000000000000
        AuthRecord authRecord = authRecordService.getTopOne(userIp);
        String callingSessionId = MacUtil.macFormat(authRecord.getMac());

        // 查询basIp所属ac的配置参数
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("is_valid", 1);
        acQuery.eq("ip", acIp);
        Ac ac = acService.getOne(acQuery);
        if (ac == null) {
            return new BaseResult("0", "该AC设备未登记", null);
        }
        Integer timeout = ac.getExpireTime();
        String sharedSecret = ac.getShareKey();
        BaseResult result = radiusCoaService.requestCoaForAruba(timeout, acIp, 3799, userIp,
                authRecord.getUserName(), sharedSecret, callingSessionId);
        if ("-2".equals(result.getReturnCode())) {
            result = radiusCoaService.requestCoaForAruba(timeout, acIp, 3799, userIp,
                    authRecord.getMac(), sharedSecret, callingSessionId);
            result.setReturnCode("0");
        }

        return result;
    }

    /**
     * 查询在线状态
     */
    public BaseResult checkOnline(AuthParam authParam) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("mac", authParam.getUserMac());
        queryWrapper.eq("is_valid", 1);
        AuthUser authUser = authUserService.getOne(queryWrapper);
        if (authUser == null) {
            queryWrapper = new QueryWrapper();
            queryWrapper.eq("ip", IpUtil.getIpAddr(request));
            queryWrapper.eq("is_valid", 1);
            authUser = authUserService.getOne(queryWrapper);
            if(null == authUser) {
                return new BaseResult("1", "0", null);
            }
        }

        if (null != globalCache.getTempLogin(authParam.getUserIp())) {
            return new BaseResult("1", "0", null);
        }

        // 允许上网时长，单位分钟
        Integer permitPeriod = null;
        BaseResult permitPeriodResult = identityCheckService.getPermitPeriod(authUser.getAuthMethod());
        Map<String, Object> map = new HashMap<>();
        if (permitPeriodResult.getReturnCode().equals("1")) {
            if (permitPeriodResult.getData() != null) {
                permitPeriod = (Integer) permitPeriodResult.getData();
            }
            map.put("permitPeriod", permitPeriod);
            map.put("authMethod", authUser.getAuthMethod());
            map.put("userName", authUser.getUserName());
            map.put("guestName", authUser.getShowUserName());
            map.put("phone", authUser.getPhone());

            // 查询是否是首次登录
            Employee employee = employeeService.getOne(new QueryWrapper<Employee>().eq("user_name", authUser.getUserName()));
            if (null != employee && 1 == employee.getIsModify()) {
                map.put("isModify", 1);
                map.put("isFinish", employee.getIsFinish());
                // 查看用户是否是第一次登录
                int countLogins = authRecordService.count(new QueryWrapper<AuthRecord>().
                        eq("user_name", authUser.getUserName()));
                if (countLogins == 1) {
                    map.put("isFirstLogin", 1);
                } else {
                    map.put("isFirstLogin", 0);
                }
            }
        }

        return new BaseResult("1", authUser.getOnlineState() + "", map);
    }

}
