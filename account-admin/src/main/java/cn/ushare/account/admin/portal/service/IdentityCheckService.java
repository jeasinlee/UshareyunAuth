package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.ApiReqParam;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 登录用户身份验证
 *
 * @author jixiang.li
 * @date 2019-03-18
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class IdentityCheckService {

    @Autowired
    EmployeeService employeeService;
    @Autowired
    AuthQrcodeService authQrcodeService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    OnlinePolicyService onlinePolicyService;
    @Autowired
    AuthMethodService authMethodService;
    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    AcService acService;
    @Autowired
    SmsRecordService smsRecordService;
    @Autowired
    SystemConfigService configService;
    @Autowired
    BlackListService blackListService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    BandwidthService bandwidthService;
    @Autowired
    SmsConfigService smsConfigService;
    @Autowired
    SsidService ssidService;

    @Autowired
    @Qualifier("ldapService")
    AdService adService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    LicenceCache licenceCache;
    @Autowired
    AccountUserService accountUserService;
    @Autowired
    AccountUserLockedService userLockedService;
    @Autowired
    AccountChargeRecordService userDebtService;


    /**
     * 用户身份认证，并更新认证参数（比如一键认证，则填入默认用户名、密码）
     */
    public BaseResult checkAuthAndUpdateParam(AuthParam authParam) {
        int authMethod = authParam.getAuthMethod();

        // 认证方式是否开通
        BaseResult result = isAuthMethodOpen(authMethod, authParam.getAcIp(),
                (null!=authParam.getTerminalType() && authParam.getTerminalType() == 1) ? 0 : ((null!=authParam.getEmployee() && authParam.getEmployee()==1)?1:0));
        if (result.getReturnCode().equals("0")) {
            return result;
        }
        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        Integer isAccount = null == licenceInfo ? 0 : licenceInfo.getIsAccount();
        //普通模式
        if(isAccount == 0) {
            // 是否黑名单
            result = checkBlackList(authParam);
            if (result.getReturnCode().equals("0")) {
                return result;
            }

            // 是否处于上网时段
            result = inOnlinePeriod(authMethod);
            if (result.getReturnCode().equals("0")) {
                return result;
            }

            // 原本逻辑：员工授权认证（先注释）
/*        if (authMethod == Constant.AuthMethod.EMPLOYEE_AUTH) {
            // 如果SSID有所属部门，则查询员工所属部门或父部门，是否和SSID所属部门一致
            result = checkSsidDepartment(authParam);
            if (result.getReturnCode().equals("0")) {
                return result;
            }

            // 员工是否有效，员工所属部门及父部门是否有效
            result = checkDepartmentValid(authParam);
            if (result.getReturnCode().equals("0")) {
                return result;
            }

            // 员工是否开通“员工授权”，员工所属部门及父部门是否开通“员工授权”
            result = checkDepartmentEmployeeAuth(authParam);
            if (result.getReturnCode().equals("0")) {
                return result;
            }
        }*/

            // AD域是否开启
            String adStatus = configService.getByCode("AD-DOMAIN-STATUS");
            // api认证是否开启
            String apiAuthStatus = configService.getByCode("ACCOUNT-AUTH-METHOD");

            // 账号密码认证
            if (authMethod == Constant.AuthMethod.ACCOUNT_AUTH
                    && !adStatus.equals("1")
                    && !apiAuthStatus.equals("2")) {
                // 如果SSID有所属部门，则查询员工所属部门或父部门，是否和SSID所属部门一致
                result = checkSsidDepartment(authParam);
                if (result.getReturnCode().equals("0")) {
                    return result;
                }

                // 员工是否有效，员工所属部门及父部门是否有效
                result = checkDepartmentValid(authParam);
                if (result.getReturnCode().equals("0")) {
                    return result;
                }

                // 检查是否配置带宽
                result = bandwidthCheck(authParam);
                if (result.getReturnCode().equals("0")) {
                    return result;
                }

                // 是否绑定MAC
//            result = checkBindMac(authParam);
//            if (result.getReturnCode().equals("0")) {
//                return result;
//            }

                // 是否超过终端数量限制
                result = checkTerminalNum(authParam);
                if (result.getReturnCode().equals("0")) {
                    return result;
                }
            }

            // 权限验证
            switch (authMethod) {
                case Constant.AuthMethod.ACCOUNT_AUTH:
                    // AD域是否开启，AD域认证优先级高于API认证，AD域和API认证同时打开时，使用AD域
                    if (adStatus.equals("1")) {
                        //todo:2020-12-12
                        boolean check;
                        try {
                            check = adService.ldapAuth(authParam.getUserName(), authParam.getPassword());
                        } catch (Exception e) {
                            log.error("====portal ldap check:", e.getMessage());
                            check = false;
                        }
                        if (check) {
                            result = new BaseResult();
                        } else {
                            result = new BaseResult("0", "LDAP账号密码错误", null);
                        }

                        return result;
                    } else {
                        // 1本地接口，2API认证
                        if (apiAuthStatus.equals("1")) {
                            return accountCheck(authParam);// 本地接口认证
                        } else {
                            result = apiAuthCheck(authParam);// api认证
                            if (result.getReturnCode().equals("0")) {
                                return result;
                            }
                            return result;
                        }
                    }
                case Constant.AuthMethod.SMS_AUTH:
                    return smsCheck(authParam);// 短信
                // case Constant.AuthMethod.WX_AUTH:
                // break;// 微信认证走单独接口
                case Constant.AuthMethod.ONEKEY_AUTH:
                    return oneKeyCheck(authParam);// 一键
                case Constant.AuthMethod.EMPLOYEE_AUTH:
                    return employeeAuthCheck(authParam);// 员工授权
                case Constant.AuthMethod.QRCODE_AUTH:
                    return qrcodeCheck(authParam);// 二维码
                case Constant.AuthMethod.QUESTION_AUTH:
                    return oneKeyCheck(authParam);
                default:
                    return new BaseResult("0", "不支持的认证方式", null);
            }
        } else {
            //计费模式
            AccountUser accountUser = accountUserService.getOne(new QueryWrapper<AccountUser>().eq("login_name", authParam.getUserName())
                    .eq("pwd", EncryptUtils.encodeBase64String(authParam.getPassword())), false);
            if(null == accountUser){
                return new BaseResult("0", "账号密码错误", null);
            }

            String smsCheck = systemConfigService.getByCode("SMS-CHECK");
            //查询用户历史登录
            AuthUser user = authUserService.getOne(new QueryWrapper<AuthUser>()
                    .eq("mac", authParam.getUserMac()).isNotNull("last_online_time"), false);
            if (null == user && "1".equals(smsCheck)) {
                //初次登录校验手机号,短信验证码
                BaseResult checkResult = checkSmsCode(authParam.getPhone(), authParam.getSmsCode());
                if(checkResult.getReturnCode().equals("0")){
                    return checkResult;
                }

                accountUser.setMobile(authParam.getPhone());
                accountUserService.updateById(accountUser);
            }

            if (null != accountUser.getExpireTime() && accountUser.getExpireTime().before(new Date())) {
                return new BaseResult("0", "账号已过期", null);
            }

            if (accountUser.getIsDebt() == 1) {
                return new BaseResult("0", "账号已欠费", null);
            }
            if (accountUser.getIsLocked() == 1) {
                return new BaseResult("0", "账号已锁定", null);
            }
            authParam.setUserName(accountUser.getLoginName());
            authParam.setPassword(EncryptUtils.decodeBase64String(accountUser.getPwd()));
            //增加姓名
            authParam.setGuestName(accountUser.getNickName());
            authParam.setPhone(accountUser.getMobile());

            return new BaseResult("1", "成功", null);
        }
    }

    private BaseResult checkSmsCode(String mobile, String smsCode){
        QueryWrapper<SmsRecord> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone", mobile);
        queryWrapper.eq("result", 1);
        queryWrapper.eq("business_type", 1);
        queryWrapper.eq("is_valid", 1);
        queryWrapper.orderByDesc("create_time");
        List<SmsRecord> list = smsRecordService.list(queryWrapper);
        if (list.size() == 0) {
            return new BaseResult("0", "没有短信记录", null);
        }
        SmsRecord smsRecord = list.get(0);
        if (!smsRecord.getCheckCode().equals(smsCode)) {
            return new BaseResult("0", "短信验证码错误", null);
        }

        int expireMin = 15;
        // 短信过期时间
        String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
        SmsConfig smsConfig = smsConfigService.getById(smsServerId);
        if (null != smsConfig) {
            expireMin = smsConfig.getExpireTime();
        }

        // 短信是否过期
        Long createTime = DateTimeUtil.getMillis(smsRecord.getCreateTime());
        if (System.currentTimeMillis() - createTime > expireMin * 60 * 1000) {
            return new BaseResult("0", "短信验证码过期", null);
        }

        return new BaseResult();
    }

    /**
     * 是否绑定MAC
     */
    public BaseResult checkBindMac(AuthParam authParam) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", authParam.getUserName());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeService.getOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "MAC绑定检查，没有该用户", null);
        }
        if (employee.getIsBindMac() == 0) {
            return new BaseResult();
        }
        String macs = employee.getBindMacs();
        if (macs.contains(authParam.getUserMac().toLowerCase())) {
            return new BaseResult();
        } else {
            return new BaseResult("0", "MAC地址不在绑定列表", null);
        }
    }

    /**
     * 是否配置带宽，带宽策略优先级：个人-》部门-》自定义策略-》全局策略
     */
    public BaseResult bandwidthCheck(AuthParam authParam) {
        String userName = authParam.getUserName();
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_valid", 1);
        queryWrapper.eq("user_name", userName);
        Employee employee = employeeService.getOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "没有该用户", null);
        }

        Integer bandwidthId = employee.getBandwidthId();
        if (bandwidthId == null) {
            // 查询部门带宽
            bandwidthId = employeeService.getDepartmentBandwidth(userName);
        }

        if (bandwidthId == null) {
            // 查询上网策略
            AuthMethod authMethodEntity = authMethodService.getById(Constant.AuthMethod.ACCOUNT_AUTH);
            Integer policyId = authMethodEntity.getCustomPolicyId();
            OnlinePolicy userOnlinePolicy = onlinePolicyService.getById(policyId);
            if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
                bandwidthId = userOnlinePolicy.getBandwidthId();
            }

            // 没配置自定义带宽ID，则使用系统默认带宽
            if (bandwidthId == null) {
                OnlinePolicy defaultOnlinePolicy = onlinePolicyService.getById(100);// 系统默认配置上网策略ID为100
                bandwidthId = defaultOnlinePolicy.getBandwidthId();
            }
        }

        // 系统带宽也没有配置，返回错误
        if (bandwidthId == null) {
            return new BaseResult("0", "请配置带宽", null);
        }

        Bandwidth bandwidth = bandwidthService.getById(bandwidthId);
        log.debug("bandwidth check " + bandwidth.toString());
        if (bandwidth == null) {
            return new BaseResult("0", "带宽ID没有记录", null);
        }

        return new BaseResult();
    }

    /**
     * 是否超过终端数量限制
     */
    public BaseResult checkTerminalNum(AuthParam authParam) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", authParam.getUserName());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeService.getOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "终端数量检查，没有该用户", null);
        }
        if (employee.getIsTerminalNumLimit() == 0) {// 不限制
            return new BaseResult();
        }

        // 使用该账号的在线用户人数
        QueryWrapper<AuthUser> onlineQuery = new QueryWrapper();
        onlineQuery.eq("user_name", authParam.getUserName());
        onlineQuery.eq("online_state", 1);
        onlineQuery.eq("is_valid", 1);
        int onlineNum = authUserService.count(onlineQuery);
        if (onlineNum >= employee.getTerminalNum()) {
            return new BaseResult("0", "该账号超过终端数限制", null);
        }

        return new BaseResult();
    }

    /**
     * 如果SSID有所属部门，则查询员工所属部门或父部门，是否和SSID所属部门一致
     */
    public BaseResult checkSsidDepartment(AuthParam authParam) {
        if (authParam.getAcId() == null) {
            return new BaseResult("0", "请求参数缺少控制器ID", null);
        }
        if (authParam.getSsid() == null) {
            return new BaseResult("0", "请求参数缺少SSID", null);
        }
        QueryWrapper<Ssid> ssidQuery = new QueryWrapper();
        ssidQuery.eq("ac_id", authParam.getAcId());
        ssidQuery.eq("name", authParam.getSsid());
        ssidQuery.eq("is_valid", 1);
        Ssid ssid = ssidService.getOne(ssidQuery);
        if (ssid == null) {// 没SSID记录，不用检查
            return new BaseResult();
        }
        if (ssid.getDepartmentId() == null) {// SSID没有绑定部门，不用检查
            return new BaseResult();
        }

        // 查询员工
        QueryWrapper<Employee> employeeQuery = new QueryWrapper();
        employeeQuery.eq("user_name", authParam.getUserName());
        employeeQuery.eq("is_valid", 1);
        Employee employee = employeeService.getOne(employeeQuery);
        if (employee == null) {
            return new BaseResult("0", "有效性检查，没有该用户", null);
        }
        if (employee.getIsUsing() != 1) {
            return new BaseResult("0", "该员工账号已设置无效", null);
        }

        // 员工没有部门，不用检查
        if (employee.getDepartmentId() == null) {
            return new BaseResult();
        }

        // 员工所属部门检查
        if (employee.getDepartmentId().equals(ssid.getDepartmentId())) {
            return new BaseResult();// 部门匹配，返回成功
        }

        Department depart = departmentService.getById(employee.getDepartmentId());
        if (depart == null) {
            return new BaseResult();// 没有该部门，不用检查
        }
        // 逐级检查父部门
        while (depart.getParentId() != null && depart.getParentId() != -1) {
            depart = departmentService.getById(depart.getParentId());
            if (depart != null && depart.getId().equals(ssid.getDepartmentId())) {
                return new BaseResult();// 部门匹配，返回成功
            }
        }
        // 没有任何部门匹配，返回失败
        return new BaseResult("0", "该热点不属于用户所在部门", null);
    }

    /**
     * 所属部门是否有效，所属部门及父级部门是否有效
     */
    public BaseResult checkDepartmentValid(AuthParam authParam) {
        // 查询员工
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", authParam.getUserName());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeService.getOne(queryWrapper, true);
        if (employee == null) {
            return new BaseResult("0", "有效性检查，没有该用户", null);
        }
        if (employee.getIsUsing() != 1) {
            return new BaseResult("0", "该员工账号已设置无效", null);
        }

        // 没有部门
        if (employee.getDepartmentId() == null) {
            return new BaseResult();
        }

        // 员工所属部门状态检查
        Department depart = departmentService.getById(employee.getDepartmentId());
        if (depart == null) {
            return new BaseResult();
        }
        if (depart.getState() == 0) {
            return new BaseResult("0", "员工所在部门已关闭认证", null);
        }

        // 逐级查询父部门
        while (depart.getParentId() != null && depart.getParentId() != -1) {
            depart = departmentService.getById(depart.getParentId());
            if (depart == null) {
                return new BaseResult();
            }
            if (depart.getState() == 0) {
                return new BaseResult("0", "员工所在父级部门已关闭认证", null);
            }
        }

        return new BaseResult();
    }

    /**
     * 员工是否打开“员工授权”，员工所属部门及父级部门是否打开“员工授权”
     */
    public BaseResult checkDepartmentEmployeeAuth(AuthParam authParam) {
        // 查询员工
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", authParam.getUserName());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeService.getOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "有效性检查，没有该用户", null);
        }
        if (employee.getIsEmployeeAuthEnable() != 1) {
            return new BaseResult("0", "该员工无权进行授权登录", null);
        }

        // 没有部门
        if (employee.getDepartmentId() == null) {
            return new BaseResult();
        }

        // 员工所属部门状态检查
        Department depart = departmentService.getById(employee.getDepartmentId());
        if (depart == null) {
            return new BaseResult();
        }
        if (depart.getIsEmployeeAuthEnable() == 0) {
            return new BaseResult("0", "员工所在部门已关闭员工授权", null);
        }

        // 逐级查询父部门
        while (depart.getParentId() != null && depart.getParentId() != -1) {
            depart = departmentService.getById(depart.getParentId());
            if (depart == null) {
                return new BaseResult();
            }
            if (depart.getIsEmployeeAuthEnable() == 0) {
                return new BaseResult("0", "员工所在父级部门已关闭员工授权", null);
            }
        }

        return new BaseResult();
    }

    /**
     * 查询允许的上网时间
     */
    public BaseResult getPermitPeriod(Integer authMethod) {
        if(authMethod==99) //白名单上网时重置成一键认证的策略
            authMethod = 4;
        // 查询上网策略
        AuthMethod authMethodEntity = authMethodService.getById(authMethod);
        Integer policyId = null;
        if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
            policyId = authMethodEntity.getCustomPolicyId();
        } else {// 使用默认上网策略ID值为100
            policyId = 100;
        }
        OnlinePolicy onlinePolicy = onlinePolicyService.getById(policyId);
        if (onlinePolicy.getIsPeriodLimit() == 1) {
            if (onlinePolicy.getOnlinePeriod() == null || onlinePolicy.getOnlinePeriod() == 0) {
                return new BaseResult("0", "请设置认证方式对应的上网时长", null);
            }
        }

        if (onlinePolicy.getIsPeriodLimit() == 1) {// 限制上网时长
            Integer permitPeriod = onlinePolicy.getOnlinePeriod();
            return new BaseResult(permitPeriod);
        } else {// 不限制上网时长
            return new BaseResult(null);
        }
    }

    /**
     * 是否处于上网时段内
     */
    public BaseResult inOnlinePeriod(Integer authMethod) {
        // 查询当天是星期几
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);// 1是周日
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }

        // 查询上网策略
        if(authMethod==99)
            authMethod = 4;
        AuthMethod authMethodEntity = authMethodService.getById(authMethod);
        Integer policyId = null;
        if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
            policyId = authMethodEntity.getCustomPolicyId();
        } else {// 使用默认上网策略ID值为100
            policyId = 100;
        }
        OnlinePolicy onlinePolicy = onlinePolicyService.getById(policyId);
        if (onlinePolicy.getDay1StartTime() == null || onlinePolicy.getDay1EndTime() == null
                || onlinePolicy.getDay2StartTime() == null || onlinePolicy.getDay2EndTime() == null
                || onlinePolicy.getDay3StartTime() == null || onlinePolicy.getDay3EndTime() == null
                || onlinePolicy.getDay4StartTime() == null || onlinePolicy.getDay4EndTime() == null
                || onlinePolicy.getDay5StartTime() == null || onlinePolicy.getDay5EndTime() == null
                || onlinePolicy.getDay6StartTime() == null || onlinePolicy.getDay6EndTime() == null
                || onlinePolicy.getDay7StartTime() == null || onlinePolicy.getDay7EndTime() == null) {
            return new BaseResult("0", "请设置认证方式对应的上网时间", null);
        }

        // 查询今天的上网开始时间、结束时间
        String startTime = null;
        String endTime = null;
        switch (weekDay) {
            case 1:
                startTime = onlinePolicy.getDay1StartTime();
                endTime = onlinePolicy.getDay1EndTime();
                break;
            case 2:
                startTime = onlinePolicy.getDay2StartTime();
                endTime = onlinePolicy.getDay2EndTime();
                break;
            case 3:
                startTime = onlinePolicy.getDay3StartTime();
                endTime = onlinePolicy.getDay3EndTime();
                break;
            case 4:
                startTime = onlinePolicy.getDay4StartTime();
                endTime = onlinePolicy.getDay4EndTime();
                break;
            case 5:
                startTime = onlinePolicy.getDay5StartTime();
                endTime = onlinePolicy.getDay5EndTime();
                break;
            case 6:
                startTime = onlinePolicy.getDay6StartTime();
                endTime = onlinePolicy.getDay6EndTime();
                break;
            case 7:
                startTime = onlinePolicy.getDay7StartTime();
                endTime = onlinePolicy.getDay7EndTime();
                break;
            default:
                break;
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String curTime = format.format(new Date());
        if (startTime.compareTo(curTime) > 0 || endTime.compareTo(curTime) < 0) {
            return new BaseResult("0", "当前时段不能上网", null);
        }
        return new BaseResult();
    }

    /**
     * 获取用户登录状态，返回重定向地址，
     * 实际测试会不能重定向到首页，因为此时设备已经能上网，不能再访问内部地址，可能和ac控制有关
     */
    public BaseResult getLoginStatus(HttpServletRequest request, String loginUrl, String logoutUrl) throws UnsupportedEncodingException {
        String userIp = IpUtil.getIpAddr(request);
        AuthRecord authRecord = authRecordService.getTopOne(userIp);
        if (authRecord == null) {
            return new BaseResult("0", "没有该用户认证记录", null);
        }

        String frontUrl = hostUrlService.getFrontUrl(request);

        // 查询basIp对应的ac的认证模板
        String acIp = authRecord.getAcIp();
        BaseResult acResult = acService.getInfoByAcIp(acIp);
        if (acResult.getReturnCode().equals("0")) {// 返回重定向地址：错误提示页面
            String errUrl = frontUrl + "/portal/authEmptyErr";
            errUrl += "?errMsg=" + URLEncoder.encode(acResult.getReturnMsg(), "utf-8");
            log.debug("errUrl " + URLDecoder.decode(errUrl, "utf-8"));
            return new BaseResult("0", acResult.getReturnMsg(), errUrl);
        }
        Ac ac = (Ac) acResult.getData();

        /*
         * ac设备上登录接口、退出接口地址
         */
//        String loginUrl = "http://" + acIp + ":9997/login";
//        String logoutUrl = "http://" + acIp + ":9997/logout";
//        if (StringUtil.isNotBlank(proxy)) {
//            loginUrl = "http://" + acIp + ":9997/SubscriberPortal/hotspotlogin";
//            logoutUrl = "http://" + acIp + ":9997/SubscriberPortal/hotspotlogout";
//        }

        // 浏览器类型
        String userAgent = request.getHeader("User-Agent");
        Integer terminalType = BrowseTypeUtil.getTerminalType(userAgent);

        // 组合重定向页面url及参数
        AuthTemplate authTemplate = ac.getAuthTemplate();
        String templateUrl;
        if (terminalType != 1) {
            templateUrl = authTemplate.getBaseTemplate().getMobileUrl();// 认证模板页面
        } else {
            templateUrl = authTemplate.getBaseTemplate().getUrl();
        }
        String targetUrl = frontUrl + templateUrl;
        targetUrl += "?frontUrl=" + URLEncoder.encode(frontUrl, "utf-8");// 要加URLEncoder，否则#会丢失
        targetUrl += "&serverUrl=" + URLEncoder.encode(hostUrlService.getServerUrl(request), "utf-8");
        targetUrl += "&acId=" + ac.getId();
        targetUrl += "&acAuthMethod=" + ac.getAuthMethod();
        targetUrl += "&authTemplateId=" + authTemplate.getId();
        targetUrl += "&authMethod=" + authRecord.getAuthMethod();
        targetUrl += "&userName=" + authRecord.getUserName();
        targetUrl += "&phone=" + authRecord.getPhone();
        targetUrl += "&userIp=" + userIp + "&userMac=" + authRecord.getMac();
        targetUrl += "&formatUserMac=" + MacUtil.macFormat(authRecord.getMac());
        targetUrl += "&userVisitUrl=" + authRecord.getUserVisitUrl();
        targetUrl += "&brandCode=" + ac.getBrand().getCode();// ac设备品牌编码
        targetUrl += "&acIp=" + acIp + "&acMac=" + authRecord.getAcMac();
        targetUrl += "&ssid=" + URLEncoder.encode(authRecord.getSsid(), "utf-8");
        targetUrl += "&apIp=" + authRecord.getAcIp();
        targetUrl += "&apMac=" + authRecord.getApMac();
        targetUrl += "&ruckusAcLoginUrl=" + loginUrl
                + "&ruckusAcLogoutUrl=" + logoutUrl;
        if (authRecord.getOnlineState() == 1) {// 显示登录状态
            // 已用时长，单位秒
            Integer usedPeriod = (int) ((System.currentTimeMillis()
                    - authRecord.getLastOnlineTime().getTime()) / 1000);
            // 允许上网时长，单位分钟
            Integer permitPeriod = null;
            BaseResult permitPeriodResult = getPermitPeriod(authRecord.getAuthMethod());
            if (permitPeriodResult.getReturnCode().equals("1")) {
                if (permitPeriodResult.getData() != null) {
                    permitPeriod = (Integer) permitPeriodResult.getData();
                }
            }

            targetUrl += "&showResult=1";
            targetUrl += "&usedPeriod=" + usedPeriod;
            targetUrl += "&permitPeriod=" + permitPeriod;
        }
        log.debug("targetUrl " + targetUrl);
        log.debug("targetUrlDecode " + URLDecoder.decode(targetUrl, "utf-8"));

        return new BaseResult(targetUrl);
    }

    /**
     * 是否属于黑名单
     */
    BaseResult checkBlackList(AuthParam authParam) {
        // ip黑名单
        /*
         * QueryWrapper<BlackList> ipQuery = new QueryWrapper();
         * ipQuery.eq("type", 3);// 1手机，2MAC，3IP ipQuery.eq("value",
         * authParam.getUserIp()); ipQuery.eq("is_valid", 1); BlackList
         * ipBlackList = blackListService.getOne(ipQuery); if (ipBlackList !=
         * null) { return new BaseResult("0", "已被列入IP黑名单", null); }
         */

        // mac黑名单
        QueryWrapper<BlackList> macQuery = new QueryWrapper();
        macQuery.eq("type", 2);// 1手机，2MAC，3IP
        macQuery.eq("value", authParam.getUserMac());
        macQuery.eq("is_valid", 1);
        BlackList macBlackList = blackListService.getOne(macQuery);
        if (macBlackList != null) {
            return new BaseResult("0", "终端已被列入MAC地址黑名单", null);
        }

        // 手机黑名单
        if (authParam.getAuthMethod() == Constant.AuthMethod.SMS_AUTH) {
            QueryWrapper<BlackList> phoneQuery = new QueryWrapper();
            phoneQuery.eq("type", 1);// 1手机，2MAC，3IP
            phoneQuery.eq("value", authParam.getPhone());
            phoneQuery.eq("is_valid", 1);
            BlackList phoneBlackList = blackListService.getOne(phoneQuery);
            if (phoneBlackList != null) {
                return new BaseResult("0", "已被列入手机号码黑名单", null);
            }
        }
        return new BaseResult();
    }

    /**
     * 认证方式是否开通
     */
    BaseResult isAuthMethodOpen(int authMethod, String acIp, Integer isMobileBrowse) {
        // 是否已全局开通这种认证方式
        QueryWrapper<AuthMethod> methodQuery = new QueryWrapper();
        methodQuery.eq("id", authMethod);
        methodQuery.eq("is_valid", 1);
        AuthMethod method = authMethodService.getOne(methodQuery);
        if (method.getIsEnable().equals(0)) {
            return new BaseResult("0", "平台未开通该认证方式", null);
        }

        // AC设备是否已开通这种认证方式
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("ip", acIp);
        acQuery.eq("is_valid", 1);
        Ac ac = acService.getOne(acQuery);// 查询basIp所属ac的配置参数
        if (ac == null) {
            return new BaseResult("0", "该AC设备未登记", null);
        }
        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        Integer isAccount = null == licenceInfo ? 0 : licenceInfo.getIsAccount();
        if (isAccount == 0 && !ac.getAuthMethod().contains(String.valueOf(authMethod))) {
            return new BaseResult("0", "该认证方式未在设备上开通", null);
        }

        // 是否允许pc登录
        if (ac.getIsPcEnable() == 0) {
            if (isMobileBrowse == 0) {
                return new BaseResult("0", "该AC设备未开通电脑登录", null);
            }
        }

        return new BaseResult();
    }

    /**
     * 用户名、密码验证
     */
    BaseResult accountCheck(AuthParam authParam) {
        // 查询用户名、密码
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_valid", 1);
        queryWrapper.eq("user_name", authParam.getUserName());
        Employee employee = employeeService.getOne(queryWrapper);
        if (employee != null) {
            if (employee.getPassword().equals(authParam.getPassword())) {
                return new BaseResult("1", "成功", null);
            } else {
                return new BaseResult("0", "用户名或密码错误", null);
            }
        } else {
            return new BaseResult("0", "没有该账户", null);
        }
    }

    /**
     * 短信认证
     */
    BaseResult smsCheck(AuthParam authParam) {
        // 查询短信记录
        QueryWrapper<SmsRecord> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone", authParam.getPhone());
        queryWrapper.eq("result", 1);
        queryWrapper.eq("is_valid", 1);
        queryWrapper.orderByDesc("create_time");
        List<SmsRecord> list = smsRecordService.list(queryWrapper);
        if (list.size() == 0) {
            return new BaseResult("0", "没有短信记录", null);
        }
        SmsRecord smsRecord = list.get(0);
        if (!smsRecord.getCheckCode().equals(authParam.getSmsCode())) {
            return new BaseResult("0", "短信验证码错误", null);
        }

        // 短信过期时间
        int expireMin = getSmsExpireMin();

        // 短信是否过期
        Long createTime = DateTimeUtil.getMillis(smsRecord.getCreateTime());
        if (System.currentTimeMillis() - createTime > expireMin * 60 * 1000) {
            return new BaseResult("0", "短信验证码过期", null);
        }

        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 修改入参的用户名、密码
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());
        return new BaseResult("1", "成功", null);
    }

    /**
     * 短信过期时间
     */
    int getSmsExpireMin() {
        String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
        SmsConfig smsConfig = smsConfigService.getById(smsServerId);
        if (smsConfig == null) {
            return 15;
        }
        return smsConfig.getExpireTime();
    }

    /**
     * 一键认证
     */
    BaseResult oneKeyCheck(AuthParam authParam) {
        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 修改入参的用户名、密码
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());
        return new BaseResult("1", "成功", null);
    }

    /**
     * 员工授权认证
     */
    BaseResult employeeAuthCheck(AuthParam authParam) {
        // 原本逻辑：检查员工账户密码
//        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
//        queryWrapper.eq("user_name", authParam.getUserName());
//        queryWrapper.eq("is_valid", 1);
//        Employee employee = employeeService.getOne(queryWrapper);
//        if (employee == null) {
//            return new BaseResult("0", "没有该账户", null);
//        }
//        if (!employee.getPassword().equals(authParam.getPassword())) {
//            return new BaseResult("0", "用户名或密码错误", null);
//        }
        //定制逻辑
//        boolean accountCheck = ldapService.ldapAuth(authParam.getUserName(), authParam.getPassword());
//        if(!accountCheck){
//            return new BaseResult("0", "用户名或密码错误", null);
//        }

        String employeeIp = IpUtil.getIpAddr(request);
        QueryWrapper<AuthUser> authUserWrapper = new QueryWrapper();
        authUserWrapper.eq("is_valid", 1);
        authUserWrapper.eq("ip", employeeIp);
        authUserWrapper.eq("online_state", 1);
        authUserWrapper.orderByDesc("update_time");
        List<AuthUser> authUsers = authUserService.list(authUserWrapper);
        if(CollectionUtils.isNotEmpty(authUsers)){
            QueryWrapper<Employee> employeeWrapper = new QueryWrapper();
            employeeWrapper.eq("is_valid", 1);
            employeeWrapper.eq("is_employee_auth_enable", 1);
            employeeWrapper.like("bind_macs", authUsers.get(0).getMac().toLowerCase());
            Employee employee = employeeService.getOne(employeeWrapper);
            if(null==employee){
                return new BaseResult("0", "用户没有授权", null);
            } else {
                Department department = departmentService.getById(employee.getDepartmentId());
                if(0==department.getIsEmployeeAuthEnable()){
                    return new BaseResult("0", "用户关联部门没有授权", null);
                }
            }
            authParam.setAuthEmployeeId(employee.getId());
            authParam.setAuthEmployeeName(employee.getFullName());
        } else {
            return new BaseResult("0", "用户状态异常", null);
        }

        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 修改入参的用户名、密码
        authParam.setUserName(defaultAccount.getUserName());
        authParam.setPassword(defaultAccount.getPassword());

        return new BaseResult("1", "成功", null);
    }

    /**
     * 二维码认证
     */
    BaseResult qrcodeCheck(AuthParam authParam) {
        // 查询默认账户密码
        QueryWrapper<Employee> defaultAccountQuery = new QueryWrapper();
        defaultAccountQuery.eq("is_valid", 1);
        defaultAccountQuery.eq("user_name", "portalDefaultAccount");
        Employee defaultAccount = employeeService.getOne(defaultAccountQuery);
        if (defaultAccount == null) {
            return new BaseResult("0", "缺少默认账户", null);
        }

        // 查询qrcode是否有效
        QueryWrapper<AuthQrcode> qrQuery = new QueryWrapper();
        qrQuery.eq("is_valid", 1);
        qrQuery.eq("sn", authParam.getQrcodeSn());
        AuthQrcode authQrcode = authQrcodeService.getOne(qrQuery);
        if (authQrcode != null) {
            // 是否过期
            if (DateUtil.compareDate(new Date(), authQrcode.getStartTime())
                    || DateUtil.compareDate(authQrcode.getEndTime(), new Date())) {
                return new BaseResult("0", "该二维码已过期", null);
            }

            // 修改入参的用户名、密码
            authParam.setUserName(defaultAccount.getUserName());
            authParam.setPassword(defaultAccount.getPassword());
            return new BaseResult("1", "成功", null);
        } else {
            return new BaseResult("0", "该二维码无效", null);
        }
    }

    public BaseResult apiAuthCheck(AuthParam authParam) {
        // 获取请求地址
        String reqUrl = systemConfigService.getByCode("API-AUTH-URL");

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            post = new HttpPost(reqUrl);
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            ApiReqParam reqParam = new ApiReqParam();
            reqParam.setAccount(authParam.getUserName());
            reqParam.setPassword(authParam.getPassword());
            String jsonParam = JsonObjUtils.obj2json(reqParam);
            StringEntity entity = new StringEntity(jsonParam, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("请求出错: " + statusCode);
                return new BaseResult("0", "Api认证接口状态码异常：" + statusCode, null);
            } else {
                String resultStr = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug("Api认证返回：" + resultStr);
                if (StringUtil.isBlank(resultStr)) {
                    return new BaseResult("0", "Api认证返回为空", null);
                } else {
                    Map<String, ?> resultMap = JsonObjUtils.json2map(resultStr);
                    if (resultMap.get("code") == null) {
                        return new BaseResult("0", "Api认证返回参数错误", null);
                    }
                    Integer resultCode = Integer.parseInt(resultMap.get("code").toString());
                    if (resultCode != 1) {
                        if (resultMap.get("msg") != null) {
                            return new BaseResult("0", "Api认证失败：" + resultMap.get("msg"), null);
                        } else {
                            return new BaseResult("0", "Api认证失败", null);
                        }
                    }
                }
                //log.debug("resultStr " + resultStr);
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
            //log.error("apiAuthCheck exception " + e.getMessage());
            return new BaseResult("0", "Api认证接口异常：" + e.getMessage(), null);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }

        //验证成功，添加用户到本地账户
        Employee existUser = employeeService.getOne(new QueryWrapper<Employee>()
                .eq("user_name", authParam.getUserName()));
        if(null == existUser){
            existUser = new Employee();
            existUser.setUserName(authParam.getUserName());
            existUser.setNickName(authParam.getUserName());
            existUser.setBindMacs(authParam.getUserMac());
            existUser.setIsEmployeeAuthEnable(1);
            existUser.setDepartmentId(1); //默认分组
            employeeService.saveOrUpdate(existUser);
        }

        return new BaseResult();
    }

    public BaseResult apiAuthCheck2(String userName, String passwd) {
        // 获取请求地址
        String reqUrl = systemConfigService.getByCode("API-AUTH-URL");

        //恒天财富webservice接口
        reqUrl = "http://macom.263.net/axis/xmapi?op=authentication_New";
        String userid = userName;
        String password = passwd;
        String domain = "chtwm.com";
        int crypttype = 4;
        String account = "chtwm.com";
        String key = "V45J6mQ2X";
//        password = EncryptUtils.MD5(password);
        String sign = EncryptUtils.MD5(userid + domain + password + key);

        //拼接好xml
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
        sb.append("<soap:Body>\n");
        sb.append("<authentication_New xmlns=\"http://WebXml.com.cn/\">\n");
        sb.append("<userid>" + userid + "</userid>\n");
        sb.append("<domain>" + domain + "</domain>\n");
        sb.append("<password>" + password + "</password>\n");
        sb.append("<crypttype>" + crypttype + "</crypttype>\n");
        sb.append("<account>" + account + "</account>\n");
        sb.append("<sign>" + sign + "</sign>\n");
        sb.append("</authentication_New>\n");
        sb.append("</soap:Body>\n");
        sb.append("</soap:Envelope>\n");
        String xmlStr = sb.toString();
        log.error("===request:" + xmlStr);

        String resultXml = HttpClientUtil.doPostWebservice(reqUrl, xmlStr);
        try {
            log.error("===repsonse:" + resultXml);
            Document doc = DocumentHelper.parseText(resultXml);
            Element rootElt = doc.getRootElement(); // 获取根节点
            System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称

            String result = rootElt.element("Body")
                    .element("authentication_NewResponse")
                    .elementText("authentication_NewReturn");
            if("0".equals(result)){
                return new BaseResult();
            }

        } catch (DocumentException e) {
            return new BaseResult("-1", "认证失败", null);
        }

        return new BaseResult("-1", "认证失败", null);
    }

    /**
     * 认证时测试参数：认证界面输入账号“jixiang”，密码“90-=uiop”，管理后台dn填入“DC=ushareyun,DC=net”
     * 本段代码测试参数："10.100.0.249", "389", "jixiang@ushareyun.net",
     * "90-=uiop"
     */
    public BaseResult adDomainCheck(String userName, String passwd) {
        String adStatus = configService.getByCode("AD-DOMAIN-STATUS");
        String adIp = configService.getByCode("AD-DOMAIN-IP");
        String adPort = configService.getByCode("AD-DOMAIN-PORT");
        String adSsl = configService.getByCode("AD-DOMAIN-SSL");
        String adDn = configService.getByCode("AD-DOMAIN-DN");
        String adDomainName = configService.getByCode("AD-DOMAIN-DOMAIN-NAME");
        String host = adIp;
        String port = adPort;
        String username = userName;
        String password = passwd;

        StringBuilder ldapDomainName = new StringBuilder(username);
        String ldapUrl = "";
        String[] dnArr = adDn.split(",");
        if (null != dnArr && dnArr.length > 0) {
            ldapDomainName.append("@");
            for (String s : dnArr) {
                if (s.toLowerCase().startsWith("dc=")) {
                    ldapDomainName.append(s.substring(3));
                    ldapDomainName.append(".");
                }
            }

            ldapUrl = ldapDomainName.substring(0, ldapDomainName.lastIndexOf("."));
        }

        DirContext ctx = null;
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别(none,simple,strong)
        env.put(Context.SECURITY_PRINCIPAL, ldapUrl); // AD的用户名
        env.put(Context.SECURITY_CREDENTIALS, password); // AD的密码
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); // LDAP工厂类
        env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port);// 默认端口389

        // 批量处理
        env.put(Context.BATCHSIZE, "50");
        // 连接超时设置
        env.put("com.sun.jndi.ldap.connect.timeout", "3000");
        // LDAP连接池
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        // LDAP连接池最大数
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3");
        // LDAP连接池优先数
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1");
        // LDAP连接池超时
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "300000");
        // LDAP连接池初始化数
        env.put("com.sun.jndi.ldap.connect.pool.initsize", "1");
        // LDAP连接池的认证方式
        env.put("com.sun.jndi.ldap.connect.pool.authentication", "simple");
        BaseResult result = null;
        try {
            ctx = new InitialDirContext(env);// 初始化上下文
            log.debug("身份验证成功!");
            result = new BaseResult();
        } catch (AuthenticationException e) {
            log.debug("身份验证失败!");
            log.error("Error Exception=", e);
            result = new BaseResult("0", "AD域身份验证失败", null);
        } catch (javax.naming.CommunicationException e) {
            log.debug("AD域连接失败!");
            log.error("Error Exception=", e);
            result = new BaseResult("0", "AD域连接失败", null);
        } catch (Exception e) {
            log.debug("身份验证未知异常!");
            log.error("Error Exception=", e);
            result = new BaseResult("0", "AD域身份验证未知异常", null);
        } finally {
            if (null != ctx) {
                try {
                    ctx.close();
                    ctx = null;
                } catch (Exception e) {
                    log.error("Error Exception=", e);
                }
            } else {
            }
            return result;
        }
    }

    /*
     * void connectSSL(){ String keystore =
     * "C:\\Program Files\\Java\\jdk1.8.0_191\\jre\\lib\\security\\cacerts";
     * System.setProperty("javax.net.ssl.trustStore", keystore);
     * System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
     *
     * Hashtable<String, String> env = new Hashtable<>();
     *
     * env.put(Context.INITIAL_CONTEXT_FACTORY,
     * "com.sun.jndi.ldap.LdapCtxFactory");// java.naming.factory.initial
     * env.put(Context.PROVIDER_URL, "ldap://10.100.0.249:636");//
     * java.naming.provider.url env.put(Context.SECURITY_AUTHENTICATION,
     * "simple");// java.naming.security.authentication
     * env.put(Context.SECURITY_PRINCIPAL,"jixiang@ushareyun.net");//
     * java.naming.security.principal env.put(Context.SECURITY_CREDENTIALS,
     * "90-=uiop");// java.naming.security.credentials
     * env.put(Context.SECURITY_PROTOCOL, "ssl");
     *
     * //批量处理 env.put(Context.BATCHSIZE, "50"); // 连接超时设置
     * env.put("com.sun.jndi.ldap.connect.timeout", "3000"); // LDAP连接池
     * env.put("com.sun.jndi.ldap.connect.pool", "true"); // LDAP连接池最大数
     * env.put("com.sun.jndi.ldap.connect.pool.maxsize", "3"); // LDAP连接池优先数
     * env.put("com.sun.jndi.ldap.connect.pool.prefsize", "1"); // LDAP连接池超时
     * env.put("com.sun.jndi.ldap.connect.pool.timeout", "300000"); //
     * LDAP连接池初始化数 env.put("com.sun.jndi.ldap.connect.pool.initsize", "1"); //
     * LDAP连接池的认证方式 env.put("com.sun.jndi.ldap.connect.pool.authentication",
     * "simple"); try { LdapContext ctx = new InitialLdapContext(env, null);
     * log.debug("AD域ssl身份认证成功"); } catch (NamingException e) {
     * log.error("Error Exception=", e); log.debug("AD域ssl身份认证失败"); } }
     */

}
