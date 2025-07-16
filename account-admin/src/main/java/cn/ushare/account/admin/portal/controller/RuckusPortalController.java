package cn.ushare.account.admin.portal.controller;

import cn.ushare.account.admin.portal.service.ActionManageService;
import cn.ushare.account.admin.portal.service.RuckusPortalService;
import cn.ushare.account.admin.service.*;
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
 * 【优科Portal接口】
 * （1）Ac设备端配置项检查：
 *      认证页重定向地址：http://ip:port/ruckus
 *      回调地址：http://ip:port/ruckus/loginCallback
 * （1）认证流程：
 *      1. 前端发送登录请求给服务器，服务器验证密码正确后，即返回成功
 *      2. 前端在收到成功后，使用location.href重定向到ac设备自带的登录接口
 *      3. ac收到登录请求后，设备内部自动请求radius，radius验证用户名密码，返回成功
 *      4. ac收到radius返回的成功后，调用验证成功回调接口（该接口由服务器提供，但要填写到ac设备的配置里面）
 *      5. 服务器收到这个回调请求后，跳转登录成功界面
 * （2）Ruckus设备不支持员工授权认证：
 *      因为ac设备登录接口没有ip参数，只能由访客发起request，ac从request中获取ip，不能由员工调用接口传递访客ip
 * （3）微信认证流程：
 *      1. 前端请求getWxPortalAccount，获取微信默认登录账号
 *      2. 调用ac设备的登录接口登录（用于临时放行上网），参数使用默认微信登录账号、密码
 *      3. Portal收到ac设备登录成功回调（loginCallback接口），
 *         加入用户到临时登录列表（1分钟之后没有完成微信认证，则踢下线，但ruckus设备不支持踢下线），
 *         如果userName是微信默认登录账号，则生成微信签名，重定向到认证首页，
 *         浏览器使用微信签名，调用微信js接口Wechat_GotoRedirect，唤起手机微信app，
 *      4. 微信验证签名正确，自动调用authUrl接口，服务器authUrl接口收到后，
 *         更新用户的登录状态为在线，返回200成功状态码
 *      5. 微信收到成功状态码，跳转认证成功页面
 * （4）退出流程：直接由前端调用AC设备上的退出接口
 * （5）常见错误：
 *      1. 超时无响应：ac设备不能ping通
 *      2. Radius返回密码错误：ac设备上的sharedSecret和数据库中的不一致
 *      3. 员工授权Portal请求超时无响应：连错了热点，员工和访客连接的热点属于不同AC设备
 *      4. Coa强制下线，如果终端关闭了wifi，Coa会失败，终端重新连接热点，再次下线，能成功
 * （6）调试时网络配置：
 *      调试优科设备时，客户端和portal服务器要在一个网段，否则无法加载vue界面
 *      （npm run dev时界面不能访问，但是java接口是能访问的，和vue的调试机制有关），
 *      先将portal服务器连接优科热点，获取ip地址，然后配置ac设备中的重定向地址（使用刚刚获取的这个ip），
 *      然后客户端连接相同热点，即可打开vue页面
 * （7）ios调试Ruckus注意：
 *      使用alert或者toast打印日志时，要屏蔽location.reload()再测，否则每次reload导致日志看不到就重新加载了
 *      
 * 【下线通知流程】
 *   1. 终端断开wifi连接，立即能收到下线报文
 *   2. 终端断开wifi连接，管理后台coa强制下线会失败，必须是连接状态才能成功
 */
@Api(tags = "RuckusPortalController", description = "RuckusPortal接口")
@RestController
@Slf4j
@RequestMapping("/ruckus")
public class RuckusPortalController {
          
    @Autowired 
    HttpServletRequest request;
    @Autowired 
    HttpServletResponse response;
    @Autowired
    RuckusPortalService portalService;
    @Autowired
    AcService acService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    ActionManageService actionManageService;
    @Autowired
    AuthParamService authParamService;

    /**
     * 认证页（该接口由ac触发，接口url在ac中设置）
     */
    @ApiOperation(value="认证页", notes="")
    @RequestMapping(value="", method={RequestMethod.GET, RequestMethod.POST})
    public void getAcParam() throws Exception {
        String userAgent = request.getHeader("User-Agent");
        
        // 获取ac请求参数，返回认证页或认证失败页面地址
        BaseResult result = portalService.getAcParam(request);
        
        // 重定向到认证页或认证失败页
        response.sendRedirect((String) result.getData());
    }

    @ApiOperation(value="获取详细参数", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getAuthParam", method={RequestMethod.GET, RequestMethod.POST})
    public BaseResult getAuthParam(@RequestBody String paramJson) throws Exception {
        log.debug("ruckus getAuthParam method");
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
        log.debug("ruckusLogin " + authParam.toString());
        if (authParam.getAuthMethod() == Constant.AuthMethod.MAC_WHITE_LIST_AUTH) {
            // mac白名单登录
            return portalService.macWhiteListLogin(authParam);
        } else {
            return portalService.login(authParam);
        }
    }
    
    /**
     * 认证成功，设备回调接口，该接口由ac设备触发（回调url在ac中配置）
     */
    @ApiOperation(value="认证成功回调", notes="")
    @RequestMapping(value="/loginCallback", method={RequestMethod.GET, RequestMethod.POST})
    public void loginCallback() throws Exception {
        log.debug("ruckus loginCallback");
        BaseResult result = portalService.loginCallback(request);
        response.sendRedirect((String) result.getData());
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
     * 二维码登录
     */
    @ApiOperation(value="二维码登录", notes="")
    @RequestMapping(value="/qrcodeLogin")
    public void qrcodeLogin(String qrcodeSn, String userName, String userPhone, String companyName) throws Exception {
        // 查询userIp
        String userIp = IpUtil.getIpAddr(request);
        BaseResult result = portalService.qrcodeLogin(qrcodeSn, userName, userPhone, companyName, userIp);
        response.sendRedirect((String) result.data);
    }
    
    /**
     * 查询钉钉登录的默认Portal账户、密码
     */
    @ApiOperation(value="查询钉钉登录的默认Portal账户密码", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getDingTalkPortalAccount", method={RequestMethod.POST})
    public BaseResult<AuthParam> getDingTalkPortalAccount(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        String userAgent = request.getHeader("User-Agent");
        authParam.setTerminalType(BrowseTypeUtil.getTerminalType(userAgent));
        return portalService.getDingTalkPortalAccount(authParam);
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
     * 查询微信登录的默认Portal账户、密码
     */
    @ApiOperation(value="查询微信登录的默认Portal账户密码", notes="")
    @RequestMapping(value="/getWxPortalAccount", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthParam> getWxPortalAccount(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        return portalService.getWxPortalAccount(authParam, request);
    }
    
    /**
     * 退出，前端调用ac设备提供的logout接口，这里只需更新下线状态
     */
    @ApiOperation(value="退出", notes="")
    @RequestMapping(value="/logout", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult logout(@RequestBody String authLogoutParamJson) throws Exception {
        AuthLogoutParam authLogoutParam = JsonObjUtils.json2obj(authLogoutParamJson, AuthLogoutParam.class);
        authUserService.updateOfflineState(authLogoutParam.getUserMac());
        return new BaseResult();
    }
    
    /**
     * 退出，radiusCoa方式
     */
    @ApiOperation(value="退出", notes="")
    @RequestMapping(value="/logoutByRadius", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult logoutByRadius(@RequestBody String authLogoutParamJson) throws Exception {
        AuthLogoutParam authLogoutParam = JsonObjUtils.json2obj(authLogoutParamJson, AuthLogoutParam.class);
        BaseResult result = portalService.logoutByRadius(authLogoutParam);// 设备下线
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
