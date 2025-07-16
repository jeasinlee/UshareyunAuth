package cn.ushare.account.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validators {

    /**
     * @param validatee
     * @return boolean
     */
    public static boolean isNull(Object validatee) {
        if (null == validatee) {
            return true;
        }
        return false;
    }

    /**
     * Method isNotnull
     *
     * @param validatee
     * @return
     */
    public static boolean isNotnull(Object validatee) {
        return !Validators.isNull(validatee);
    }

    /**
     * Method isNull
     *
     * @param validatee
     * @return
     */
    public static boolean isNull(String validatee) {
        if (null == validatee || "".equals(validatee.trim())||"null".equals(validatee)) {
            return true;
        }
        return false;
    }

    /**
     * Method isNotnull
     *
     * @param validatee
     * @return
     */
    public static boolean isNotnull(String validatee) {
        return !Validators.isNull(validatee);
    }

    public static boolean isListNotNull(String validatee) {
        if(!Validators.isNull(validatee)){
            if("[]".equals(validatee)){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * @param tester
     * @return boolean
     */
    public static boolean isNumeric(String validatee) {
        boolean isNumeric = false;
        if (!Validators.isNull(validatee)
                && Pattern.matches("^(-)?\\d+(\\.\\d+)?", validatee)) {
            isNumeric = true;
        }
        return isNumeric;
    }

    /**
     * @param validatee
     * @return boolean
     */
    public static boolean isInteger(String validatee) {
        boolean isInteger = false;
        if (Pattern.matches("^(-)?\\d+$", validatee)) {
            isInteger = true;
        }
        return isInteger;
    }

    /**
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {

        boolean isEmail = false;
        String regex1 = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
        String regex2 = "^([a-zA-Z0-9_\\.\\-])+\\@(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})+$";
        String regex3 = "^([a-zA-Z0-9_\\.\\-])+\\@([a-zA-Z0-9\\-\\_])+$";
        if (Pattern.matches(regex1, email) || Pattern.matches(regex2, email)
                || Pattern.matches(regex3, email)) {
            isEmail = true;
        }
        return isEmail;
    }

    public static boolean isYear(String year) {
        if (isNotnull(year) && isInteger(year)) {
            long value = Long.valueOf(year);
            if (value >= 1970 && value <= 2100) {
                return true;
            }
        }
        return false;
    }

    /**
     * IPV4
     *
     * @param ipstr
     * @return
     */
    public static boolean isIPAddress(String ipstr) {
        ipstr = ipstr.trim();
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

        if (!ipstr.matches(regex)) {
            return false;
        }
        String[] ips = ipstr.split("\\.");
        for (int i = 0; i < 4; i++) {
            int num = Integer.parseInt(ips[i]);
            if (num < 0 || num > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断电话
     * @param telNum
     * @return
     */
    public static boolean isMobiPhoneNum(String telNum) {
        if(Validators.isNull(telNum)){
            return false;
        }
        String regex = "^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(telNum);
        return m.matches();
    }
    
    public static boolean isPhoneNum(String telNum) {
        if(Validators.isNull(telNum)){
            return false;
        }
        String regexPhone = "^0\\d{2,3}-?\\d{7,8}$";
        Pattern p = Pattern.compile(regexPhone, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(telNum);
        return m.matches();
    }
    
    /**
     * 验证该url是否是合格的
     * @param url
     * @return
     */
    public static boolean isURL(String url) {
        String regex0 = "^\\s*((http|ftp|https)://){0,1}(([A-Za-z0-9-_\\u4E00-\\u9FA5]+(\\.([A-Za-z0-9-_\\u4E00-\\u9FA5]+)){0,}(\\.[a-zA-Z]{2,9}){1,})|(([0-9]{1,3}\\.){3}[0-9]{1,3}))(:[0-9]{1,5}){0,1}(\\/.*)?\\s*$";

        if (url.matches(regex0)) {
            return true;
        }
        return false;
    }
    
    public static boolean isURL2(String url) {
        String regex0 = "^\\s*((http|https)://)(([A-Za-z0-9-_\\u4E00-\\u9FA5]+(\\.([A-Za-z0-9-_\\u4E00-\\u9FA5]+)){0,}(\\.[a-zA-Z]{2,9}){1,})|(([0-9]{1,3}\\.){3}[0-9]{1,3}))(:[0-9]{1,5}){0,1}(\\/.*)?\\s*$";
        
        if (url.matches(regex0)) {
            return true;
        }
        return false;
    }

    /**
     * 校验ip是否是正确的ip
     * @param ip
     * @return
     */
    public static boolean isIP(String ip) {

        if(!isNotnull(ip)) return false;

        if(isIPv4(ip) || isIPv6(ip) || isIP4in6(ip)) {
            return true;
        }

        return false;
    }

    /**
     * IPV4 格式
     * @param ipv4
     * @return
     */
    public static boolean isIPv4(String ipv4) {

        if(!isNotnull(ipv4)) return false;
        ipv4 = ipv4.trim();
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

        if(!ipv4.matches(regex)){
            return false;
        }

        String[] ips = ipv4.split("\\.");

        for(int i=0 ; i<4; i++){
            int num = Integer.parseInt(ips[i]);
            if(num<0 || num>255){
                return false;
            }
        }

        return true;
    }

    /**
     * IPV6格式
     * @param ipv6
     * @return
     */
    public static boolean isIPv6(String ipv6) {
        if(!isNotnull(ipv6)) return false;
        ipv6 = ipv6.trim();
        boolean result = false;
        String regHex = "(\\p{XDigit}{1,4})";
        String regIPv6Full = "^(" + regHex + ":){7}" + regHex + "$";
        String regIPv6AbWithColon = "^(" + regHex + "(:|::)){0,6}" + regHex
                + "$";
        String regIPv6AbStartWithDoubleColon = "^(" + "::(" + regHex
                + ":){0,5}" + regHex + ")$";
        String regIPv6 = "^(" + regIPv6Full + ")|("
                + regIPv6AbStartWithDoubleColon + ")|(" + regIPv6AbWithColon
                + ")$";

        if (ipv6.indexOf(":") != -1) {

            if (ipv6.length() <= 39) {
                String addressTemp = ipv6;
                int doubleColon = 0;

                while (addressTemp.indexOf("::") != -1) {
                    addressTemp = addressTemp.substring(addressTemp
                            .indexOf("::") + 2, addressTemp.length());
                    doubleColon++;
                }

                if (doubleColon <= 1) {
                    result = ipv6.matches(regIPv6);
                }
            }
        }

        return result;
    }

    /**
     * IPV4 IN IPV6
     * @param ip
     * @return
     */
    public static boolean isIP4in6(String ip) {
        if(!isNotnull(ip)) return false;
        ip = ip.trim();
        String reg = "：^([\\da-fA-F]{1,4}:){6}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^::([\\da-fA-F]{1,4}:){0,4}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^([\\da-fA-F]{1,4}:):([\\da-fA-F]{1,4}:){0,3}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^([\\da-fA-F]{1,4}:){2}:([\\da-fA-F]{1,4}:){0,2}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^([\\da-fA-F]{1,4}:){3}:([\\da-fA-F]{1,4}:){0,1}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^([\\da-fA-F]{1,4}:){4}:((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ip.matches(reg);
    }

    /**
     * 检查 参数 是否是数值
     * @param portNo
     * @return
     */
    public static boolean isNumeric(Integer portNo) {
        boolean isNumeric=false;
        if(portNo==null || portNo.equals("")) {
            isNumeric=false;
            return isNumeric;
        }
        if(Pattern.matches("^(-)?\\d+$", portNo.toString())) {
            isNumeric = true;
        }
        return isNumeric;
    }
    
    
     /** <比较两个日期大小>
        * <判断开始时间是否小于等于结束时间>
        * @param startDate     开始时间
        * @param endDate       结束时间
        * @return              true:是;false:否
        */
        public static boolean dataCommpare(String startDate, String endDate){
            if(startDate.equals("")||endDate.equals("")){
               return false;
            }
            String[] sdatetime = startDate.split("\\ ");
            String[] edatetime = endDate.split("\\ ");
            String[] sdate = sdatetime[0].split("\\-");
            String[] edate = edatetime[0].split("\\-");

            if(Integer.parseInt(sdate[0])>Integer.parseInt(edate[0])){
                return false;
            }else if(Integer.parseInt(sdate[0])==Integer.parseInt(edate[0])){
                if(Integer.parseInt(sdate[1])>Integer.parseInt(edate[1])){
                    return false;
                }else if(Integer.parseInt(sdate[1])==Integer.parseInt(edate[1])){
                    if(Integer.parseInt(sdate[2])>Integer.parseInt(edate[2])){
                        return false;
                    }else if(Integer.parseInt(sdate[2])==Integer.parseInt(edate[2])){
                        String stime = sdatetime[1].replaceAll(":","");
                        String etime = edatetime[1].replaceAll(":","");
                        if(Integer.parseInt(stime)>Integer.parseInt(etime)){
                            return false;
                        }
                    }
                }
            }
            return true;
        }

    /** <判断字符串是否为正浮点数>
     * <功能详细描述>
     * @param tester  待判断字符串
     * @return        true：是正浮点数 false：不是正浮点数
     */
    public static boolean isFlodric(String tester) {
        boolean isNumeric=false;
        if(tester==null || tester.equals("")) {
            isNumeric=false;
            return isNumeric;
        }
        if(Pattern.matches("^\\d+(\\.\\d+)?$", tester)) {
            isNumeric = true;
        }
        return isNumeric;
    }
}
