package cn.ushare.account.admin.radius.service;

import cn.ushare.account.admin.config.ApplicationContextProvider;
import cn.ushare.account.admin.portal.service.PortalUtil;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.service.impl.AdServiceImpl;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Radius计费、计时、流量统计服务
 * 注意：
 *  1.h3c设备计费频率使用Radius设置无效，要在ac设备上设置
 */
@Slf4j
public class RadiusAccountService implements Runnable {

    private Integer listenPort = 1813;// 监听端口，固定值
    private boolean isRunning = false;
    private DatagramSocket socket = null;
    private final static String DEBUG_LABEL = "debug====  ";

    AcService acService;
    AuthRecordService authRecordService;
    AuthUserService authUserService;
    AuthParamService authParamService;
    EmployeeService employeeService;
    AdService adService;

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

    public RadiusAccountService() {
        this.acService = ApplicationContextProvider.getBean(AcService.class);
        this.authRecordService = ApplicationContextProvider.getBean(AuthRecordService.class);
        this.authUserService = ApplicationContextProvider.getBean(AuthUserService.class);
        this.authParamService = ApplicationContextProvider.getBean(AuthParamService.class);
        this.employeeService = ApplicationContextProvider.getBean(EmployeeService.class);
        this.adService = ApplicationContextProvider.getBean(AdServiceImpl.class);
    }

    @Override
    public void run() {
        log.debug("RadiusAccountService run");
        try {
            socket = new DatagramSocket(listenPort);
            isRunning = true;
            while (isRunning) {
                try {
                    byte[] buf = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buf, 4096);
                    socket.receive(packet);// 接收数据
//                    handlerData(packet);// 解析数据，返回响应

                    //2023-2-22 改为多线程(加入线程池)
                    // 异步处理
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

    // 接收数据
    public void handlerData(DatagramPacket in) {
        log.debug("radius account receive");
        try {
            String ip = in.getAddress().getHostAddress();
            int port = in.getPort();
            // byte[] inData = new byte[in.getLength()];
            // System.arraycopy(in.getData(), 0, inData, 0, inData.length);
            byte[] inData = in.getData();
            // 包类型
            int code = inData[0];
            // 包标识符：用于匹配请求包和响应包
            byte[] id = new byte[1];
            id[0] = inData[1];
            int identifier = RadiusUtil.ByteToInt(id);
            // int identifier = inData[1];
            // 包长度：code、identifier、length、authenticator、attributes的长度总和，有效范围是20~4096
            // int length = (inData[2] << 8) | inData[3];
            int length = in.getLength();
            // 验证字：
            // (1)请求验证字(Request Authenticator)，用在请求报文中，必须为全局唯一的随机值
            // (2)响应验证字(Response Authenticator)，用在响应报文中，用于鉴别响应报文的合法性，
            // 响应验证字=MD5(Code+ID+Length+请求验证字+Attributes+SharedSecret)
            String authenticator = RadiusUtil.ByteToHex(inData, 4, 20);
            // 属性域
            String attributes = RadiusUtil.ByteToHex(inData, 20, length);
            log.error("Accounting Receive ", "ip=" + ip + ",port=" + port
                    + ",code=" + code + ",identifier=" + identifier
                    + ",length=" + length + ",authenticator=" + authenticator
                    + ",attributes=" + attributes);
            String[][] attributesList = null;
            if ((attributes != null) && (attributes.length() > 0)) {
                attributesList = RadiusUtil.getAttributes(attributes);
            }
            byte[] outData = optionData(code, ip, port, identifier,
                    authenticator, attributesList);
            if (outData != null) {
                DatagramPacket out = new DatagramPacket(outData,
                        outData.length, in.getSocketAddress());
                socket.send(out);
                log.debug(ip, "Accounting Send OK !!");
            }
        } catch (Exception e) {
            log.error("Accounting Error Exception=", e);
        }
    }

    private byte[] optionData(int code, String ip, int port, int identifier,
            String authenticator, String[][] attributesList) {
        byte[] ret = null;
        try {
            // 1接入请求报文，2接入成功回应报文，3接入拒绝回应报文，4计费请求报文
            // 5计费回应报文，11接入挑战报文，12服务器状态报文（试验），
            // 13客户端状态报文（试验），255保留
            switch (code) {
            case 4:// Accounting-Request(计费请求数据包)
            {
                log.debug(ip + " Accounting>>Accounting-Request");
                ret = accountingRequest(ip, port, identifier, authenticator,
                        attributesList);
            }
                break;
            default: {
                log.debug(ip + " Accounting code ERROR (" + code + ")");
            }
                break;
            }
        } catch (Exception e) {
            log.error("Accounting Error Exception=", e);
        }
        return ret;
    }

    // Accounting-Request(计费请求数据包)
    private byte[] accountingRequest(String ip, int port, int identifier,
            String authenticator, String[][] attributesList) {
        byte[] ret = null;
        try {
            String inS = "0";
            String outS = "0";
            String name = null;
            String userIp = null;
            String nasIp = null;
            String acctSessionId = null;
            String callingStationId = null;// 客户端mac
            String acctSessionTime = null;
            String acctType = null;
            String nasIdentifier = "";
            for (int i = 0; i < attributesList.length; i++) {
                try {
                    int type = Integer.parseInt(attributesList[i][0], 16);
                    String value = RadiusUtil.getAttributeValue(ip, type,
                            attributesList[i][1]).trim();
                    switch (type) {
                    case 1:
                    {
                        // User-Name(用户账户ID)
                        name = value;
                    }
                        break;
                    case 4:
                    {
                        // Nas-IP-Address(Nas的ip地址)
                        nasIp = value;
                    }
                        break;
                    case 5:
                        // Nas-Port(用户接入端口号)
                    {
                        port = Integer.parseInt(value);
                    }
                        break;
                    case 8:
                    {
                        // Framed-IP-Address(为用户提供的IP地址)
                        userIp = value;
                    }
                        break;
                    case 31:
                    {
                        // Calling-Station-Id(mac地址)， 客户端mac
                        callingStationId = value;
                    }
                        break;
                    case 32:
                        // Nas-Identifier(标识NAS的字符串)
                    {
                        nasIdentifier = value;
                    }
                        break;
                    case 40:
                        // Acct-Status-Type(计费请求报文的类型)
                    {
                        acctType = value;
                    }
                        break;
                    case 42:
                        // Acct-Input-Octets(上行)
                    {
                        inS = value;
                    }
                        break;
                    case 43:
                        // Acct-Output-Octets(下行)
                    {
                        outS = value;
                    }
                        break;
                    case 44:
                        // Acct-Session-Id(计费会话标识)
                    {
                        acctSessionId = value;
                    }
                        break;
                    case 46:
                        // Acct-Session-Time(通话时长(用户在线时长）)
                    {
                        acctSessionTime = value;
                    }
                        break;

                    default:
                        break;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    log.error("Accounting Error" + e1.getMessage());
                }
            }

            String userMac = null;
            if (StringUtil.isNotBlank(callingStationId)) {
                log.debug("RadiusAccount callingStationId " + callingStationId);
                // callingStationId = PortalUtil.MacFormat(callingStationId);
                userMac = PortalUtil.MacFormat1(callingStationId);
            }

            AuthParam authParam = authParamService.getByUserIp(userIp);
            if(null==authParam){
                authParam = authParamService.getByUserMac(userMac);
                if(null!=authParam) {
                    authParam.setUserIp(userIp);
                    authParamService.saveOrUpdate(authParam);
                }
            }
            if (userMac == null) {
                // 没有callingStationId，则从authParam里面读取
                log.debug("RadiusAccount get userMac from authParam");
                if (authParam != null) {
                    userMac = authParam.getUserMac();
                }
            }

            log.debug(DEBUG_LABEL + "userMac " + userMac);
            log.debug(DEBUG_LABEL + "Radius account userIp " + userIp + " userName " + name + " AcctType " + acctType + " inS " + inS
                    + " outS " + outS + " acctSessionId " + acctSessionId + " callingStationId " + callingStationId);
            // 流量是从认证成功开始算的总流量
            Long downFlow = Long.parseLong(outS);
            Long upFlow = Long.parseLong(inS);

            // 更新流量到authUser
            AuthUser authUser = new AuthUser();
            if (acctType.startsWith("Start")) {
                authUserService.resetFlow(userIp, userMac);// 重置authUser流量
                authUser.setLastOnlineTime(new Date());
                // 更新authRecord
                authRecordService.updateAccountInfo(userIp, userMac, upFlow, downFlow, acctSessionId, 1);
            }
            Date now = new Date();
            authUser.setMac(userMac);
            authUser.setIp(userIp);
            authUser.setUpDataFlow(upFlow);
            authUser.setDownDataFlow(downFlow);
            authUser.setAcctSessionId(acctSessionId);
            authUser.setUpdateTime(now);
            authUser.setOnlineState(1);
            authUserService.updateByMac(authUser);

            //修复钉钉同步问题
//            if(StringUtils.isNotBlank(authParam.getPhone())){
//                Employee employee = employeeService.getOne(new QueryWrapper<Employee>()
//                        .eq("phone", authParam.getPhone()));
//                if(null == employee){
//                    List<LdapUser> users = adService.findUser(authParam.getPhone());
//                    if(CollectionUtils.isNotEmpty(users)){
//                        employee = new Employee();
//                        employee.setFullName(users.get(0).getUserCn());
//                        employee.setUserName(users.get(0).getUid());
//                        employee.setPassword(users.get(0).getUserPassword());
//                        employee.setDepartmentId(1);
//                        employee.setPhone(users.get(0).getMobile());
//                        employee.setBandwidthId(8);
//                        employee.setTerminalNum(15);
//                        employee.setIsBindMac(1);
//                        employee.setBindMacs(userMac.toLowerCase());
//                        employee.setSex((int)(Math.random()*2));
//                        employee.setIsEmployeeAuthEnable(1);
//                        employee.setIsValid(1);
//                        employee.setIsUsing(1);
//
//                        employeeService.save(employee);
//                    }
//                }
//            }

            // 下线消息，ruckus有效（关闭wifi马上收到），h3c无效，华为未测试
            if (acctType.startsWith("Stop")) {
                authUserService.updateOfflineState(userMac);
                // 更新authRecord
                authRecordService.updateAccountInfo(userIp, userMac, upFlow, downFlow, acctSessionId, 0);
            }

            String attributes = "";
            // code值：
            // 1接入请求报文，2接入成功回应报文，3接入拒绝回应报文，4计费请求报文
            // 5计费回应报文，11接入挑战报文，12服务器状态报文（试验），
            // 13客户端状态报文（试验），255保留
            int code = 5;

            String tempNasIp = nasIp;
            // 根据nasIp查询共享密钥
            QueryWrapper<Ac> acQuery = new QueryWrapper();
            acQuery.eq("is_valid", 1);
            acQuery.and( wrapper-> wrapper.eq("ip", tempNasIp).or().like("nas_ip", tempNasIp));
            Ac ac = acService.getOne(acQuery);
            if (ac == null) {
                log.debug("Radius accountingRequest失败：没有Ip " + nasIp + " 对应的AC设备");
                return ret;
            }
            String sharedSecret = ac.getShareKey();
            if (StringUtil.isBlank(sharedSecret)) {// 检查密钥
                log.debug(ip + " 未知的ac设备 !!");
                return ret;
            }
            String sharedSecretHex = RadiusUtil.ByteToHex(sharedSecret.getBytes());

            log.debug(ip + " Accounting-Request Print Finish !!");
            ret = RadiusUtil.getOutData("accountingResponse", sharedSecretHex, ip,
                    port, code, identifier, authenticator, attributes);
        } catch (Exception e) {
            log.error("Accounting Error Exception=", e);
        }
        return ret;
    }


}
