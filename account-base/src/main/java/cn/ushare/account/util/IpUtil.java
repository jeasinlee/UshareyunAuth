package cn.ushare.account.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ouyangjunfeng on 2017/6/21.
 */
public class IpUtil {


	// 获取客户端IP
	public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
	}

    public static int[] ipAddressSplit(String ipAddress) {
        String[] ipSplit = ipAddress.split("\\.");
        int[] ip = new int[ipSplit.length];
        if (ipSplit.length == 4) {
            for (int i = 0; i < ipSplit.length; i++) {
                ip[i] = Integer.parseInt(ipSplit[i]);
            }
        }
        return ip;
    }

    /**
     * 根据IP和掩码获取网段
     *
     * @param gateway
     * @param netmask
     * @return
     */
    public static String getNetworkAddress(String gateway, String netmask) {
        int[] ipInt = new int[4];
        int[] netmaskInt = new int[4];
        int[] temp = new int[4];
        String ip = null;
        ipInt = ipAddressSplit(gateway);
        netmaskInt = ipAddressSplit(netmask);
        for (int i = 0; i < 4; i++) {
            temp[i] = ipInt[i] & netmaskInt[i];
        }
        ip = String.valueOf(temp[0]) + "." + String.valueOf(temp[1]) + "."
                + String.valueOf(temp[2]) + "." + String.valueOf(temp[3]);
        return ip;
    }

    /**
     * 根据掩码获取掩码长度
     *
     * @param ipNetmask
     * @return
     */
    public static int toNetworkFlag(String ipNetmask) {
        int flag = 0;
        String[] ipSplit = ipNetmask.split("\\.");
        if (ipSplit.length == 4) {
            for (int i = 0; i < ipSplit.length; i++) {
                int temp = Integer.parseInt(ipSplit[i]);
                if (temp == 255) {
                    flag += 8;
                } else {
                    if (temp == 254) {
                        flag += 7;
                    } else if (temp == 252) {
                        flag += 6;
                    } else if (temp == 248) {
                        flag += 5;
                    } else if (temp == 240) {
                        flag += 4;
                    } else if (temp == 224) {
                        flag += 3;
                    } else if (temp == 192) {
                        flag += 2;
                    } else if (temp == 128) {
                        flag += 1;
                    }
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 根据IP和IP地址段，判断该IP是否存在改网段中
     * 192.168.1.0/24类型
     *
     * @param ip
     * @param cidr
     * @return
     */
    public static boolean isInRange(String ip, String cidr) {
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24)
                | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = cidr.replaceAll("/.*", "");
        String[] cidrIps = cidrIp.split("\\.");
        int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8)
                | Integer.parseInt(cidrIps[3]);

        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    /**
     * 根据IP和IP地址段，判断该IP是否存在改网段中
     * 192.168.1.1-192.168.1.100类型
     *
     * @param ipSection
     * @param ip
     * @return
     */
    public static boolean ipIsValid(String ipSection, String ip) {
        if (ipSection == null) {
            throw new NullPointerException("IP段不能为空！");
        }
        if (ip == null) {
            throw new NullPointerException("IP不能为空！");
        }

        ipSection = ipSection.trim();
        ip = ip.trim();

        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        final String REGX_IPB = REGX_IP + "\\-" + REGX_IP;
        if (!ipSection.matches(REGX_IPB) || !ip.matches(REGX_IP))
            return false;
        int idx = ipSection.indexOf('-');
        String[] sips = ipSection.substring(0, idx).split("\\.");
        String[] sipe = ipSection.substring(idx + 1).split("\\.");
        String[] sipt = ip.split("\\.");
        long ips = 0L, ipe = 0L, ipt = 0L;
        for (int i = 0; i < 4; ++i) {
            ips = ips << 8 | Integer.parseInt(sips[i]);
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);
        }
        if (ips > ipe) {
            long t = ips;
            ips = ipe;
            ipe = t;
        }
        return ips <= ipt && ipt <= ipe;
    }


    /**
     * 用正则表达式判断是否为IP
     */
    public static boolean isIP(String addr) {
        String regex = "^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)$";
        Pattern pattern = Pattern.compile(regex);
        String[] addrs = addr.split(",");
        boolean checkout = true;
        for (int i = 0; i < addrs.length; i++) {
            Matcher matcher = pattern.matcher(addrs[i]);
            if (matcher.find()) {
                int a = Integer.parseInt(matcher.group(1));
                int b = Integer.parseInt(matcher.group(2));
                int c = Integer.parseInt(matcher.group(3));
                int d = Integer.parseInt(matcher.group(4));

                if (!(isValidRange(a) && isValidRange(b) && isValidRange(c) && isValidRange(d))) {
                    checkout = false;
                }

            } else {
                checkout = false;
            }
        }
        return checkout;
    }

    private static boolean isValidRange(int range) {
        return (range >= 0 && range <= 255);
    }

    /**
     * ip转数字
     * @param ipString
     * @return
     */
    public static long ipToLong(String ipString) {
        long result = 0;
        java.util.StringTokenizer token = new java.util.StringTokenizer(
                ipString, ".");
        result += Long.parseLong(token.nextToken()) << 24;
        result += Long.parseLong(token.nextToken()) << 16;
        result += Long.parseLong(token.nextToken()) << 8;
        result += Long.parseLong(token.nextToken());
        return result;
    }


    public static String getBeginIpStr(String ip, String maskBit){
        return getIpFromLong(getBeginIpLong(ip, maskBit));
    }

    public static String getEndIpStr(String ip, String maskBit){
        return getIpFromLong(getEndIpLong(ip, maskBit));
    }

    public static Long getEndIpLong(String ip, String maskBit){
        return getBeginIpLong(ip, maskBit)
                + ~ ipToLong(getMaskByMaskBit(maskBit));
    }

    public static String getIpFromLong(Long ip){
        String s1 = String.valueOf((ip & 4278190080L) / 16777216L);
        String s2 = String.valueOf((ip & 16711680L) / 65536L);
        String s3 = String.valueOf((ip & 65280L) / 256L);
        String s4 = String.valueOf(ip & 255L);
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    public static Long getBeginIpLong(String ip, String maskBit){
        return ipToLong(ip) & ipToLong(getMaskByMaskBit(maskBit));
    }

    public static String getMaskByMaskBit(String maskBit){
        return StringUtils.isEmpty(maskBit) ? "error, maskBit is null !"
                : maskBitMap().get(maskBit);
    }

    /*
     * 存储着所有的掩码位及对应的掩码 key:掩码位 value:掩码（x.x.x.x）
     */
    private static Map<String, String> maskBitMap(){
        Map<String, String> maskBit = new HashMap<String, String>();
        maskBit.put("1", "128.0.0.0");
        maskBit.put("2", "192.0.0.0");
        maskBit.put("3", "224.0.0.0");
        maskBit.put("4", "240.0.0.0");
        maskBit.put("5", "248.0.0.0");
        maskBit.put("6", "252.0.0.0");
        maskBit.put("7", "254.0.0.0");
        maskBit.put("8", "255.0.0.0");
        maskBit.put("9", "255.128.0.0");
        maskBit.put("10", "255.192.0.0");
        maskBit.put("11", "255.224.0.0");
        maskBit.put("12", "255.240.0.0");
        maskBit.put("13", "255.248.0.0");
        maskBit.put("14", "255.252.0.0");
        maskBit.put("15", "255.254.0.0");
        maskBit.put("16", "255.255.0.0");
        maskBit.put("17", "255.255.128.0");
        maskBit.put("18", "255.255.192.0");
        maskBit.put("19", "255.255.224.0");
        maskBit.put("20", "255.255.240.0");
        maskBit.put("21", "255.255.248.0");
        maskBit.put("22", "255.255.252.0");
        maskBit.put("23", "255.255.254.0");
        maskBit.put("24", "255.255.255.0");
        maskBit.put("25", "255.255.255.128");
        maskBit.put("26", "255.255.255.192");
        maskBit.put("27", "255.255.255.224");
        maskBit.put("28", "255.255.255.240");
        maskBit.put("29", "255.255.255.248");
        maskBit.put("30", "255.255.255.252");
        maskBit.put("31", "255.255.255.254");
        maskBit.put("32", "255.255.255.255");
        return maskBit;
    }

    public static String getIpFromUrl(String url){
        return URI.create(url).getHost();
    }

    public static String getRemoteAddrIp(HttpServletRequest request) {
        String ipFromNginx = getHeader(request, "X-Real-IP");
//	    System.out.println("ipFromNginx:" + ipFromNginx);
//	    System.out.println("getRemoteAddr:" + request.getRemoteAddr());
        return StringUtils.isBlank(ipFromNginx) ? request.getRemoteAddr() : ipFromNginx;
    }


    private static String getHeader(HttpServletRequest request, String headName) {
        String value = request.getHeader(headName);
        return !StringUtils.isBlank(value) && !"unknown".equalsIgnoreCase(value) ? value : "";
    }

    public static void main(String[] args) {
        System.out.println(getIpFromUrl("https://1.1.1.1/login.html"));
    }
}
