package cn.ushare.account.admin.portal.controller;

import cn.ushare.account.admin.portal.service.ActionManageService;
import cn.ushare.account.admin.portal.service.HuaweiPortalService;
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
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 【华为Portal接口】
 * （1）微信认证流程：
 *      1. 前端调用预登录接口，临时放行上网，获取微信签名
 *      2. 前端使用微信签名，调用Wechat_GotoRedirect，从浏览器唤起手机微信，进行认证
 *      3. 微信验证签名正确，自动调用authUrl接口，服务器authUrl接口收到后，
 *         更新用户的登录状态为在线，返回200成功状态码
 *      4. 手机微信认证成功，自动跳转认证成功页面
 * （2）员工授权认证流程：
 *      使用员工账号密码认证，但传给portal的userIp为访客的ip
 * （3）常见错误：
 *      1. 短信发送失败：服务器没联网
 *      1. 如果Portal报错“发送认证请求失败”（代码validAckInfo[14] & 0xFF) == 4），
 *         重新设置一遍AC的sharekey后，可以认证成功
 *      2. 超时无响应：ac设备不能ping通
 *      3. 员工授权Portal请求超时无响应：连错了热点，员工和访客连接的热点属于不同AC设备
 *      4. Radius返回密码错误：如果Radis解析出的密码是乱码，往往是sharekey与数据库中不一致，
 *         检查ac设备的sharekey或者重新设置一遍ac设备的sharekey
 *      5. 微信认证点击没反应：没临时放行上网，不能加载微信js
 *      6. Coa强制下线，如果终端关闭了wifi，Coa会失败，终端重新连接热点，再次下线，能成功
 * （4）调试时网络配置：
 *      调试华为设备时，如果portal收不到客户端请求，要将portal服务器使用网线连接，
 *      客户端用wifi热点连接
 *      
 *  【表数据修改流程】
 *  1. authParam数据在getAcParam时新增，认证时更新authMethod、userName、password
 *  2. authRecord、authUser在认证成功后新增，radiusAccountService报文时更新流量、acctSessionId
 */
@Api(tags = "HuaweiPortalController", description = "HuaweiPortal接口")
@RestController
@Slf4j
@RequestMapping("/huawei")
public class HuaweiPortalController {
          
    @Autowired 
    HttpServletRequest request;
    @Autowired 
    HttpServletResponse response;
    @Autowired
    HuaweiPortalService portalService;
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
        log.debug("huawei authPage");
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
        log.debug("huawei getAuthParam method");
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        // 获取ac请求参数，返回认证页或认证失败页面地址
        return portalService.getAuthParam(param);
    }
        
    /**
     * 登录
     */
    @ApiOperation(value="登录", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/login", method={RequestMethod.POST})
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

    @ApiOperation(value="钉钉登录成功回调", notes="")
    @RequestMapping(value="/dingTalkCallback", method={RequestMethod.GET, RequestMethod.POST})
    public void dingTalkCallback() throws Exception {
        BaseResult result = portalService.dingTalkCallback(request);
     
        // 重定向到认证页或认证失败页
        String targetUrl = (String) result.getData();
        response.sendRedirect(targetUrl);
    }
    
    //微信预登录：临时允许上网，返回微信签名
    @ApiOperation(value="微信预登录", notes="")
    @RequestMapping(value="/wxPreLogin", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult wxPreLogin(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        String userAgent = request.getHeader("User-Agent");
        authParam.setTerminalType(BrowseTypeUtil.getTerminalType(userAgent));
        return portalService.wxPrelogin(authParam, request);
    }
    
    //微信登录
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

    @ApiOperation(value="获取钉钉用户信息", notes="")
    @RequestMapping(value="/getDingTalkUserInfo", method={RequestMethod.GET, RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getDingTalkUserInfo(@RequestBody String jsonStr) throws Exception{
        JSONObject jsonObj = JSONObject.fromObject(jsonStr);
        String code = jsonObj.optString("code");
        return portalService.getDingTalkUserInfo(code, request);
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
    public BaseResult checkOnline(@RequestBody String authParamJson) {
        AuthParam authParam = null;
        try {
            authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return portalService.checkOnline(authParam);
    }
    
    
}
