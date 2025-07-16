package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Portal协议
 * @author jixiang.li
 * @date 2019-03-18
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class PortalApiService {

    @Autowired
    AcService acService;
    @Value("${isCloud}")
    private String isCloud;

    /**
     * 第6步，Request UserInfo
     */
    public BaseResult userInfo(String basIp, Integer basPort,
            Integer timeout, byte[] serialNo, byte[] userIp,
            String sharedSecret, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[50];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr = new byte[4];
        byte[] errorInfo = new byte[1];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[20];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[36];
            reqInfo[0] = (byte) 2;
        } else {
            errorInfo[0] = (byte) 12;
            return new BaseResult("0", "不支持该版本", errorInfo);
        }

        reqInfo[1] = (byte) 9;      // Type，9：REQ_INFO
        reqInfo[2] = (byte) 0;      // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;      // Rsvd，保留字段
        reqInfo[4] = serialNo[0];   // SerialNo，序列号
        reqInfo[5] = serialNo[1];   // SerialNo，序列号
        reqInfo[6] = (byte) 0;      // ReqID
        reqInfo[7] = (byte) 0;      // ReqID
        reqInfo[8] = userIp[0];     // UserIp
        reqInfo[9] = userIp[1];     // UserIp
        reqInfo[10] = userIp[2];    // UserIp
        reqInfo[11] = userIp[3];    // UserIp
        reqInfo[12] = (byte) 0;     // UserPort，保留字段
        reqInfo[13] = (byte) 0;     // UserPort，保留字段
        reqInfo[14] = (byte) 0;     // ErrCode
        reqInfo[15] = (byte) 1;     // AttrNum，属性个数
        reqInfo[16] = (byte) 8;     // Attr
        reqInfo[17] = (byte) 4;     // Attr
        reqInfo[18] = (byte) 0;     // Attr
        reqInfo[19] = (byte) 0;     // Attr

        // 2.0版本协议，增加authenticator字段
        if (portalVersion == 2) {
            // 基础内容，16字节
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }
            // Attr字段
            attr[0] = (byte) 8;
            attr[1] = (byte) 4;
            attr[2] = (byte) 0;
            attr[3] = (byte) 0;

            // 生成Authenticator
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr, sharedSecret.getBytes());
            // Authenticator存入第16-32位
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }
            // Attr加到尾部
            for (int i = 0; i < attr.length; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("REQ Info" + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(
                    reqInfo, reqInfo.length,
                    InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);

            // 接收响应
            DatagramPacket receivePacket = new DatagramPacket(
                    ackInfo, 50);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);
            log.debug("ACK Info" + PortalUtil.Getbyte2HexString(ackInfo));

            // 解析数据
            if ((int) (ackInfo[14] & 0xFF) == 0) {// 认证成功
                log.debug("建立INFO会话成功，准备发送REQ Challenge!!!");
                return new BaseResult(ackInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 1) {// 认证请求被拒绝
                log.debug("建立INFO会话失败,不支持信息查询功能或者处理失败!!!");
                errorInfo[0] = (byte) 11;
                return new BaseResult("0", "认证请求被拒绝", errorInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 2) {// 消息处理失败
                log.debug("建立INFO会话失败,消息处理失败，由于某种不可知原因，使处理失败，例如询问消息格式错误等!!!");
                errorInfo[0] = (byte) 12;
                return new BaseResult("0", "消息处理失败", errorInfo);
            } else {
                log.debug("建立INFO会话失败,出现未知错误!!!");
                errorInfo[0] = (byte) 13;
                return new BaseResult("0", "出现未知错误", errorInfo);
            }
        } catch (IOException e) {
            log.debug("建立INFO会话，请求无响应!!!");
            errorInfo[0] = (byte) 01;
            return new BaseResult("0", "请求无响应", errorInfo);
        } finally {
            dataSocket.close();
        }
    }

    /**
     * 第8步，Request Challenge
     */
    public BaseResult challenge(String basIp, Integer basPort,
            Integer timeout, byte[] serialNo, byte[] userIp,
            String sharedSecret, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[50];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr = new byte[0];
        byte[] errorInfo = new byte[2];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[16];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 1;      // Type，1：REQ_CHALLENGE
        reqInfo[2] = (byte) 0;      // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;      // Rsvd，保留字段
        reqInfo[4] = serialNo[0];   // SerialNo，序列号
        reqInfo[5] = serialNo[1];   // SerialNo，序列号
        reqInfo[6] = (byte) 0;      // ReqID
        reqInfo[7] = (byte) 0;      // ReqID
        reqInfo[8] = userIp[0];     // UserIp
        reqInfo[9] = userIp[1];     // UserIp
        reqInfo[10] = userIp[2];    // UserIp
        reqInfo[11] = userIp[3];    // UserIp
        reqInfo[12] = (byte) 0;     // UserPort，保留字段
        reqInfo[13] = (byte) 0;     // UserPort，保留字段
        reqInfo[14] = (byte) 0;     // ErrCode
        reqInfo[15] = (byte) 0;     // AttrNum，属性个数

        // 2.0版本协议，增加authenticator字段
        if (portalVersion == 2) {
            // 基础内容，16字节
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }

            // 生成Authenticator
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr, sharedSecret.getBytes());
            // Authenticator存入第16-32位
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }
        }
        log.debug("REQ Challenge" + PortalUtil.Getbyte2HexString(reqInfo));
        log.debug("reqInfo.length " + reqInfo.length);

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(
                    reqInfo, reqInfo.length,
                    InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);

            // 接收响应
            int ackLength = (portalVersion == 1 ? 34 : 50);
            DatagramPacket receivePacket = new DatagramPacket(
                    ackInfo, ackLength);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);
            log.debug("ACK Challenge " + PortalUtil.Getbyte2HexString(ackInfo));

            // 解析数据
            errorInfo[0] = ackInfo[6];
            errorInfo[1] = ackInfo[7];
            if ((int) (ackInfo[14] & 0xFF) == 0) {
                log.debug("请求Challenge成功,准备发送REQ Auth认证请求!!!");
                return new BaseResult(ackInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 1) {
                log.debug("Challenge请求被拒绝!!!");
                return new BaseResult("0", "Challenge请求被拒绝", errorInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 2) {
                log.debug("Challenge连接已建立!!!");
                return new BaseResult("0", "Challenge连接已建立", errorInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 3) {
                log.debug("系统繁忙，请稍后再试!!!");
                return new BaseResult("0", "系统繁忙，请稍后再试", errorInfo);
            } else if ((int) (ackInfo[14] & 0xFF) == 4) {
                log.debug("Challenge请求失败!!!");
                return new BaseResult("0", "Challenge请求失败", errorInfo);
            } else {
                log.debug("Challenge请求发生未知错误!!!");
                return new BaseResult("0", "Challenge请求发生未知错误", errorInfo);
            }
        } catch (IOException e) {
            log.debug("Challenge请求无响应!!!");
            return new BaseResult("0", "Challenge请求无响应", errorInfo);
        } finally {
            dataSocket.close();
        }
    }

    /**
     * 第10步，Request Auth，Chap类型
     */
    public BaseResult chapAuth(String basIp, Integer basPort,
            Integer timeout, String userName, String userPassword,
            byte[] serialNo, byte[] userIp, byte[] reqId,
            byte[] challenge, String sharedSecret, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[100];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr;
        byte[] errorInfo = new byte[1];

        // 生成chapPassword
        byte[] chapPassword = new byte[16];
        byte[] userNameByte = userName.getBytes();
        byte[] passwordByte = userPassword.getBytes();
        chapPassword = PortalUtil.makeChapPassword(reqId, challenge, passwordByte);

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[20 + userNameByte.length
                               + chapPassword.length];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[36 + userNameByte.length
                               + chapPassword.length + 6];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 3;     // Type，3：REQ_AUTH
        reqInfo[2] = (byte) 0;     // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;     // Rsvd，保留字段
        reqInfo[4] = serialNo[0];  // SerialNo，序列号
        reqInfo[5] = serialNo[1];  // SerialNo，序列号
        reqInfo[6] = reqId[0];     // ReqID
        reqInfo[7] = reqId[1];     // ReqID
        reqInfo[8] = userIp[0];    // UserIp
        reqInfo[9] = userIp[1];    // UserIp
        reqInfo[10] = userIp[2];   // UserIp
        reqInfo[11] = userIp[3];   // UserIp
        reqInfo[12] = (byte) 0;    // UserPort，保留字段
        reqInfo[13] = (byte) 0;    // UserPort，保留字段
        reqInfo[14] = (byte) 0;    // ErrCode
        if (portalVersion == 1) {  // AttrNum，属性个数
            reqInfo[15] = (byte) 2;
        } else {
            reqInfo[15] = (byte) 3;
        }

        // 其余字段
        if (portalVersion == 1) {// 版本1.0
            reqInfo[16] = (byte) 1;
            reqInfo[17] = (byte) (userNameByte.length + 2);
            for (int i = 0; i < userNameByte.length; i++) {
                reqInfo[18 + i] = userNameByte[i];
            }
            reqInfo[18 + userNameByte.length] = (byte) 4;
            reqInfo[19 + userNameByte.length] = (byte) (chapPassword.length + 2);
            for (int i = 0; i < chapPassword.length; i++) {
                reqInfo[20 + userNameByte.length + i] = chapPassword[i];
            }
        } else {// 版本2.0
            attr = new byte[4 + userNameByte.length
                            + chapPassword.length + 6];
            // 存userName
            attr[0] = (byte) 1;
            attr[1] = (byte) (userNameByte.length + 2);
            for (int i = 0; i < userNameByte.length; i++) {
                attr[2 + i] = userNameByte[i];
            }

            // 存chapPassword
            attr[2 + userNameByte.length] = (byte) 4;
            attr[3 + userNameByte.length] = (byte) (chapPassword.length + 2);
            for (int i = 0; i < chapPassword.length; i++) {
                attr[4 + userNameByte.length + i] = chapPassword[i];
            }

            // basIp String转byte格式
            byte[] basIpByte = new byte[4];
            String[] ips = basIp.split("[.]");
            for (int i = 0; i < 4; i++) {
                int m = NumberUtils.toInt(ips[i]);
                basIpByte[i] = (byte) m;
            }

            // 存basIp
            attr[4 + userNameByte.length + chapPassword.length] = (byte) 10;
            attr[5 + userNameByte.length + chapPassword.length] = (byte) 6;
            attr[6 + userNameByte.length + chapPassword.length] = basIpByte[0];
            attr[7 + userNameByte.length + chapPassword.length] = basIpByte[1];
            attr[8 + userNameByte.length + chapPassword.length] = basIpByte[2];
            attr[9 + userNameByte.length + chapPassword.length] = basIpByte[3];

            // 生成Authenticator
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }

            // 存attr
            for (int i = 0; i < attr.length; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("REQ Chap Auth" + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(reqInfo,
                    reqInfo.length, InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);

            // 接收响应
            int ackLength = (portalVersion == 1 ? 16 : 100);
            DatagramPacket receivePacket = new DatagramPacket(
                    ackInfo, ackLength);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);

            // 取有效数据（上面读取了100字节，实际没有那么多数据）
            byte[] validAckInfo = new byte[receivePacket.getLength()];
            for (int i = 0; i < validAckInfo.length; i++) {
                validAckInfo[i] = receivePacket.getData()[i];
            }
            log.debug("ACK Chap Auth " + PortalUtil.Getbyte2HexString(validAckInfo));

            // 解析数据
            if ((int) (validAckInfo[14] & 0xFF) == 0
                    || ((int) (validAckInfo[14] & 0xFF) == 2)) {
                log.debug("认证成功,准备发送AFF_ACK_AUTH!!!");
                return new BaseResult();
            } else if ((int) (validAckInfo[14] & 0xFF) == 1) {
                log.debug("认证请求被拒绝!!!");
                errorInfo[0] = (byte) 21;
                return new BaseResult("0", "认证请求被拒绝", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 2) {
                log.debug("认证请求连接已建立!!!");
                errorInfo[0] = (byte) 22;
                return new BaseResult("0", "认证请求连接已建立", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 3) {
                log.debug("系统繁忙,请稍后再试!!!");
                errorInfo[0] = (byte) 23;
                return new BaseResult("0", "系统繁忙,请稍后再试", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 4) {
                log.debug("发送认证请求失败!!!");
                errorInfo[0] = (byte) 24;
                return new BaseResult("0", "发送认证请求失败", errorInfo);
            } else {
                log.debug("发送认证请求出现未知错误!!!");
                errorInfo[0] = (byte) 02;
                return new BaseResult("0", "发送认证请求出现未知错误", errorInfo);
            }
        } catch (IOException e) {
            log.debug("发送认证请求无响应!!!");
            errorInfo[0] = (byte) 02;
            return new BaseResult("0", "发送认证请求无响应", errorInfo);
        } finally {
            dataSocket.close();
        }
    }

    /**
     * 第10步，Request Auth，Pap类型
     */
    public BaseResult papAuth(String basIp, Integer basPort,
            Integer timeout, String userName, String userPassword,
            byte[] serialNo, byte[] userIp, byte[] reqId,
            byte[] challenge, String sharedSecret, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[100];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr;
        byte[] errorInfo = new byte[1];

        byte[] userNameByte = userName.getBytes();
        byte[] userPasswordByte = userPassword.getBytes();

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[20 + userNameByte.length
                               + userPasswordByte.length];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32 + 4 + userNameByte.length
                               + userPasswordByte.length + 6];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 3;     // Type，3：REQ_AUTH
        reqInfo[2] = (byte) 1;     // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;     // Rsvd，保留字段
        reqInfo[4] = serialNo[0];  // SerialNo，序列号
        reqInfo[5] = serialNo[1];  // SerialNo，序列号
        reqInfo[6] = (byte) 0;     // ReqID
        reqInfo[7] = (byte) 0;     // ReqID
        reqInfo[8] = userIp[0];    // UserIp
        reqInfo[9] = userIp[1];    // UserIp
        reqInfo[10] = userIp[2];   // UserIp
        reqInfo[11] = userIp[3];   // UserIp
        reqInfo[12] = (byte) 0;    // UserPort，保留字段
        reqInfo[13] = (byte) 0;    // UserPort，保留字段
        reqInfo[14] = (byte) 0;    // ErrCode
        if (portalVersion == 1) {  // AttrNum，属性个数
            reqInfo[15] = (byte) 2;
        } else {
            reqInfo[15] = (byte) 3;
        }

        // 其余字段
        if (portalVersion == 1) {// 版本1.0
            reqInfo[16] = (byte) 1;
            reqInfo[17] = (byte) (userNameByte.length + 2);
            for (int i = 0; i < userNameByte.length; i++) {
                reqInfo[18 + i] = userNameByte[i];
            }
            reqInfo[18 + userNameByte.length] = (byte) 2;
            reqInfo[19 + userNameByte.length] =
                    (byte) (userPasswordByte.length + 2);
            for (int i = 0; i < userPasswordByte.length; i++) {
                reqInfo[20 + userNameByte.length + i] = userPasswordByte[i];
            }
        } else {// 版本2.0
            attr = new byte[4 + userNameByte.length
                            + userPasswordByte.length + 6];
            // 存userName
            attr[0] = (byte) 1;
            attr[1] = (byte) (userNameByte.length + 2);
            for (int i = 0; i < userNameByte.length; i++) {
                attr[2 + i] = userNameByte[i];
            }

            // 存password
            attr[2 + userNameByte.length] = (byte) 2;// 注意该值是2，chap的值是4
            attr[3 + userNameByte.length] = (byte)
                    (userPasswordByte.length + 2);
            for (int i = 0; i < userPasswordByte.length; i++) {
                attr[4 + userNameByte.length + i] = userPasswordByte[i];
            }

            // basIp String转byte格式
            byte[] basIpByte = new byte[4];
            String[] ips = basIp.split("[.]");
            for (int i = 0; i < 4; i++) {
                int m = NumberUtils.toInt(ips[i]);
                basIpByte[i] = (byte) m;
            }

            // 存basIp
            attr[4 + userNameByte.length + userPasswordByte.length] = (byte) 10;
            attr[5 + userNameByte.length + userPasswordByte.length] = (byte) 6;
            attr[6 + userNameByte.length + userPasswordByte.length] = basIpByte[0];
            attr[7 + userNameByte.length + userPasswordByte.length] = basIpByte[1];
            attr[8 + userNameByte.length + userPasswordByte.length] = basIpByte[2];
            attr[9 + userNameByte.length + userPasswordByte.length] = basIpByte[3];

            // 生成Authenticator
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }

            // 存attr
            for (int i = 0; i < attr.length; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("REQ Pap Auth" + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(reqInfo,
                    reqInfo.length, InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);

            // 接收响应
            DatagramPacket receivePacket = new DatagramPacket(
                    ackInfo, 100);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);

            // 取有效数据（上面读取了100字节，实际没有那么多数据）
            byte[] validAckInfo = new byte[receivePacket.getLength()];
            for (int i = 0; i < validAckInfo.length; i++) {
                validAckInfo[i] = receivePacket.getData()[i];
            }
            log.debug("ACK Pap Auth " + PortalUtil.Getbyte2HexString(validAckInfo));

            // 解析数据
            if ((int) (validAckInfo[14] & 0xFF) == 0
                    || ((int) (validAckInfo[14] & 0xFF) == 2)) {
                log.debug("认证成功,准备发送AFF_ACK_AUTH!!!");
                return new BaseResult();
            } else if ((int) (validAckInfo[14] & 0xFF) == 1) {
                log.debug("认证请求被拒绝!!!");
                errorInfo[0] = (byte) 21;
                return new BaseResult("0", "认证请求被拒绝", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 2) {
                log.debug("认证请求连接已建立!!!");
                errorInfo[0] = (byte) 22;
                return new BaseResult("0", "认证请求连接已建立", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 3) {
                log.debug("系统繁忙,请稍后再试!!!");
                errorInfo[0] = (byte) 23;
                return new BaseResult("0", "系统繁忙,请稍后再试", errorInfo);
            } else if ((int) (validAckInfo[14] & 0xFF) == 4) {
                log.debug("发送认证请求失败!!!");
                errorInfo[0] = (byte) 24;
                return new BaseResult("0", "发送认证请求失败", errorInfo);
            } else {
                log.debug("发送认证请求出现未知错误!!!");
                errorInfo[0] = (byte) 02;
                return new BaseResult("0", "发送认证请求出现未知错误", errorInfo);
            }
        } catch (IOException e) {
            log.debug("发送认证请求无响应!!!");
            errorInfo[0] = (byte) 02;
            return new BaseResult("0", "发送认证请求无响应", errorInfo);
        } finally {
            dataSocket.close();
        }
    }

    /**
     * 第15步，Request AFF_ACK_Auth，Chap类型
     */
    public BaseResult chapAffAckAuth(byte[] serialNo,
            byte[] userIp, byte[] reqId, String basIp,
            int basPort, String sharedSecret,
            Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr = new byte[6];
        byte[] errorInfo = new byte[1];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[16];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32 + 6];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 7;      // Type，7：AFF_ACK_AUTH
        reqInfo[2] = (byte) 0;      // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;      // Rsvd，保留字段
        reqInfo[4] = serialNo[0];   // SerialNo，序列号
        reqInfo[5] = serialNo[1];   // SerialNo，序列号
        reqInfo[6] = reqId[0];      // ReqID
        reqInfo[7] = reqId[1];      // ReqID
        reqInfo[8] = userIp[0];     // UserIp
        reqInfo[9] = userIp[1];     // UserIp
        reqInfo[10] = userIp[2];    // UserIp
        reqInfo[11] = userIp[3];    // UserIp
        reqInfo[12] = (byte) 0;     // UserPort，保留字段
        reqInfo[13] = (byte) 0;     // UserPort，保留字段
        reqInfo[14] = (byte) 0;     // ErrCode
        if (portalVersion == 1) {   // AttrNum，属性个数
            reqInfo[15] = (byte) 0;
        } else {
            reqInfo[15] = (byte) 1;
        }

        // 2.0版本协议
        if (portalVersion == 2) {
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }

            // basIp String转byte格式
            byte[] basIpByte = new byte[4];
            String[] ips = basIp.split("[.]");
            for (int i = 0; i < 4; i++) {
                int m = NumberUtils.toInt(ips[i]);
                basIpByte[i] = (byte) m;
            }

            // Attr
            attr[0] = (byte) 10;
            attr[1] = (byte) 6;
            attr[2] = basIpByte[0];
            attr[3] = basIpByte[1];
            attr[4]= basIpByte[2];
            attr[5] = basIpByte[3];

            // 生成Authenticator
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }

            // 存attr
            for (int i = 0; i < 6; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("Chap AFF_Ack_Auth" + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(
                    reqInfo, reqInfo.length,
                    InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);
            log.debug("发送AFF_Ack_Auth认证成功响应报文回复成功!!!");
        } catch (IOException e) {
            log.debug("发送AFF_Ack_Auth认证成功响应报文回复失败!!!");
        } finally {
            dataSocket.close();
        }
        errorInfo[0] = (byte) 20;
        return new BaseResult(errorInfo);
    }

    /**
     * 第15步，Request AFF_ACK_Auth，Pap类型
     */
    public BaseResult papAffAckAuth(byte[] serialNo,
            byte[] userIp, byte[] reqId, String basIp,
            int basPort, String sharedSecret,
            Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr = new byte[0];
        byte[] errorInfo = new byte[1];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[16];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 7;      // Type，7：AFF_ACK_AUTH
        reqInfo[2] = (byte) 1;      // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;      // Rsvd，保留字段
        reqInfo[4] = serialNo[0];   // SerialNo，序列号
        reqInfo[5] = serialNo[1];   // SerialNo，序列号
        reqInfo[6] = (byte) 0;      // ReqID
        reqInfo[7] = (byte) 0;      // ReqID
        reqInfo[8] = userIp[0];     // UserIp
        reqInfo[9] = userIp[1];     // UserIp
        reqInfo[10] = userIp[2];    // UserIp
        reqInfo[11] = userIp[3];    // UserIp
        reqInfo[12] = (byte) 0;     // UserPort，保留字段
        reqInfo[13] = (byte) 0;     // UserPort，保留字段
        reqInfo[14] = (byte) 0;     // ErrCode
        reqInfo[15] = (byte) 0;     // AttrNum，属性个数

        // 2.0版本协议
        if (portalVersion == 2) {
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }

            // 生成Authenticator
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }
        }
        log.debug("Pap AFF_Ack_Auth" + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(
                    reqInfo, reqInfo.length,
                    InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);
            log.debug("发送AFF_Ack_Auth认证成功响应报文回复成功!!!");
        } catch (IOException e) {
            log.debug("发送AFF_Ack_Auth认证成功响应报文回复失败!!!");
        } finally {
            dataSocket.close();
        }
        errorInfo[0] = (byte) 20;
        return new BaseResult(errorInfo);
    }

    /**
     * 下线
     * @param authType 认证类型，0：Chap， 1：Pap
     */
    public BaseResult logout(String basIp,
            Integer basPort, Integer timeout, byte[] serialNo,
            byte[] userIp, byte[] reqId, String sharedSecret,
            Integer authType, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[100];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr;
        byte[] errorInfo = new byte[1];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[16];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32 + 6];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 5;     // Type，5：REQ_LOGOUT
        reqInfo[2] = (byte) 1;     // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;     // Rsvd，保留字段
        reqInfo[4] = serialNo[0];  // SerialNo，序列号
        reqInfo[5] = serialNo[1];  // SerialNo，序列号
        if (authType == 0) {// Chap
            reqInfo[6] = reqId[0]; // ReqID
            reqInfo[7] = reqId[1]; // ReqID
        } else {// Pap
            reqInfo[6] = (byte) 0; // ReqID
            reqInfo[7] = (byte) 0; // ReqID
        }
        reqInfo[8] = userIp[0];    // UserIp
        reqInfo[9] = userIp[1];    // UserIp
        reqInfo[10] = userIp[2];   // UserIp
        reqInfo[11] = userIp[3];   // UserIp
        reqInfo[12] = (byte) 0;    // UserPort，保留字段
        reqInfo[13] = (byte) 0;    // UserPort，保留字段
        reqInfo[14] = (byte) 0;    // ErrCode
        if (portalVersion == 1) {  // AttrNum，属性个数
            reqInfo[15] = (byte) 0;
        } else {
            reqInfo[15] = (byte) 1;
        }

        // 其余字段
        if (portalVersion == 2) {// 版本2.0
            // basIp String转byte格式
            byte[] basIpByte = new byte[4];
            String[] ips = basIp.split("[.]");
            for (int i = 0; i < 4; i++) {
                int m = NumberUtils.toInt(ips[i]);
                basIpByte[i] = (byte) m;
            }

            attr = new byte[6];
            // 存basIp
            attr[0] = (byte) 10;
            attr[1] = (byte) 6;
            attr[2] = basIpByte[0];
            attr[3] = basIpByte[1];
            attr[4] = basIpByte[2];
            attr[5] = basIpByte[3];

            // 生成Authenticator
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }

            // 存attr
            for (int i = 0; i < attr.length; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("REQ Logout " + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(reqInfo,
                    reqInfo.length, InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);

            // 接收响应
            int ackLength = (portalVersion == 1 ? 16 : 100);
            DatagramPacket receivePacket = new DatagramPacket(
                    ackInfo, ackLength);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);

            // 取有效数据（上面读取了100字节，实际没有那么多数据）
            byte[] validAckInfo = new byte[receivePacket.getLength()];
            for (int i = 0; i < validAckInfo.length; i++) {
                validAckInfo[i] = receivePacket.getData()[i];
            }
            log.debug("ACK Logout " + PortalUtil.Getbyte2HexString(validAckInfo));

            // 解析数据
            if ((int) (validAckInfo[14] & 0xFF) == 1) {
                log.debug("下线请求被拒绝!!!");
                return new BaseResult("0", "下线请求被拒绝", null);
            } else if ((int) (validAckInfo[14] & 0xFF) == 2) {
                log.debug("下线请求出现错误!!!");
                return new BaseResult("0", "下线请求出现错误", null);
            } else {
                log.debug("请求下线成功!!!");
                return new BaseResult("1", "请求下线成功", null);
            }
        } catch (IOException e) {
            log.debug("请求下线无响应!!!");
            return new BaseResult("0", "请求下线无响应", null);
        } finally {
            dataSocket.close();
        }
    }

    /**
     * 超时
     * @param authType 认证类型，0：Chap， 1：Pap
     */
    public BaseResult timeout(String basIp,
            Integer basPort, Integer timeout, byte[] serialNo,
            byte[] userIp, byte[] reqId, String sharedSecret,
            Integer authType, Integer portalVersion) {
        DatagramSocket dataSocket = null;
        byte[] reqInfo;// 请求数据
        byte[] ackInfo = new byte[100];// 响应数据
        byte[] authenBaseBuf = new byte[16];
        byte[] attr;
        byte[] errorInfo = new byte[1];

        // Ver字段：协议版本号
        if (portalVersion == 1) {// 1.0版协议
            reqInfo = new byte[16];
            reqInfo[0] = (byte) 1;
        } else if (portalVersion == 2) {// 2.0版协议
            reqInfo = new byte[32 + 6];
            reqInfo[0] = (byte) 2;
        } else {
            return new BaseResult("0", "不支持该版本", null);
        }

        reqInfo[1] = (byte) 5;     // Type，5：REQ_LOGOUT
        reqInfo[2] = (byte) 1;     // 认证方式，0：Chap，1：Pap（只在Type为3时有效）
        reqInfo[3] = (byte) 0;     // Rsvd，保留字段
        reqInfo[4] = serialNo[0];  // SerialNo，序列号
        reqInfo[5] = serialNo[1];  // SerialNo，序列号
        if (authType == 0) {// Chap
            reqInfo[6] = reqId[0]; // ReqID
            reqInfo[7] = reqId[1]; // ReqID
        } else {// Pap
            reqInfo[6] = (byte) 0; // ReqID
            reqInfo[7] = (byte) 0; // ReqID
        }
        reqInfo[8] = userIp[0];    // UserIp
        reqInfo[9] = userIp[1];    // UserIp
        reqInfo[10] = userIp[2];   // UserIp
        reqInfo[11] = userIp[3];   // UserIp
        reqInfo[12] = (byte) 0;    // UserPort，保留字段
        reqInfo[13] = (byte) 0;    // UserPort，保留字段
        if (portalVersion == 1) {  // AttrNum，属性个数
            reqInfo[14] = (byte) 1;// ErrCode
            reqInfo[15] = (byte) 0;// AttrNum，属性个数
        } else {
            reqInfo[14] = (byte) 1;// ErrCode
            reqInfo[15] = (byte) 1;// AttrNum，属性个数
        }

        // 其余字段
        if (portalVersion == 2) {// 版本2.0
            // basIp String转byte格式
            byte[] basIpByte = new byte[4];
            String[] ips = basIp.split("[.]");
            for (int i = 0; i < 4; i++) {
                int m = NumberUtils.toInt(ips[i]);
                basIpByte[i] = (byte) m;
            }

            attr = new byte[6];
            // 存basIp
            attr[0] = (byte) 10;
            attr[1] = (byte) 6;
            attr[2] = basIpByte[0];
            attr[3] = basIpByte[1];
            attr[4] = basIpByte[2];
            attr[5] = basIpByte[3];

            // 生成Authenticator
            for (int i = 0; i < 16; i++) {
                authenBaseBuf[i] = reqInfo[i];
            }
            byte[] authen = PortalUtil.makeAuthen(authenBaseBuf, attr,
                    sharedSecret.getBytes());

            // 存authen
            for (int i = 0; i < 16; i++) {
                reqInfo[16 + i] = authen[i];
            }

            // 存attr
            for (int i = 0; i < attr.length; i++) {
                reqInfo[32 + i] = attr[i];
            }
        }
        log.debug("REQ Logout " + PortalUtil.Getbyte2HexString(reqInfo));

        try {
            // 发送请求（不需要处理响应）
            dataSocket = new DatagramSocket();
            DatagramPacket requestPacket = new DatagramPacket(reqInfo,
                    reqInfo.length, InetAddress.getByName(basIp), basPort);
            dataSocket.send(requestPacket);
            log.debug("超时回复报文成功:" + PortalUtil.Getbyte2HexString(reqInfo));
            return new BaseResult();
        } catch (IOException e) {
            log.debug("超时回复报文发生未知错误!!!");
            return new BaseResult("0", "超时回复报文发生未知错误", null);
        } finally {
            dataSocket.close();
        }
    }


    //Portal协议登录
    public BaseResult portalLogin(AuthParam authParam) {
        String acIp = authParam.getAcIp();

        // 查询basIp所属ac的配置参数
        BaseResult<Ac> acBaseResult = acService.getInfoByAcIp(acIp);
        Ac ac = acBaseResult.data;
        if (ac == null) {
            return new BaseResult("0", "该AC设备未登记", null);
        }

//        if("cisco".equals(ac.getBrand().getCode())){
//            acIp = ac.getNasIp();
//        }
        if("1".equals(isCloud)){
            acIp = ac.getNasIp();
        }

        Integer basPort = ac.getPort();// ac端口，固定2000
        Integer timeout = ac.getExpireTime();
        Integer authType = ac.getAuthType();// 认证类型，0：Chap， 1：Pap
        Integer portalVersion = Integer.valueOf(ac.getPortalVersion());// 协议版本，1或2
        String sharedSecret = ac.getShareKey();
        log.debug("portalLogin param"
                + " userName " + authParam.getUserName()
                + " password " + authParam.getPassword()
                + " acIp " + acIp
                + " basPort " + basPort
                + " timeout " + timeout + " authType " + authType
                + " portalVersion " + portalVersion + " sharedSecret " + sharedSecret);

        // userIp String转byte格式
        byte[] userIpByte = new byte[4];
        String[] ips = authParam.getUserIp().split("[.]");
        for (int i = 0; i < 4; i++) {
            int m = NumberUtils.toInt(ips[i]);
            userIpByte[i] = (byte) m;
        }
        // serialNo，报文序列号
        short serialShort = (short) (1 + Math.random() * 32767);
        byte[] serialNo = PortalUtil.SerialNo(serialShort);

        // 第6步，Request UserInfo，可选，有的Radius设备没有这个接口

        // 第8步，Request Challenge
        byte[] reqId = new byte[2];
        byte[] challenge = new byte[16];
        if (authType == 0) {// Chap需要Request Challenge，Pap不需要
            BaseResult challengeResult = challenge(acIp,
                    basPort, timeout, serialNo, userIpByte,
                    sharedSecret, portalVersion);
            if (challengeResult.getReturnCode().equals("0")) {// 失败
                reqId = (byte[]) challengeResult.getData();
                // 发送超时报文
                timeout(acIp, basPort, timeout, serialNo,
                        userIpByte, reqId, sharedSecret, authType,
                        portalVersion);
                return challengeResult;
            }
            byte[] challengeData = (byte[]) challengeResult.getData();
            reqId[0] = challengeData[6];
            reqId[1] = challengeData[7];
            for (int i = 0; i < 16; i++) {
                if (portalVersion == 1) {
                    challenge[i] = challengeData[18 + i];
                } else {
                    challenge[i] = challengeData[34 + i];
                }
            }
            log.info("获得Challenge " + PortalUtil.Getbyte2HexString(challenge));
        }

        // 第10步，Request Auth
        BaseResult authResult = null;
        if (authType == 0) {// Chap
            authResult = chapAuth(acIp,
                    basPort, timeout, authParam.getUserName(),
                    authParam.getPassword(),
                    serialNo, userIpByte, reqId,
                    challenge, sharedSecret, portalVersion);
            if (authResult.getReturnCode().equals("0")) {// 失败
                // 发送超时报文
                timeout(acIp, basPort, timeout, serialNo,
                        userIpByte, reqId, sharedSecret, authType,
                        portalVersion);
                return authResult;
            }
        } else {// Pap
            authResult = papAuth(acIp,
                    basPort, timeout, authParam.getUserName(),
                    authParam.getPassword(),
                    serialNo, userIpByte, reqId,
                    challenge, sharedSecret, portalVersion);
            if (authResult.getReturnCode().equals("0")) {// 失败
                // 发送超时报文
                timeout(acIp, basPort, timeout, serialNo,
                        userIpByte, reqId, sharedSecret, authType,
                        portalVersion);
                return authResult;
            }
        }

        // 第15步，Request AFF_ACK_Auth，该接口不论成功失败，都不影响认证结果
        if (authType == 0) {// Chap
            chapAffAckAuth(serialNo, userIpByte,
                    reqId, acIp, basPort, sharedSecret,
                    portalVersion);
        } else {
            papAffAckAuth(serialNo, userIpByte,
                    reqId, acIp, basPort, sharedSecret,
                    portalVersion);
        }

        return new BaseResult();
    }

}
