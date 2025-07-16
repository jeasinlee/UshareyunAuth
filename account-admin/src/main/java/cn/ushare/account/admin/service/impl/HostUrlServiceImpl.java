package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.HostUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class HostUrlServiceImpl implements HostUrlService {

    @Value("${server.port}")
    String serverPort;// 后台服务端口
    @Value("${frontPort}")
    String frontPort;// 前端服务端口
    @Value("${frontPrefix}")
    String frontPrefix;// 前端服务地址前缀


    String hostIp;// 本机地址
    String frontUrl;// 前端访问地址
    String serverUrl;// 服务器访问地址

    @Override
    public String getFrontUrl(HttpServletRequest request) {
        /*****local test start*****/
//        try {
//            InetAddress inetAddress = getLocalHostLANAddress();
//            hostIp = inetAddress.getHostAddress();
//        } catch (UnknownHostException e) {
//            log.error("Error Exception=", e);
//        } catch (Exception e) {
//            log.error("Error Exception=", e);
//        }
//        frontUrl = "http://" + hostIp + ":" + frontPort + frontPrefix;
        /*****local test end*****/
        frontUrl = request.getScheme() + "://" + request.getServerName() + ":" + frontPort + frontPrefix;
        return frontUrl;
    }

    @Override
    public String getServerUrl(HttpServletRequest request) {
        /*****local test start*****/
//        try {
//            InetAddress inetAddress = getLocalHostLANAddress();
//            hostIp = inetAddress.getHostAddress();
//            //hostIp = inetAddress.getLocalHost().getHostAddress().toString();
//            //hostIp = "172.16.11.251";
//            //hostIp = "172.16.200.250";
//        } catch (UnknownHostException e) {
//            log.error("Error Exception=", e);
//        } catch (Exception e) {
//            log.error("Error Exception=", e);
//        }
//        serverUrl = "http://" + hostIp + ":" + serverPort;
        /*****local test end*****/
        serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + serverPort;
        return serverUrl;
    }

    /**
     * 要关闭虚拟网卡，否则会读到虚拟网卡地址
     */
    public InetAddress getLocalHostLANAddress() throws Exception {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    //log.debug("inetAddr " + inetAddr);
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
        return null;
    }
}
