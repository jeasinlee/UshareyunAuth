package cn.ushare.account.admin.portal.controller;

import cn.ushare.account.admin.portal.service.ActionManageService;
import cn.ushare.account.admin.portal.service.TplinkPortalService;
import cn.ushare.account.admin.service.AuthParamService;
import cn.ushare.account.admin.service.AuthQrcodeService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Constant;
import cn.ushare.account.util.BrowseTypeUtil;
import cn.ushare.account.util.IpUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 【新华三Portal接口】
 * 1. 流程参考华为
 * 2. 常见错误：portalVersion使用2.0版本时，登录请求无响应，使用1.0版本时正常，此时重新设置ac的共享密钥后，接口响应正常，原因未知。
 * 3. Coa强制下线，如果终端关闭了wifi，Coa会失败，终端重新连接热点，再次下线，能成功
 *
 * 【下线通知流程】
 *   1. 终端断开wifi连接，不能收到下线报文，此时终端再次连接wifi，不会弹出认证页面，能继续上网；
 *   2. 终端断开wifi长时间之后，再次连接wifi，会弹出认证页面
 *   3. 终端断开wifi连接，管理后台能coa强制下线成功
 *
 * 常见错误：参考华为
 */
@Api(tags = "TplinkPortalController", description = "TplinkPortal接口")
@RestController
@Slf4j
@RequestMapping("/tplink")
public class TplinkPortalController {

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    TplinkPortalService portalService;
    @Autowired
    AuthQrcodeService authQrcodeService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    ActionManageService actionManageService;
    @Autowired
    AuthParamService authParamService;

    /**
     * 认证页（该接口由ac设备调用，url在ac设备中设置）
     */
    @ApiOperation(value="认证页", notes="")
    @RequestMapping(value="", method={RequestMethod.GET, RequestMethod.POST})
    public void getAcParam() throws Exception {
        log.debug("tplink authPage");
        // 获取ac请求参数，返回认证页或认证失败页面地址
        BaseResult result = portalService.getAcParam(request);

        // 重定向到认证页或认证失败页
        String targetUrl = (String) result.getData();
        response.sendRedirect(targetUrl);
    }

    @ApiOperation(value="获取详细参数", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getAuthParam", method={RequestMethod.GET, RequestMethod.POST})
    public BaseResult getAuthParam(@RequestBody String paramJson) throws Exception {
        log.debug("tplink getAuthParam method");
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        // 获取ac请求参数，返回认证页或认证失败页面地址
        return portalService.getAuthParam(param);
    }

    /**
     * 登录
     */
    @ApiOperation(value="登录", notes="")
    @RequestMapping(value="/login", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult login(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        if (authParam.getAuthMethod() == Constant.AuthMethod.MAC_WHITE_LIST_AUTH) {
            // mac白名单登录
            return portalService.macWhiteListLogin(authParam);
        } else {
            return portalService.login(authParam);
        }
    }

    /**
     * 二维码登录
     */
    @ApiOperation(value="二维码登录", notes="")
    @RequestMapping(value="/qrcodeLogin", method={RequestMethod.GET})
    public void qrcodeLogin(String qrcodeSn, String userName, String userPhone, String companyName) throws Exception {
        // 查询userIp
        String userIp = IpUtil.getIpAddr(request);
        BaseResult result = portalService.qrcodeLogin(qrcodeSn, userName, userPhone, companyName, userIp);
        response.sendRedirect((String) result.getData());
    }

    /**
     * 钉钉预登录：临时允许上网
     */
    @ApiOperation(value="钉钉预登录", notes="")
    @RequestMapping(value="/dingTalkPreLogin", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult dingTalkPreLogin(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        String userAgent = request.getHeader("User-Agent");
        authParam.setTerminalType(BrowseTypeUtil.getTerminalType(userAgent));
        return portalService.dingTalkPrelogin(authParam);
    }

    //钉钉登录
    @ApiOperation(value="钉钉登录", notes="")
    @RequestMapping(value="/dingTalkLogin", method={RequestMethod.GET, RequestMethod.POST})
    public void dingTalkLogin() throws Exception {
        BaseResult result = portalService.dingTalkLogin(request);
        if (result.getReturnCode().equals("1")) {
            response.sendRedirect((String) result.data);
        } else {
            response.setStatus(500);
        }
    }

    /**
     * 钉钉登录成功回调
     */
    @ApiOperation(value="钉钉登录成功回调", notes="")
    @RequestMapping(value="/dingTalkCallback", method={RequestMethod.GET, RequestMethod.POST})
    public void dingTalkCallback() throws Exception {
        BaseResult result = portalService.dingTalkCallback(request);

        // 重定向到认证页或认证失败页
        String targetUrl = (String) result.getData();
        response.sendRedirect(targetUrl);
    }

    /**
     * 微信预登录：临时允许上网，返回微信签名
     */
    @ApiOperation(value="微信预登录", notes="")
    @RequestMapping(value="/wxPreLogin", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult wxPreLogin(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        String userAgent = request.getHeader("User-Agent");
        authParam.setTerminalType(BrowseTypeUtil.getTerminalType(userAgent));
        return portalService.wxPrelogin(authParam, request);
    }

    /**
     * 微信登录
     */
    @ApiOperation(value="微信登录", notes="")
    @RequestMapping(value="/wxLogin", method={RequestMethod.GET, RequestMethod.POST})
    public void wxLogin() throws Exception {
        BaseResult result = portalService.wxLogin(request);
        if (result.getReturnCode().equals("1")) {
            response.sendRedirect((String) result.data);
        } else {
            response.setStatus(500);
        }
    }

    /**
     * 退出
     */
    @ApiOperation(value="退出", notes="")
    @RequestMapping(value="/logout", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult logout(@RequestBody String authLogoutParamJson) throws Exception {
        AuthLogoutParam authLogoutParam = JsonObjUtils.json2obj(authLogoutParamJson, AuthLogoutParam.class);
        BaseResult result = portalService.logout(authLogoutParam);// 设备下线
        if (result.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
            result = portalService.logoutByRadius(authLogoutParam);
        }
        if (result.getReturnCode().equals("1")) {// 更新在线记录状态
            authUserService.updateOfflineState(authLogoutParam.getUserMac());
        }

        //查询是否开通行为管理
        BaseResult actionResult = licenceService.checkActionConfig();
        if("1".equals(actionResult.getReturnCode())){
            AuthParam authParam = authParamService.getByUserMac(authLogoutParam.getUserMac());
            actionManageService.logout(authParam);
        }

        return result;
    }

    /**
     * 查询在线状态
     */
    @ApiOperation(value="查询在线状态", notes="")
    @RequestMapping(value="/checkOnline", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult checkOnline(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        return portalService.checkOnline(authParam);
    }

}
