package cn.ushare.account.util;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 用于发送syslog
 * @author wuws
 *
 */
public class SyslogUtil {

    /**
     * 使用udp协议
     * @param ip
     * @param port
     * @param msg
     */
    public static void sendSyslog(String ip, int port, String msg){
        //获取syslog的操作类，使用udp协议。syslog支持"udp", "tcp", "unix_syslog", "unix_socket"协议
        SyslogIF syslog = Syslog.getInstance("udp");
        //设置syslog服务器端地址
        syslog.getConfig().setHost(ip);
        //设置syslog接收端口
        syslog.getConfig().setPort(Integer.valueOf(port));
        try {
            syslog.log(0, URLDecoder.decode(msg,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void sendSyslog(String protocol, String ip, int port, String msg){
        //获取syslog的操作类，使用udp协议。syslog支持"udp", "tcp", "unix_syslog", "unix_socket"协议
        SyslogIF syslog = Syslog.getInstance(protocol);
        //设置syslog服务器端地址
        syslog.getConfig().setHost(ip);
        //设置syslog接收端口
        syslog.getConfig().setPort(Integer.valueOf(port));
        try {
            syslog.log(0, URLDecoder.decode(msg,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
