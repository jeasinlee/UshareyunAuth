package cn.ushare.account.util;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;

/**
 * 判断浏览器类型
 */
@Slf4j
public class BrowseTypeUtil {

    // 定义移动端请求的所有可能类型
    private final static String[] agent = { "Android", "iPhone", "iPod",
            "iPad", "Windows Phone", "MQQBrowser" }; 

    /**
     * 判断终端类型，返回值：1pc，2android，3ios
     */
    public static Integer getTerminalType(String ua) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ua);
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        DeviceType deviceType = operatingSystem.getDeviceType();
        log.info("====operatingSystem:" + operatingSystem.getName());
        log.info("===deviceType", deviceType.getName());
        if(deviceType.getName().equals("Computer")){
            return 1;
        }else if(deviceType.getName().equals("Mobile")){
            if(operatingSystem.getName().contains("iPhone") || operatingSystem.getName().contains("iPad")){
                return 3;
            } else if(operatingSystem.getName().contains("Android")){
                return 2;
            } else {
                return 1;
            }
        }
        return 1;
    }

    public static String getTerminalTypeStr(String ua) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ua);
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        DeviceType deviceType = operatingSystem.getDeviceType();
        log.info("====operatingSystem:" + operatingSystem.getName());
        log.info("===deviceType", deviceType.getName());
        if(deviceType.getName().equals("Computer")){
            return "PC";
        }else if(deviceType.getName().equals("Mobile")){
            if(operatingSystem.getName().contains("iPhone") || operatingSystem.getName().contains("iPad")){
                return "iOS";
            } else if(operatingSystem.getName().contains("Android")){
                return "Android";
            } else {
                return "PC";
            }
        }
        return "PC";
    }
}
