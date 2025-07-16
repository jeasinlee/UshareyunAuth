package cn.ushare.account.admin.radius.service;

import cn.ushare.account.admin.config.ApplicationContextProvider;
import cn.ushare.account.admin.portal.service.IdentityCheckService;
import cn.ushare.account.admin.portal.service.PortalUtil;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.service.impl.AdServiceImpl;
import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.EncryptUtils;
import cn.ushare.account.util.MacUtil;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Radius认证服务
 */
@Slf4j
public class RadiusService implements Runnable {

    private Integer listenPort = 1812;// 监听端口，固定值
    // private String updateUrl = null;// 推送地址
    private boolean isRunning = false;
    private DatagramSocket socket = null;

    AcService acService;
    EmployeeService employeeService;
    BandwidthService bandwidthService;
    SystemConfigService systemConfigService;
    AuthMethodService authMethodService;
    AuthParamService authParamService;
    OnlinePolicyService onlinePolicyService;
    AuthQrcodeService authQrcodeService;
    AuthRecordService authRecordService;
    AuthUserService authUserService;
    IdentityCheckService identityCheckService;
    WhiteListService whiteListService;
    AdService adService;
    String guestExpireTime;
    LicenceService licenceService;
    SsidService ssidService;
    MuteDeviceService muteDeviceService;
    AccountUserService userService;
//    AccountUserDebtService userDebtService;
//    AccountUserLockedService userLockedService;
    AccountUserMacService userMacService;

    //多线池
    private static ThreadPoolExecutor threadPool;
    private static int poolThread = 10;//开启1个线程(获得CPU核数，多少核就多少线程，最多不建议超过cpu的4倍 )/

    public static ThreadPoolExecutor getThreadPool() {
        if (threadPool == null) {
            //ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue workQueue,  RejectedExecutionHandler handler)
            /*corePoolSize： 线程池维护线程的最少数量
            maximumPoolSize：线程池维护线程的最大数量
            keepAliveTime： 线程池维护线程所允许的空闲时间
            unit： 线程池维护线程所允许的空闲时间的单位
            workQueue： 线程池所使用的缓冲队列
            handler： 线程池对拒绝任务的处理策略  */
            //创建一个3个线程的线程池,超时时间500秒
            threadPool = new ThreadPoolExecutor(2, poolThread, 500L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        return threadPool;
    }

    public RadiusService() {
        this.acService = ApplicationContextProvider.getBean(AcService.class);
        this.employeeService = ApplicationContextProvider.getBean(EmployeeService.class);
        this.bandwidthService = ApplicationContextProvider.getBean(BandwidthService.class);
        this.systemConfigService = ApplicationContextProvider.getBean(SystemConfigService.class);
        this.authMethodService = ApplicationContextProvider.getBean(AuthMethodService.class);
        this.authParamService = ApplicationContextProvider.getBean(AuthParamService.class);
        this.onlinePolicyService = ApplicationContextProvider.getBean(OnlinePolicyService.class);
        this.authQrcodeService = ApplicationContextProvider.getBean(AuthQrcodeService.class);
        this.authRecordService = ApplicationContextProvider.getBean(AuthRecordService.class);
        this.authUserService = ApplicationContextProvider.getBean(AuthUserService.class);

        this.whiteListService = ApplicationContextProvider.getBean(WhiteListService.class);
        this.identityCheckService = ApplicationContextProvider.getBean(IdentityCheckService.class);
        this.adService = ApplicationContextProvider.getBean(AdServiceImpl.class);
        this.licenceService = ApplicationContextProvider.getBean(LicenceService.class);
        this.ssidService = ApplicationContextProvider.getBean(SsidService.class);
        this.muteDeviceService = ApplicationContextProvider.getBean(MuteDeviceService.class);

        this.userService = ApplicationContextProvider.getBean(AccountUserService.class);
//        this.userDebtService = ApplicationContextProvider.getBean(AccountUserDebtService.class);
//        this.userLockedService = ApplicationContextProvider.getBean(AccountUserLockedService.class);
        this.userMacService = ApplicationContextProvider.getBean(AccountUserMacService.class);
    }

    @Override
    public void run() {
        log.debug("RadiusService run");
        try {
            socket = new DatagramSocket(listenPort);
            isRunning = true;
            while (isRunning) {
                try {
                    byte[] buf = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buf, 4096);
                    socket.receive(packet);// 接收数据
//                    handlerData(packet);// 解析数据，返回响应

                    Thread tempThread = new Thread(() -> handlerData(packet));
                    getThreadPool().submit(tempThread);

                } catch (Exception e1) {
                    e1.printStackTrace();
                    log.error(e1.getStackTrace().toString());
                }
            }
        } catch (SocketException e) {
            log.error("Error Exception=", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        isRunning = false;
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            log.error(e.getStackTrace().toString());
        }
    }

    /**
     * 解析数据，返回响应
     * 必须加synchronized，存在一次登录中，因为radius响应不及时，ac连续发送多次相同用户的登录请求的情况，
     * 此时不加同步，会出现authUser中多条相同记录
     */
    private void handlerData(DatagramPacket packet) {
        try {
            String ip = packet.getAddress().getHostAddress();
            Integer port = packet.getPort();
            byte[] inData = packet.getData();
            int code = inData[0];// code字段
            int identifier = inData[1] & 0xff;// identifier字段

            /* 字段：包长度
             * code、identifier、length、authenticator、 attributes的长度总和，
             * 有效范围是20~4096，
             * int length = (inData[2] << 8) | inData[3];
             */
            int length = packet.getLength();

            /* 字段：验证字
             * (1)请求验证字(Request Authenticator)，用在请求报文中，必须为全局唯一的随机值
             * (2)响应验证字(Response Authenticator)，用在响应报文中，用于鉴别响应报文合法性，
             *    响应验证字 = MD5(Code+ID+Length+请求验证字+Attributes+SharedSecret)
             */
            String authenticator = RadiusUtil.ByteToHex(inData, 4, 20);

            // 属性域
            String attributes = RadiusUtil.ByteToHex(inData, 20, length);
            // 属性域解析成list，每行item[0]为type，item[1]为value
            String[][] attrList = null;
            if ((attributes != null) && (attributes.length() > 0)) {
                attrList = RadiusUtil.getAttributes(attributes);
            }

            log.debug("Receive " + "ip=" + ip + ",port=" + port + ",code="
                    + code + ",identifier=" + identifier
                    + ",length=" + length + ",authenticator="
                    + authenticator + ",attributes=" + attributes);

            // 生成响应数据
            byte[] outData = buildOutData(code, ip, port, identifier,
                    authenticator, attrList);

            // 发送响应数据
            if (outData != null) {
                /*
                 * 调试数据打印 int lengthTo = outData.length; // 属性域 String
                 * attributesTo = RadiusUtil.ByteToHex(outData, 20, lengthTo);
                 *
                 * String[][] attributesListTo = null; if ((attributesTo !=
                 * null) && (attributesTo.length() > 0)) { attributesListTo =
                 * RadiusUtil.getAttributes(attributesTo); } if
                 * (attributesListTo != null && attributesListTo.length > 0) {
                 * for (int i = 0; i < attributesListTo.length; i++) { try { int
                 * type = Integer.parseInt( attributesListTo[i][0], 16);
                 * RadiusUtil.getAttributeValue(ip, type,
                 * attributesListTo[i][1]).trim(); } catch (Exception e) {
                 * RadiusUtil.writeErrorLog("Error", e); } } }
                 */

                DatagramPacket outPacket = new DatagramPacket(outData, outData.length, packet.getSocketAddress());
                socket.send(outPacket);
                log.debug(ip + " Radius Response Finish");
            }
        } catch (Exception e) {
            log.error(e.getStackTrace().toString());
        }
    }

    private byte[] buildOutData(int code, String ip, int port, int identifier, String authenticator,
                                String[][] attrList) {
        byte[] ret = null;
        try {
            // 1接入请求报文，2接入成功回应报文，3接入拒绝回应报文，4计费请求报文
            // 5计费回应报文，11接入挑战报文，12服务器状态报文（试验），
            // 13客户端状态报文（试验），255保留
            switch (code) {
                case 1: // Access-Request(认证请求数据包)
                    log.debug(ip + ">>Access-Request");
                    ret = accessRequest(ip, port, identifier,
                            authenticator, attrList);
                    break;
                case 11:// Access-Challenge()
                    log.debug(ip + ">>Access-Challenge");
                    ret = accessChallenge(ip, port, identifier,
                            authenticator, attrList);
                    break;
                case 12:// Status-Server(试验阶段)
                    log.debug(ip + ">>Status-Server");
                    ret = statusServer(ip, port, identifier,
                            authenticator, attrList);
                    break;
                case 13:// Status-Client(试验阶段)
                    log.debug(ip + ">>Status-client");
                    ret = statusClient(ip, port, identifier,
                            authenticator, attrList);
                    break;
                case 255:// Reserved(保留)
                    log.debug(ip + ">>Reserved");
                    ret = reserved(ip, port, identifier,
                            authenticator, attrList);
                    break;
                default:
                    log.debug(ip + " code ERROR (" + code + ")");
                    break;
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
        return ret;
    }

    /**
     * Access-Request(认证请求数据包)
     */
    private byte[] accessRequest(String ip, int port, int identifier,
                                 String authenticator, String[][] attrList) {
        byte[] ret = null;
        // 1接入请求报文，2接入成功回应报文，3接入拒绝回应报文，4计费请求报文
        // 5计费回应报文，11接入挑战报文，12服务器状态报文（试验），
        // 13客户端状态报文（试验），255保留
        Integer code = 0;
        String attributes = "";
        String userName = null;
        String reqPassword = null;// 请求参数中的密码
        String userIp = null;
        String challenge = null;
        String nasIp = null;
        Integer nasPort = 0;
        String acctSessionId = null;
        String callingStationId = null;
        String nasIdentifier = "";
        String typeName = null;
        boolean isChap = false;

        String eapMessage = null;
        String eapUser = null;
        String messageAuthenticator = null;

        String calledStationId = null;

        // 解析属性域列表
        for (int i = 0; i < attrList.length; i++) {
            try {
                int type = Integer.parseInt(attrList[i][0], 16);
                String value = RadiusUtil.getAttributeValue(ip, type,
                        attrList[i][1]).trim();
                switch (type) {
                    case 1:// User-Name(用户账户ID)
                        userName = value;
                        break;
                    case 2:// User-Password(用户密码)
                        reqPassword = value;
                        isChap = false;
                        break;
                    case 3:// Chap-Password()
                        reqPassword = value;
                        isChap = true;
                        break;
                    case 4:// Nas-IP-Address(Nas的ip地址)
                        nasIp = value;
                        break;
                    case 5:// Nas-Port(用户接入端口号)
                        nasPort = Integer.parseInt(value);
                        break;
                    case 8:// Framed-IP-Address(为用户提供的IP地址)
                        userIp = value;
                        break;
                    case 30:
                        calledStationId = value;
                        break;
                    case 31:// Calling-Station-Id(mac地址)
                        // 即userMac，不同设备格式不同，h3c和ruckus：14-A5-1A-33-0B-BC，华为：14a5-1a33-0bbc
                        callingStationId = value;
                        break;
                    case 32:// Nas-Identifier(标识NAS的字符串)
                        nasIdentifier = value;
                        break;
                    case 44:// Acct-Session-Id(计费会话标识)
                        acctSessionId = value;
                        break;
                    case 60:// CHAP-Challenge
                        challenge = value;
                        break;

                    case 79: // EAP-Message
                        eapMessage = value;
                        eapUser = RadiusUtil.hexStringToString(RadiusUtil.ByteToHex(RadiusUtil.HexToByte(value), 5, RadiusUtil.HexToByte(value).length));
                        break;
                    case 80://Message_Authenticator
                        messageAuthenticator = value;
                        break;

                    default:
                        break;
                }
            } catch (Exception e1) {
                log.error("Error " + e1.getMessage());
            }
        }

//        log.debug("=====eap:" + eapMessage + ",eapUser:" + eapUser + ",messageAuthenticator:" + messageAuthenticator);
        // packetUserName，如果username变成了zhangsan@dmtest，要检查h3c设备的配置
        String packetUserName = userName;


        // userMac即callingStationId，去掉mac中的“-”
        if (StringUtil.isNotBlank(callingStationId)) {
            callingStationId = PortalUtil.MacFormat(callingStationId);
        }
        String userMac = PortalUtil.MacFormat1(callingStationId);

        //802.1x认证：中继方式
        if (!StringUtils.isAnyBlank(eapUser, messageAuthenticator)) {
            typeName = ">>Access-Challenge";
        }

        //校验授权
        BaseResult licenceResult = licenceService.checkInfo();
        if (!licenceResult.getReturnCode().equals("1")) {
            log.error("授权错误：" + licenceResult.getReturnMsg());
            return ret;
        }

        // 数据校验
        if (userName == null || ip == null
                || nasIp == null) {
            // 用户名、用户Ip、basIp为空
            log.debug("Radius accessRequest失败：用户名或密码、用户Ip、basIp为空");
            return ret;
        }
        String clientType = "";
        String acctInterimInterval = "";// 计费消息发送间隔，单位秒，华为设备数值小于60会请求失败，h3c设备无效，要在ac设备上配置
        String idleTimeout = "";// 空闲时间，超过断网，h3c无效
        String autoKick = "";
        String radiusNasIp = "";
        String[] clients = null;
        int sessionTime = 3600 * 24;// 重新认证时长（即用户在线时长），该值不能太小，因为超过该时间ac会自动下线

        String tempNasIp = nasIp;
        // 根据nasIp查询共享密钥
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("is_valid", 1);
        acQuery.and( wrapper-> wrapper.eq("ip", tempNasIp).or().like("nas_ip", tempNasIp));
        Ac ac = acService.getOne(acQuery);
        if (ac == null) {
            log.debug("Radius accessRequest失败：没有Ip " + nasIp + " 对应的AC设备");
            return ret;
        }
        String sharedSecret = ac.getShareKey();
        if (StringUtil.isBlank(sharedSecret)) {// 检查密钥
            log.debug(ip + " Unknow NAS Client !!");
            return ret;
        }
        String ssid = null;
        if(StringUtils.isNotBlank(calledStationId)){
            String[] ssids = calledStationId.split(":");
            if(null!=ssids && ssids.length>1) {
                ssid = ssids[1];
            } else {
                ssid = PortalUtil.MacFormat1(calledStationId);
            }
        }

        String sharedSecretHex = RadiusUtil.ByteToHex(sharedSecret.getBytes());
        log.error("Access-Request " + "ip=" + ip + ",nasIp=" +nasIp+",port=" + port + ",userMac=" + userMac
                + ",userName=" + userName + ",userIp=" + userIp +",password=" + reqPassword + ",ssid=" + ssid);


        // 查询访客登录参数
        AuthParam authParam = null;
        if (null != userIp) {
            authParam = authParamService.getByUserIp(userIp);
        } else {
            authParam = authParamService.getByUserMac(userMac);
            if(null == authParam){
                //针对哑终端的逻辑
                QueryWrapper<MuteDevice> muteDeviceQueryWrapper = new QueryWrapper<>();
                muteDeviceQueryWrapper.eq("bind_purpose", 0);
                muteDeviceQueryWrapper.eq("bind_mac", userName);
                muteDeviceQueryWrapper.eq("is_valid", 1);
                muteDeviceQueryWrapper.orderByDesc("id");

                List<MuteDevice> muteDevices = muteDeviceService.list(muteDeviceQueryWrapper);
                if(CollectionUtils.isNotEmpty(muteDevices)){
                    MuteDevice muteDevice = muteDevices.get(0);
                    authParam = new AuthParam();
                    authParam.setAcId(ac.getId());
                    authParam.setAcIp(ac.getIp());
                    authParam.setUserMac(muteDevice.getBindMac());
                    authParam.setSsid(ssid);
                    authParamService.save(authParam);
                }
            }

        }
        if(null == authParam){
            // 没有该用户
            typeName = "Access-Reject";
            code = 3;
            attributes = RadiusUtil.getAttributeString(18,
                    "code=101,Login Failed. Please check your userName");
            log.debug(ip + " code=101,Login Failed. Please check your userName");
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
            return ret;
        }

        if(null==authParam.getTerminalType()){
            authParam.setTerminalType(2);
        }
        authParam.setSsid(ssid);

        Integer authMethod = authParam.getAuthMethod();
        // AD域是否开启
        String adStatus = systemConfigService.getByCode("AD-DOMAIN-STATUS");
        // api认证是否开启
        String apiAuthStatus = systemConfigService.getByCode("ACCOUNT-AUTH-METHOD");

        // 根据userName查询账号密码
        String databasePassword = null;
        // 校验密码
        String checkResult = null;
        // attributes根据不同clientType，增加速率参数
        // 查询系统默认带宽策略
        OnlinePolicy defaultOnlinePolicy = onlinePolicyService.getById(100);// 系统默认配置上网策略ID为100

        // 查询带宽
        Integer bandwidthId = null;
        boolean macPrior = false;  //为true不校验密码

        // 如果是账号密码登录，则查询员工带宽，如果员工没设置带宽，则查询部门的带宽，如果没有部门带宽，则查询自定义带宽，没有自定义带宽，则查询全局带宽
        // 优先级：员工带宽-》部门带宽-》自定义带宽策略-》全局带宽策略([A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2}
        log.error("====ssid:" + ssid + ", authMethod:" + authParam.getAuthMethod());

        if(null == authMethod){
            authParam.setAuthMethod(Constant.AuthMethod.ACCOUNT_AUTH);
        }

        // 计费版本
        LicenceInfo accountInfo = licenceService.getAccountInfo();
        boolean isAccount = (null != accountInfo.getIsAccount() && accountInfo.getIsAccount() == 1);

        QueryWrapper<Ssid> wrapper= new QueryWrapper<>();
        wrapper.eq("ac_id", ac.getId());
        wrapper.eq("name", ssid);
        wrapper.eq("is_valid", 1);
        Ssid ssidModel = ssidService.getOne(wrapper, false);
        if(!MacUtil.isIDNumber(userName) && MacUtil.isMacAddress(userName)){
            //MAC无感知
            if(null!=ssidModel){
                if(!isAccount){
                    if(1==ssidModel.getIsEmployee()){
                        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                        queryWrapper.eq("is_valid", 1);
                        queryWrapper.like("bind_macs", userName);
                        Employee employee = employeeService.getOne(queryWrapper);
                        if (null != employee) {
                            macPrior = true;
                            authParam.setUserName(userName);
                        }
                    } else {
                        QueryWrapper<WhiteList> whiteWrapper = new QueryWrapper<>();
                        whiteWrapper.eq("value", PortalUtil.MacFormat1(userName));
                        whiteWrapper.eq("type", 2);
                        WhiteList whiteList = whiteListService.getOne(whiteWrapper);
                        if(null!=whiteList){
                            macPrior = true;
                            authParam.setUserName(userName);
                        }
                    }

                    //哑终端设备
                    QueryWrapper<MuteDevice> muteDeviceQueryWrapper = new QueryWrapper<>();
                    muteDeviceQueryWrapper.eq("bind_purpose", 0);
                    muteDeviceQueryWrapper.eq("bind_mac", userName);
                    muteDeviceQueryWrapper.eq("is_valid", 1);
                    muteDeviceQueryWrapper.orderByDesc("id");

                    List<MuteDevice> muteDevices = muteDeviceService.list(muteDeviceQueryWrapper);
                    if(CollectionUtils.isNotEmpty(muteDevices)){
                        MuteDevice muteDevice = muteDevices.get(0);
                        if(null!=muteDevice){
                            if(1==muteDevice.getIsAlways()){
                                macPrior = true;
                            } else {
                                macPrior = muteDevice.getRangeTime().after(new Date());
                            }
                        }
                    }
                    bandwidthId = defaultOnlinePolicy.getBandwidthId();
                } else {
                    //计费版本逻辑
                    QueryWrapper<AccountUserMac> queryWrapper = new QueryWrapper();
                    queryWrapper.eq("mac", userName);
                    AccountUserMac accountUserMac = userMacService.getOne(queryWrapper, false);
                    if(null != accountUserMac){
                        AccountUser accountUser = userService.getDetail(accountUserMac.getLoginName(), 1);
                        if(accountUser.getIsDebt()==1){
                            log.debug("用户：" + accountUser.getLoginName() + " 已欠费");
                            return ret;
                        }

                        if(accountUser.getIsLocked()==1){
                            log.debug("用户：" + accountUser.getLoginName() + " 已锁定");
                            return ret;
                        }

                        bandwidthId = accountUser.getBandId();
                    }
                }
            }
        } else {
            if (1 == authMethod) {
                if(!isAccount){
                    BaseResult result;
                    if("1".equals(adStatus) || "2".equals(apiAuthStatus)){
                        if ("1".equals(adStatus)) {
                            //AD域验证
                            boolean check;
                            try {
                                check = adService.ldapAuth(userName, authParam.getPassword());
                            } catch (Exception e) {
                                log.error("====radius ldap check:" , e.getMessage());
                                check = false;
                            }

                            if(check){
                                QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                                queryWrapper.eq("is_valid", 1);
                                queryWrapper.eq("user_name", userName);
                                Employee employee = employeeService.getOne(queryWrapper);
                                if(null!=employee){
                                    if(!employee.getBindMacs().contains(userMac.toLowerCase())){
                                        if(StringUtils.isNotBlank(employee.getBindMacs())) {
                                            employee.setBindMacs(employee.getBindMacs() + "," + userMac.toLowerCase());
                                        }else {
                                            employee.setBindMacs(userMac.toLowerCase());
                                        }
                                        employeeService.updateById(employee);
                                    }
                                } else {
                                    List<LdapUser> users = adService.findUserByName(userName);
                                    if(CollectionUtils.isNotEmpty(users)){
                                        employee = new Employee();
                                        employee.setUserName(userName);
                                        employee.setPassword(users.get(0).getUserPassword());
                                        employee.setFullName(users.get(0).getUserCn());
                                        employee.setDepartmentId(1);
                                        employee.setPhone(users.get(0).getMobile());
                                        employee.setBandwidthId(8);
                                        employee.setTerminalNum(15);
                                        employee.setIsBindMac(1);
                                        employee.setBindMacs(userMac.toLowerCase());
                                        employee.setSex((int)(Math.random()*2));
                                        employee.setIsEmployeeAuthEnable(1);
                                        employee.setIsValid(1);
                                        employee.setIsUsing(1);

                                        employeeService.save(employee);
                                    }
                                }
                                result = new BaseResult();
                            } else {
                                result = new BaseResult("0", "LDAP账号密码错误", null);
                            }

                        } else{
                            //API接口验证
                            result = identityCheckService.apiAuthCheck(authParam);
                        }

                        checkResult = "1".equals(result.returnCode) ? "0" : null;
                        bandwidthId = defaultOnlinePolicy.getBandwidthId();
                        if (null !=checkResult  && checkResult.equals("0")) {
                            macPrior = true;
                        }
                    } else {
                        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                        queryWrapper.eq("is_valid", 1);
                        queryWrapper.eq("user_name", userName);
                        Employee employee = employeeService.getOne(queryWrapper);
                        if (null != employee) {
                            databasePassword = employee.getPassword();
                            bandwidthId = null!=employee?employee.getBandwidthId():null;
                            if (bandwidthId == null) {
                                // 查询部门带宽
                                bandwidthId = employeeService.getDepartmentBandwidth(userName);
                                if(null == bandwidthId){
                                    bandwidthId = 1;
                                }
                            }
                        }
                    }
                } else {
                    AccountUser accountUser = userService.getDetail(userName, 1);
                    try {
                        if(null!=accountUser && authParam.getPassword()
                                .equals(EncryptUtils.decodeBase64String(accountUser.getPwd()))){
                            macPrior = true;
                            bandwidthId = accountUser.getBandId();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if(authMethod ==2 || authMethod == 5
                    || authMethod == 4 || authMethod == 6 || authMethod == 9){
                //短信认证，授权认证，一键认证，二维码认证
                QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                queryWrapper.eq("user_name", "portalDefaultAccount");
                Employee employee = employeeService.getOne(queryWrapper);
                databasePassword = employee.getPassword();
                if(null!=ssidModel && "1".equals(ssidModel.getIsEmployee())){
                    bandwidthId = authMethod;
                }
            } else if(authParam.getAuthMethod() == 3){
                //微信认证
                QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                queryWrapper.eq("user_name", "portalDefaultWxAccount");
                Employee employee = employeeService.getOne(queryWrapper);
                databasePassword = employee.getPassword();
                if(null!=ssidModel && "1".equals(ssidModel.getIsEmployee())){
                    bandwidthId = defaultOnlinePolicy.getBandwidthId();
                }
            } else if(authParam.getAuthMethod() == 7){
                //钉钉认证
                QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                queryWrapper.eq("user_name", "portalDefaultDingTalkAccount");
                Employee employee = employeeService.getOne(queryWrapper);
                databasePassword = employee.getPassword();
                if(null!=ssidModel && "1".equals(ssidModel.getIsEmployee())){
                    bandwidthId = defaultOnlinePolicy.getBandwidthId();
                }

            } else if(authParam.getAuthMethod() == 99){
//                QueryWrapper<Employee> queryWrapper = new QueryWrapper();
//                queryWrapper.eq("is_valid", 1);
//                queryWrapper.like("bind_macs", userName.toLowerCase());
//                Employee employee = employeeService.getOne(queryWrapper);
//                if (null != employee) {
//                    macPrior = true;
//                } else {
//                    QueryWrapper<WhiteList> whiteWrapper = new QueryWrapper<>();
//                    whiteWrapper.eq("value", userMac.toLowerCase());
//                    whiteWrapper.eq("type", 2);
//                    WhiteList whiteList = whiteListService.getOne(whiteWrapper);
//                    if(null!=whiteList){
//                        macPrior = true;
//                    }
//                }
                AccountUser accountUser = userService.getDetail(userName, 1);
                try {
                    if(null!=accountUser && authParam.getPassword()
                            .equals(EncryptUtils.decodeBase64String(accountUser.getPwd()))){
                        macPrior = true;
                        databasePassword = accountUser.getPwd();
                        bandwidthId = accountUser.getBandId();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        log.warn("====认证方式：" + authParam.getAuthMethod());

        if(!macPrior) {
            if (isChap) {// Chap模式
                byte[] challengeByte = RadiusUtil.HexToByte(challenge);
                byte[] chapIdByte = new byte[2];
                chapIdByte[1] = (byte) Integer.parseInt(
                        reqPassword.substring(0, 2), 16);

                // 用数据库密码生成chapPassword
                byte[] chapPassword = PortalUtil.makeChapPassword(
                        chapIdByte, challengeByte, databasePassword.getBytes());
                String chapPasswordHex = RadiusUtil.ByteToHex(chapPassword);

                // 数据库密码生成的chapPassword和请求的chapPassword比较
                reqPassword = reqPassword.substring(2);
                log.debug(ip + " Database ChapPWD=" + chapPasswordHex + ":::: Req ChapPWD=" + reqPassword);
                if (reqPassword.equals(chapPasswordHex)) {
                    checkResult = "0";// 密码正确
                }
            } else {// Pap模式
                // 如果密码是乱码，检查ac设备的sharekey或者重新设置一遍ac设备的sharekey
                reqPassword = RadiusUtil.secretAuthenMD5(sharedSecretHex,
                        authenticator, reqPassword);
                log.debug(ip + " Database PWD=" + databasePassword
                        + ":::: Req PWD=" + reqPassword);
                if (reqPassword.equals(databasePassword)) {
                    checkResult = "0";// 密码正确
                }
            }
        } else {
            checkResult = "0";
        }

        // 密码错误，返回
        if (checkResult == null) {
            // 密码解密校验不正确
            typeName = "Access-Reject";
            code = 3;
            attributes = RadiusUtil.getAttributeString(18,
                    "code=101,Login Failed. Please check your password");
            log.debug(ip + " code=101,Login Failed. Please check your password");
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
            return ret;
        }

        log.info("checkResult=: " + checkResult);
        // 其他错误，返回
        if (!checkResult.equals("0")) {
            if (checkResult.equals("-1")) {
                // 账户欠费
                attributes = RadiusUtil.getAttributeString(18,
                        "code=106,Login Failed. Your account has expired");
                log.debug(ip + " code=106,Login Failed. Your account has expired");
            } else if (checkResult.equals("-2")) {
                // 共享数
                attributes = RadiusUtil.getAttributeString(18,
                        "code=105,Login Failed. Your account simultaneous connections limited");
                log.debug(ip +
                        " code=105,Login Failed. Your account simultaneous connections limited");
            } else if (checkResult.equals("-3")) {
                // MAC未绑定
                attributes = RadiusUtil.getAttributeString(18,
                        "code=102,Login Failed. Your account mac address is bind");
                log.debug(ip + " code=102,Login Failed. Your account mac address is bind");
            } else if (checkResult.equals("-4")) {
                // MAC限制
                attributes = RadiusUtil.getAttributeString(18,
                        "code=107,Login Failed. Your Mac address limited");
                log.debug(ip + " code=107,Login Failed. Your Mac address limited");
            } else if (checkResult.equals("-5")) {
                // 用户名不存在
                attributes = RadiusUtil.getAttributeString(18,
                        "code=101,Login Failed. Please check your username");
                log.debug(ip + " code=101,Login Failed. Please check your username");
            } else if (checkResult.equals("-6")) {
                // NAS 绑定
                attributes = RadiusUtil.getAttributeString(18,
                        "code=103,Login Failed. Your account nas ip address is bind");
                log.debug(ip + " code=103,Login Failed. Your account nas ip address is bind");
            } else {
                attributes = RadiusUtil.getAttributeString(18,
                        "code=108,Login Failed. Unknow Error");
                log.debug(ip + " code=108,Login Failed. Unknow Error");
            }
            code = 3;
            typeName = "Access-Reject";
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
            return ret;
        }

        // 组装attributes
        if (!(userIp == null || "".equals(userIp))) {
            attributes = attributes + RadiusUtil.getAttributeIP(userIp);
        }
        if (StringUtil.isNotBlank(nasIp)) {
            attributes = attributes + RadiusUtil.getAttributeNasIP(nasIp);
        }
        if (StringUtil.isNotBlank(packetUserName)) {
            attributes = attributes
                    + RadiusUtil.getAttributeString(1, packetUserName);
        }
        if (StringUtil.isNotBlank(acctSessionId)) {
            attributes = attributes
                    + RadiusUtil.getAttributeString(44, acctSessionId);
        }
        attributes = attributes + RadiusUtil.getAttributeString(18,
                "code=0,Login Success.");
//        attributes = attributes + RadiusUtil.getAttributeInt(27,
//                sessionTime); // 重新认证时长
//        if (StringUtil.isNotBlank(idleTimeout)) {// 空闲时长
//            Integer idletime = Integer.valueOf(idleTimeout);
//            attributes = attributes + RadiusUtil.getAttributeInt(28,
//                    idletime);
//        }
//        if (StringUtil.isNotBlank(acctInterimInterval)) {// accounting间隔
//            Integer acctInterval = Integer.valueOf(acctInterimInterval);
//            attributes = attributes + RadiusUtil.getAttributeInt(85,
//                    acctInterval);
//        }

        log.debug("radiusService userIp " + userIp);
        if (null != authParam) {
            log.debug(" authParam " + authParam.toString());
        }/* else {
            // 测试radius服务，返回成功
            typeName = "Access-Accept";
            code = 2;
            log.debug(ip + " code=2,Login Success.");
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip, port,
                    code, identifier, authenticator, attributes);
            return ret;
        }*/

        if (bandwidthId == null) {
            // 查询上网策略
            if(null == authParam.getAuthMethod()) {
                authParam.setAuthMethod(Constant.AuthMethod.ONEKEY_AUTH);
            }
            AuthMethod authMethodEntity = authMethodService.getById(authParam.getAuthMethod());
            Integer policyId = authMethodEntity.getCustomPolicyId();
            OnlinePolicy userOnlinePolicy = onlinePolicyService.getById(policyId);
            if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
                bandwidthId = userOnlinePolicy.getBandwidthId();
            }

            // 没配置自定义带宽ID，则使用系统默认带宽
            if (bandwidthId == null) {
                bandwidthId = defaultOnlinePolicy.getBandwidthId();
            }
        }

        // 系统带宽
        if (bandwidthId == null) {
            log.error("系统配置错误，没有访客带宽");
            typeName = "Access-Reject";
            code = 3;
            attributes = RadiusUtil.getAttributeString(18,
                    "code=101,Login Failed. Guest Bandwidth empty");
            log.debug(ip + " code=101,Login Failed. Guest Bandwidth empty");
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
            return ret;
        }

        // 设置带宽，华为、新华三需要设置，优科、思科不需设置
        Bandwidth bandwidth = bandwidthService.getById(bandwidthId);
        log.debug("radius set bandwidth " + bandwidth.toString());
        if (bandwidth == null) {
            log.error("系统配置错误，带宽ID查询为空");
            typeName = "Access-Reject";
            code = 3;
            attributes = RadiusUtil.getAttributeString(18,
                    "code=101,Login Failed. Bandwidth null");
            log.debug(ip + " code=101,Login Failed. Bandwidth null");
            ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
            return ret;
        }
        BaseResult acInfoResult = acService.getInfoByAcIp(ac.getIp());
        Ac acInfo = (Ac) acInfoResult.getData();
        String brandCode = acInfo.getBrand().getCode();
        if (brandCode.contains("huawei") || brandCode.contains("ruijie")) {// 华为设备
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 2011);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    5, (int) Math.floor(bandwidth.getDownstreamRate() * 1024));// 下行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 2011);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    6, (int) Math.floor(bandwidth.getBurstDownstreamRate() * 1024));// 最大下行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 2011);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    2, (int) Math.floor(bandwidth.getUpstreamRate() * 1024));// 上行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 2011);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    3, (int) Math.floor(bandwidth.getBurstUpstreamRate() * 1024));// 最大上行速率，bps
        } else if (brandCode.contains("h3c")) {// 新华三
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 25506);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    5, (int) Math.floor(bandwidth.getDownstreamRate() * 1024));// 下行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 25506);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    4, (int) Math.floor(bandwidth.getBurstDownstreamRate() * 1024));// 最大下行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 25506);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    2, (int) Math.floor(bandwidth.getUpstreamRate() * 1024));// 上行速率，bps
            attributes = attributes + RadiusUtil.getAttributeVendor(26, 25506);
            attributes = attributes + RadiusUtil.getAttributeSpeed(
                    1, (int) Math.floor(bandwidth.getBurstUpstreamRate() * 1024));// 最大上行速率，bps
        }

        // 二维码认证时，没有经过ac重定向，authParam中没有存入userMac
        if (authParam.getUserMac() == null) {
            authParam.setUserMac(userMac);
        }
        /******!!!!CISCO跳转不传userMac，需要在radius中获取回填到authParam******/
        authParamService.saveOrUpdate(authParam);
        log.info("radius更新认证参数：===" + authParam.toString());
        // 新增登录记录
        BaseResult addAuthRecord = authRecordService.add(authParam, macPrior);
        log.info("加入认证记录：===" + addAuthRecord.toString());
        // 新增或更新用户记录，
        // 只能在这里调用，不能在portalApi返回之后调用，也不能在RadiusAccountService中调用，
        // 因为“portalApi返回”和“RadiusAccount报文”会在radius返回成功后同时发起，不能控制先后，同时对authUser表操作会有线程不同步问题

        AuthUser authUser = getAuthUser(authParam, macPrior);
        log.info("更新在线用户：===" + authUser.toString());
        authUserService.saveOrUpdateByMac(authUser);

        if(1 == ac.getIsWhitelistEnable()){
            if(authParam.getAuthMethod() == 1){
                if(!isAccount){
                    QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                    queryWrapper.eq("is_valid", 1);
                    queryWrapper.eq("user_name", authUser.getUserName());
                    Employee employee = employeeService.getOne(queryWrapper);
                    if(null != employee && employee.getIsBindMac() == 1){
                        //mac是否已经添加到其他用户
                        QueryWrapper<Employee> subWrapper = new QueryWrapper();
                        subWrapper.like("bind_macs", userMac.toLowerCase());
                        List<Employee> existEmployees = employeeService.list(subWrapper);
                        if(CollectionUtils.isEmpty(existEmployees)){
                            if(null!=employee.getBindMacs()){
                                if(1==employee.getIsTerminalNumLimit()&&employee.getBindMacs().split(",").length<employee.getTerminalNum()){
                                    if (!employee.getBindMacs().contains(userMac.toLowerCase())) {
                                        if(StringUtils.isNotBlank(employee.getBindMacs())) {
                                            employee.setBindMacs(employee.getBindMacs() + "," + userMac.toLowerCase());
                                        }else {
                                            employee.setBindMacs(userMac.toLowerCase());
                                        }
                                    }
                                }
                            } else {
                                employee.setBindMacs(userMac.toLowerCase());
                            }
                            employeeService.updateById(employee);
                        }
                    }
                } else {
                    AccountUser accountUser = userService.getDetail(authParam.getUserName(), 1);
                    QueryWrapper<AccountUserMac> userMacWrapper = new QueryWrapper<>();
                    userMacWrapper.eq("login_name", authParam.getUserName());
                    List<AccountUserMac> userMacs = userMacService.list(userMacWrapper);
                    if (CollectionUtils.isEmpty(userMacs) || accountUser.getBindMacNum()==0
                            || userMacs.size() < accountUser.getBindMacNum()) {
                        AuthParam finalAuthParam = authParam;
                        List<AccountUserMac> macsTemp = userMacs.stream().filter(item -> item.getMac().equals(finalAuthParam.getUserMac()))
                                .collect(Collectors.toList());
                        if(CollectionUtils.isEmpty(macsTemp)){
                            AccountUserMac macObj = new AccountUserMac();
                            macObj.setLoginName(authParam.getUserName());
                            macObj.setUserId(accountUser.getId());
                            macObj.setMac(authParam.getUserMac());

                            userMacService.save(macObj);
                        }
                    }
                }

            } else if (authParam.getAuthMethod() == 2) {
                if(1==ssidModel.getIsEmployee()){
                    //短信认证,把MAC加入员工绑定mac
                    QueryWrapper<Employee> queryWrapper = new QueryWrapper();
                    queryWrapper.eq("is_valid", 1);
                    queryWrapper.eq("phone", authUser.getShowUserName());
                    Employee employee = employeeService.getOne(queryWrapper);
                    if (null != employee) {
                        if(null!=employee.getBindMacs()){
                            if(1==employee.getIsTerminalNumLimit()&&employee.getBindMacs().split(",").length<employee.getTerminalNum()){
                                if (!employee.getBindMacs().contains(userMac.toLowerCase())) {
                                    if(StringUtils.isNotBlank(employee.getBindMacs())) {
                                        employee.setBindMacs(employee.getBindMacs() + "," + userMac.toLowerCase());
                                    }else {
                                        employee.setBindMacs(userMac.toLowerCase());
                                    }
                                    employeeService.updateById(employee);
                                }
                            }
                        }

                    } else {
                        //新员工首次短信认证
                        List<LdapUser> users = adService.findUser(authUser.getShowUserName());
                        if(CollectionUtils.isNotEmpty(users)){
                            log.info("===ldapUser: "+ users.get(0));
                            employee = new Employee();
                            employee.setFullName(users.get(0).getUserCn());
                            employee.setUserName(users.get(0).getUid());
                            employee.setPassword(users.get(0).getUserPassword());
                            employee.setDepartmentId(1);
                            employee.setPhone(users.get(0).getMobile());
                            employee.setBandwidthId(8);
                            employee.setTerminalNum(15);
                            employee.setIsBindMac(1);
                            employee.setBindMacs(userMac.toLowerCase());
                            employee.setSex((int)(Math.random()*2));
                            employee.setIsEmployeeAuthEnable(1);
                            employee.setIsValid(1);
                            employee.setIsUsing(1);

                            employeeService.save(employee);
                        }
                    }
                } else {
                    QueryWrapper<WhiteList> whiteQueryWrapper = new QueryWrapper<WhiteList>().eq("type", 2)
                            .eq("value", authParam.getUserMac().toLowerCase());
                    List<WhiteList> list = whiteListService.list(whiteQueryWrapper);
                    if(CollectionUtils.isEmpty(list)) {
                        // 查询上网策略
                        AuthMethod authMethodEntity = authMethodService.getById(Constant.AuthMethod.SMS_AUTH);
                        Integer policyId = authMethodEntity.getCustomPolicyId();
                        OnlinePolicy userOnlinePolicy = onlinePolicyService.getById(policyId);
                        Integer onlinePeriod = 60;  //默认60分钟
                        if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
                            onlinePeriod = userOnlinePolicy.getOnlinePeriod();
                        } else {
                            userOnlinePolicy = onlinePolicyService.getById(100);
                            if(userOnlinePolicy.getIsPeriodLimit()==1){
                                onlinePeriod = userOnlinePolicy.getOnlinePeriod();
                            } else {
                                onlinePeriod = 99 * 365 * 24 * 60;
                            }
                        }

                        //查询用户mac对应的手机号
                        AuthParam tempParam = authParamService.getByUserMac(userMac);

                        WhiteList whiteList = new WhiteList();
                        if (null != tempParam) {
                            whiteList.setUserName(tempParam.getPhone());

                            //check bind mac count whether exceed 3.
                            QueryWrapper<WhiteList> checkWrapper = new QueryWrapper<>();
                            checkWrapper.eq("user_name", tempParam.getPhone());
                            checkWrapper.eq("type", 2);

                            List<WhiteList> whiteLists = whiteListService.list(checkWrapper);
                            if (CollectionUtils.isEmpty(whiteLists) || whiteLists.size() < 3) {
                                //1手机，2MAC，3IP
                                whiteList.setType(2);
                                whiteList.setValue(authParam.getUserMac().toLowerCase());
                                log.info("===插入时间:" + (new DateTime().plusMinutes(onlinePeriod)).toDate());
                                whiteList.setExpireTime((new DateTime().plusMinutes(onlinePeriod)).toDate());
                                whiteList.setIsValid(1);
                                whiteListService.save(whiteList);
                            }
                        } else {
                            whiteList.setUserName(userName);
                            //1手机，2MAC，3IP
                            whiteList.setType(2);
                            whiteList.setValue(authParam.getUserMac().toLowerCase());
                            log.info("===插入时间:" + (new DateTime().plusMinutes(onlinePeriod)).toDate());
                            whiteList.setExpireTime((new DateTime().plusMinutes(onlinePeriod)).toDate());
                            whiteList.setIsValid(1);
                            whiteListService.save(whiteList);
                        }

                    }
                }

            } else if(authParam.getAuthMethod() == 7){
//            if(userName.equals("portalDefaultDingTalkAccount")) {
//                QueryWrapper<Employee> employeeWrapper = new QueryWrapper();
//                employeeWrapper.eq("is_valid", 1);
//                employeeWrapper.eq("phone", authParam.getPhone());
//                Employee employeeDb = employeeService.getOne(employeeWrapper);
//                if (null != employeeDb) {
//                    if(null!=employeeDb.getBindMacs()){
//                        if (!employeeDb.getBindMacs().contains(userMac.toLowerCase())) {
//                            if (StringUtils.isNotBlank(employeeDb.getBindMacs())) {
//                                employeeDb.setBindMacs(employeeDb.getBindMacs() + "," + userMac.toLowerCase());
//                            } else {
//                                employeeDb.setBindMacs(userMac.toLowerCase());
//                            }
//                            employeeService.updateById(employeeDb);
//                        }
//                    } else {
//                        employeeDb.setBindMacs(userMac.toLowerCase());
//                        employeeService.updateById(employeeDb);
//                    }
//
//                } else {
//                    List<LdapUser> users = adService.findUser(authParam.getPhone());
//                    if(CollectionUtils.isNotEmpty(users)){
//                        employeeDb = new Employee();
//                        employeeDb.setFullName(users.get(0).getUserCn());
//                        employeeDb.setUserName(users.get(0).getUid());
//                        employeeDb.setPassword(users.get(0).getUserPassword());
//                        employeeDb.setDepartmentId(1);
//                        employeeDb.setPhone(users.get(0).getMobile());
//                        employeeDb.setBandwidthId(8);
//                        employeeDb.setTerminalNum(15);
//                        employeeDb.setIsBindMac(1);
//                        employeeDb.setBindMacs(userMac.toLowerCase());
//                        employeeDb.setSex((int)(Math.random()*2));
//                        employeeDb.setIsEmployeeAuthEnable(1);
//                        employeeDb.setIsValid(1);
//                        employeeDb.setIsUsing(1);
//
//                        employeeService.save(employeeDb);
//                    }
//                }
//            }
            }
        }

        //登录成功，如果ac打开无感知，把用户mac自动加入白名单
        if (1 == ac.getIsWhitelistEnable() && StringUtils.contains("3,5,6", authParam.getAuthMethod() +"")) {
            QueryWrapper<WhiteList> whiteQueryWrapper = new QueryWrapper<WhiteList>().eq("type", 2)
                    .eq("value", authParam.getUserMac().toLowerCase());
            List<WhiteList> list = whiteListService.list(whiteQueryWrapper);
            if(CollectionUtils.isEmpty(list)) {
                // 查询上网策略
                AuthMethod authMethodEntity = authMethodService.getById(Constant.AuthMethod.EMPLOYEE_AUTH);
                Integer policyId = authMethodEntity.getCustomPolicyId();
                OnlinePolicy userOnlinePolicy = onlinePolicyService.getById(policyId);
                //默认60分钟
                Integer onlinePeriod = 60;
                // 自定义上网策略
                if (authMethodEntity.getUseCustomPolicy() == 1) {
                    onlinePeriod = userOnlinePolicy.getOnlinePeriod();
                } else {
                    userOnlinePolicy = onlinePolicyService.getById(100);
                    if(userOnlinePolicy.getIsPeriodLimit()==1){
                        onlinePeriod = userOnlinePolicy.getOnlinePeriod();
                    } else {
                        onlinePeriod = 99 * 365 * 24 * 60;
                    }
                }

                WhiteList whiteList = new WhiteList();
                whiteList.setUserName(userName);
                whiteList.setType(2);  //1手机，2MAC，3IP
                whiteList.setValue(authParam.getUserMac().toLowerCase());
                log.info("===插入时间:" + (new DateTime().plusMinutes(onlinePeriod)).toDate());
                whiteList.setExpireTime((new DateTime().plusMinutes(onlinePeriod)).toDate());
                whiteList.setIsValid(1);
                whiteListService.save(whiteList);
            }
        }

        // 返回成功
        typeName = "Access-Accept";
        code = 2;
        log.debug(ip + " code=2,Login Success.");
        ret = RadiusUtil.getOutData(typeName, sharedSecretHex, ip, port,
                code, identifier, authenticator, attributes);
        return ret;
    }

    private AuthUser getAuthUser(AuthParam authParam, boolean macPrior) {
        AuthUser authUser = new AuthUser();
        Integer authMethod = authParam.getAuthMethod();
        authUser.setAuthMethod(authMethod);
        authUser.setPhone(authParam.getPhone());
        Integer userType;
        if (null!= authParam.getUserName() &&
                authParam.getUserName().equals("portalDefaultAccount")) {
            userType = 1;// 访客
        } else {
            userType = 0;// 员工
        }
        authUser.setUserType(userType);
        // 员工授权认证，记录访客姓名、电话、授权员工ID
        if (authMethod == Constant.AuthMethod.EMPLOYEE_AUTH) {
            authUser.setFullName(authParam.getGuestName());
            authUser.setPhone(authParam.getGuestPhone());
            // 根据userName查找授权员工ID
            Employee authEmployee = employeeService.getById(authParam.getAuthEmployeeId());
            authUser.setAuthEmployeeId(null != authEmployee ? authEmployee.getId() : -1);
            authUser.setAuthEmployeeName(null != authEmployee ? authEmployee.getUserName(): "");
            // 其他认证方式，记录userName
        }
        authUser.setUserName(authParam.getUserName());

        // 用于展示的用户账号
        if (authMethod == Constant.AuthMethod.ACCOUNT_AUTH) {
//            authUser.setShowUserName(authParam.getUserName());
            authUser.setShowUserName(authParam.getGuestName());
        } else if (authMethod == Constant.AuthMethod.SMS_AUTH) {
            authUser.setShowUserName(authParam.getPhone());
        } else if (authMethod == Constant.AuthMethod.WX_AUTH) {
            authUser.setShowUserName("微信用户");
        } else if (authMethod == Constant.AuthMethod.ONEKEY_AUTH) {
            authUser.setShowUserName("一键登录账号");
        } else if (authMethod == Constant.AuthMethod.EMPLOYEE_AUTH) {
            if(StringUtils.isNotBlank(authParam.getGuestName())) {
                authUser.setShowUserName(authParam.getGuestName());
            } else {
                authUser.setShowUserName("默认账户");
            }
            authUser.setAuthEmployeeId(authParam.getAuthEmployeeId());
            authUser.setAuthEmployeeName(authParam.getAuthEmployeeName());
        } else if (authMethod == Constant.AuthMethod.QRCODE_AUTH) {
            authUser.setShowUserName(authParam.getGuestName());
        } else if (authMethod == Constant.AuthMethod.DING_TALK_AUTH) {
            authUser.setShowUserName(authParam.getGuestName());
        }

        authUser.setIp(authParam.getUserIp());
        authUser.setMac(authParam.getUserMac());
        authUser.setAcIp(authParam.getAcIp());
        authUser.setAcMac(authParam.getAcMac());
        authUser.setSsid(authParam.getSsid());
        authUser.setApIp(authParam.getApIp());
        authUser.setApMac(authParam.getApMac());
        authUser.setIsWired(authParam.getIsWired());
        authUser.setNasIp(authParam.getNasIp());
        authUser.setTerminalType(authParam.getTerminalType());
//        authUser.setOnlineState(1);
        authUser.setLastOnlineTime(new Date());
        authUser.setIsValid(1);
        authUser.setUpdateTime(new Date());
        authUser.setMacPrior(macPrior ? 1 : 0);
        authUser.setPhone(authParam.getPhone());
        return authUser;
    }

    // Access-Challenge()，未使用
    private byte[] accessChallenge(String ip, int port, int identifier, String authenticator,
                                   String[][] attrList) {
        byte[] ret = null;
        try {
            for (int i = 0; i < attrList.length; i++) {
                try {
                    int type = Integer.parseInt(attrList[i][0], 16);
                    RadiusUtil.getAttributeValue(ip, type, attrList[i][1]).trim();
                } catch (Exception e1) {
                    log.error("Error " + e1.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }
        return ret;
    }

    // Status-Server(试验阶段)，未使用
    private byte[] statusServer(String ip, int port, int identifier, String authenticator, String[][] attrList) {
        byte[] ret = null;
        for (int i = 0; i < attrList.length; i++) {
            try {
                int type = Integer.parseInt(attrList[i][0], 16);
                RadiusUtil.getAttributeValue(ip, type, attrList[i][1]).trim();
            } catch (Exception e1) {
                log.error("Error " + e1.getMessage());
            }
        }
        return ret;
    }

    // Status-Client(试验阶段)，未使用
    private byte[] statusClient(String ip, int port, int identifier, String authenticator, String[][] attrList) {
        byte[] ret = null;
        for (int i = 0; i < attrList.length; i++) {
            try {
                int type = Integer.parseInt(attrList[i][0], 16);
                RadiusUtil.getAttributeValue(ip, type, attrList[i][1]).trim();
            } catch (Exception e1) {
                log.error("Error " + e1.getMessage());
            }
        }
        return ret;
    }

    // Reserved(保留)，未使用
    private byte[] reserved(String ip, int port, int identifier, String authenticator, String[][] attrList) {
        byte[] ret = null;
        for (int i = 0; i < attrList.length; i++) {
            try {
                int type = Integer.parseInt(attrList[i][0], 16);
                RadiusUtil.getAttributeValue(ip, type, attrList[i][1]).trim();
            } catch (Exception e1) {
                log.error("Error " + e1.getMessage());
            }
        }
        return ret;
    }

    private String domainToIp(String domain) {
        String ip = "";
        try {
            ip = InetAddress.getByName(domain).toString().split("/")[1];
        } catch (UnknownHostException e) {
            log.error("Radius domainToIp ERROR INFO " + e.getMessage());
        }
        log.debug("Domain:" + domain + " IP:" + ip);
        return ip;
    }

}
