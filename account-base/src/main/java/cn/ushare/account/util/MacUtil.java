package cn.ushare.account.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class MacUtil {

    /**
     * mac地址字符串加冒号，全部小写，比如14a51a330bbc转成14:a5:1a:33:0b:bc
     */
    public static String macFormat(String mac) {
        if(StringUtils.isBlank(mac)){
            return null;
        }
        if(mac.indexOf(":")>-1){
            return mac;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mac.length(); i++) {
            String str = mac.substring(i, i + 1);
            str = str.toLowerCase();
            sb.append(str);
            if (i % 2 == 1 && i != (mac.length() - 1)) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    public static boolean isMacAddress(String macStr){
        String patternMac = "(([A-Fa-f0-9]{2}:?)|([A-Fa-f0-9]{2}-?)){5}[A-Fa-f0-9]{2}";
        if(!Pattern.compile(patternMac).matcher(macStr).find()){
            return false;
        }
        return true;
    }

    public static boolean isIDNumber(String IDNumber) {
        if (IDNumber == null || "".equals(IDNumber)) {
            return false;
        }
        // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
        String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";

        boolean matches = IDNumber.matches(regularExpression);

        //判断第18位校验值
        if (matches) {
            if (IDNumber.length() == 18) {
                try {
                    char[] charArray = IDNumber.toCharArray();
                    //前十七位加权因子
                    int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                    //这是除以11后，可能产生的11位余数对应的验证码
                    String[] idCardY = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
                    int sum = 0;
                    for (int i = 0; i < idCardWi.length; i++) {
                        int current = Integer.parseInt(String.valueOf(charArray[i]));
                        int count = current * idCardWi[i];
                        sum += count;
                    }
                    char idCardLast = charArray[17];
                    int idCardMod = sum % 11;
                    if (idCardY[idCardMod].toUpperCase().equals(String.valueOf(idCardLast).toUpperCase())) {
                        return true;
                    } else {
                        System.out.println("身份证最后一位:" + String.valueOf(idCardLast).toUpperCase() +
                                "错误,正确的应该是:" + idCardY[idCardMod].toUpperCase());
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常:" + IDNumber);
                    return false;
                }
            }

        }
        return matches;
    }

    public static void main(String[] args) {
        System.out.println("==" + isIDNumber("430522200209287594"));
        System.out.println("==" + isMacAddress("24:79:f3:5b:c2:6b"));
    }
}
