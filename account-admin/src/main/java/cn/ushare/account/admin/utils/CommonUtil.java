package cn.ushare.account.admin.utils;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {

    public static Boolean isPc(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (CommonUtil.checkAgentIsMobile(ua)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断User-Agent 是不是来自于手机
     * @param ua
     * @author Leemeea
     * @return
     */
    public static boolean checkAgentIsMobile(String ua) {
        String[] deviceArray = new String[] { "android", "iphone", "ipod",
                "ipad", "blackberry", "ucweb", "windows phone","pad","pod","iphon","ipod","ios","ipad","android","mobile","blackberry","iemobile","mqqbrowser","juc","fennec","wosbrowser","browserNG","webos","symbian","windows phone" };
        if (ua == null) {
            return false;
        }
        ua = ua.toLowerCase();
        for (String string : deviceArray) {
            if (ua.indexOf(string) > 0) {
                return true;
            }
        }
        return false;
    }
}
