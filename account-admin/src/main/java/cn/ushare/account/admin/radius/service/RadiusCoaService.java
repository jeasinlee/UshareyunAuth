package cn.ushare.account.admin.radius.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cn.ushare.account.admin.portal.service.PortalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.DateUtil;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 强制下线，COA，Change-Of-Authorization
 */
@Service
@Transactional
@Slf4j
public class RadiusCoaService {

    private static final int port = 3799;

    public BaseResult requestCoa(Integer timeout, String acIp, String userIp,
            String userName, String sharedSecret, String acctSessionId) {
        log.debug("requestCoa params timeout " + timeout + " acIp " + acIp
                + " userIp " + userIp + " userName " + userName
                + " sharedSecret " + sharedSecret
                + " acctSessionId " + acctSessionId);
        String typeName = "Disconnect-Request";
        int identifier = (int) (255 * (Math.random()));
        if (identifier < 1) {
            identifier = 1;
        }
        if (identifier > 255) {
            identifier = 255;
        }
        String authenticator = "00000000000000000000000000000000";
        String attributes = "";
        if (StringUtil.isNotBlank(acctSessionId)) {
            attributes = RadiusUtil.getAttributeString(44, acctSessionId);
        }
        if (StringUtil.isNotBlank(userName)) {
            attributes = attributes + RadiusUtil.getAttributeString(1, userName);
        }
        if (StringUtil.isNotBlank(userIp)) {
            attributes = attributes + RadiusUtil.getAttributeIP(userIp);
        }
        if (StringUtil.isNotBlank(acIp)) {
            attributes = attributes + RadiusUtil.getAttributeNasIP(acIp);
        }
        String sharedSecretHex = RadiusUtil.ByteToHex(sharedSecret.getBytes());
        byte[] reqInfo = RadiusUtil.getOutData(typeName, sharedSecretHex,
                acIp, port, 40, identifier, authenticator, attributes);

        DatagramSocket dataSocket = null;
        DatagramPacket requestPacket = null;
        try {
            dataSocket = new DatagramSocket();
            requestPacket = new DatagramPacket(reqInfo, reqInfo.length,
                    InetAddress.getByName(acIp), port);
            dataSocket.send(requestPacket);

            // 接收响应
            byte[] ackInfo = new byte[1024];// 接收服务器的数据包
            DatagramPacket receivePacket = new DatagramPacket(ackInfo, 1024);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);
            log.debug("ACK Info" + PortalUtil.Getbyte2HexString(ackInfo));

            ackInfo = new byte[receivePacket.getLength()];
            for (int i = 0; i < ackInfo.length; i++) {
                ackInfo[i] = receivePacket.getData()[i];
            }

            int code = ackInfo[0] & 0xFF;
            log.info("CoA Offline code:" + code);
            if (code == 41) {
                log.info("CoA Offline Success !!");
                return new BaseResult();
            } else {
                log.info("CoA Offline Fail !!");
                return new BaseResult("0", "Coa下线失败", null);
            }
        } catch (IOException e) {
            log.error("CoA Error Exception=", e);
            return new BaseResult("-2", e.getMessage(), null);
        } finally {
            dataSocket.close();
        }
    }

    public BaseResult requestCoaForAruba(Integer timeout, String acIp, Integer port, String userIp,
                                 String userName, String sharedSecret, String callingStationId) {
        log.debug("requestCoa params timeout " + timeout + " acIp " + acIp
                + " userIp " + userIp + " userName " + userName
                + " sharedSecret " + sharedSecret
                + " callingStationId " + callingStationId);
        String typeName = "Disconnect-Request";
        int identifier = (int) (255 * (Math.random()));
        if (identifier < 1) {
            identifier = 1;
        }
        if (identifier > 255) {
            identifier = 255;
        }
        String authenticator = "00000000000000000000000000000000";
        String attributes = "";
        if (StringUtil.isNotBlank(callingStationId)) {
            attributes = RadiusUtil.getAttributeString(31, callingStationId);
        }
        if (StringUtil.isNotBlank(userName)) {
            attributes = attributes + RadiusUtil.getAttributeString(1, userName);
        }
        if (StringUtil.isNotBlank(userIp)) {
            attributes = attributes + RadiusUtil.getAttributeIP(userIp);
        }
        if (StringUtil.isNotBlank(acIp)) {
            attributes = attributes + RadiusUtil.getAttributeNasIP(acIp);
        }
        String sharedSecretHex = RadiusUtil.ByteToHex(sharedSecret.getBytes());
        byte[] reqInfo = RadiusUtil.getOutData(typeName, sharedSecretHex,
                acIp, port, 40, identifier, authenticator, attributes);

        DatagramSocket dataSocket = null;
        DatagramPacket requestPacket = null;
        try {
            dataSocket = new DatagramSocket();
            requestPacket = new DatagramPacket(reqInfo, reqInfo.length,
                    InetAddress.getByName(acIp), port);
            dataSocket.send(requestPacket);

            // 接收响应
            byte[] ackInfo = new byte[1024];// 接收服务器的数据包
            DatagramPacket receivePacket = new DatagramPacket(ackInfo, 1024);
            dataSocket.setSoTimeout(timeout * 1000);// 超时时间
            dataSocket.receive(receivePacket);
            log.debug("ACK Info" + PortalUtil.Getbyte2HexString(ackInfo));

            ackInfo = new byte[receivePacket.getLength()];
            for (int i = 0; i < ackInfo.length; i++) {
                ackInfo[i] = receivePacket.getData()[i];
            }
            int code = ackInfo[0] & 0xFF;
            log.info("CoA Offline code:" + code);
            if (code == 41) {
                log.info("CoA Offline Success !!");
                return new BaseResult();
            } else {
                log.info("CoA Offline Fail !!");
                return new BaseResult("0", "Coa下线失败", null);
            }
        } catch (IOException e) {
            log.error("CoA Error Exception=", e);
            return new BaseResult("-2", e.getMessage(), null);
        } finally {
            dataSocket.close();
        }
    }
}
