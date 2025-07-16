package cn.ushare.account.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Encoder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.ushare.account.util.EncryptUtils.MD5;

@Slf4j
public class StringUtil {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(String str) {
        if (null == str || 0 == str.length() || "".equals(str.trim()) || "null".equals(str)
                || "null".equals(str.trim())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  正则：手机号（简单）, 1字头＋10位数字即可.
     * @param in
     * @return
     */
    public static boolean isMobile(String in) {
        String mobile = "^[1]\\d{10}$";
        return in.matches(mobile);
    }

    public static boolean validPwd(String in){
        String pwd = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{8,16}$";
        return in.matches(pwd);
    }

    public static boolean isNotBlank(String str) {
        if (null == str || 0 == str.length() || "".equals(str.trim()) || "null".equals(str)
                || "null".equals(str.trim())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 转换成string类型
     *
     * @param obj
     * @return
     */
    public static String transStringValue(Object obj) {
        return "null".equals(String.valueOf(obj)) ? "" : String.valueOf(obj);
    }

    public static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    /**
     * @param text
     * @param searchString
     * @param replacement
     * @param max
     * @return
     */
    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase >= 0 ? increase : 0;
        increase *= max >= 0 ? max <= 64 ? max : 64 : 16;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        do {
            if (end == -1) {
                break;
            }
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        } while (true);
        buf.append(text.substring(start));
        return buf.toString();
    }

    /**
     * 把Map中的KEY转换成小写
     *
     * @param mapData
     * @return
     */
    public static Map<String, Object> keyLowerCaseTrans(Map<String, Object> mapData) {
        Map<String, Object> result = new HashMap();

        for (Map.Entry<String, Object> entry : mapData.entrySet()) {
            result.put(entry.getKey().toString().toLowerCase(), entry.getValue());
        }
        return result;
    }

    /**
     * 对象转map
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }

        return map;
    }

    /**
     * json转Map，注意导入的json的jar包是net.sf.json，不是alibaba的fastjson
     */
    public static Map<String, Object> parseJSON2Map(String jsonStr) {
        if (jsonStr == "" || jsonStr == null) {
            return null;
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            // 最外层解析
            JSONObject json = JSONObject.fromObject(jsonStr);
            for (Object k : json.keySet()) {
                Object v = json.get(k);
                // 如果内层还是数组的话，继续解析
                if (v instanceof JSONArray) {
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    Iterator<Object> it = ((JSONArray) v).iterator();
                    while (it.hasNext()) {
                        JSONObject json2 = (JSONObject) it.next();
                        list.add(parseJSON2Map(json2.toString()));
                    }
                    map.put(k.toString(), list);
                } else {
                    map.put(k.toString(), v);
                }
            }
            return map;
        }
    }

    /**
     * xml格式转Map
     */
    public static Map<String, Object> xmlToMap(String xmlStr) {
        if (xmlStr == "" || xmlStr == null) {
            return null;
        } else {
            XMLSerializer xmlSerializer = new XMLSerializer();
            JSON json = (JSON) xmlSerializer.read(xmlStr);

            return parseJSON2Map(json.toString());
        }
    }

    /**
     * 判断字符串是否是整数
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是浮点数
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains("."))
                return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是数字
     */
    public static boolean isNumber(String value) {
        return isInteger(value) || isDouble(value);
    }

    /**
     * 随机字符串
     */
    public static String getRandomString(int length) { // length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(String key) {
        String result = "";
        try {
            result = (new BASE64Encoder()).encodeBuffer(key.getBytes());
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 比较是否有新版本
     *
     * @param oldVersion
     * @param newVersion
     * @return
     */
    public static boolean compareVersion(String oldVersion, String newVersion) {
        boolean flag = false;
        if (StringUtils.isAnyBlank(oldVersion, newVersion)) {
            return flag;
        }
        String[] oldArr = oldVersion.split("\\.");
        String[] newArr = newVersion.split("\\.");
        int len = Math.min(oldArr.length, newArr.length);
        for (int i = 0; i < len; i++) {
            if (Integer.parseInt(oldArr[i]) < Integer.parseInt(newArr[i])) {
                flag = true;
                break;
            } else if (Integer.parseInt(oldArr[i]) == Integer.parseInt(newArr[i])) {
                continue;
            } else {
                break;
            }
        }
        if (!flag) {
            if (oldArr.length < newArr.length) {
                flag = true;
            }
        }

        return flag;
    }

    public static boolean isEnable(String userAccountControl) {
        if (StringUtils.isBlank(userAccountControl)) {
            return false;
        }
        int userAccContr = Integer.parseInt(userAccountControl.substring(20));
        log.error("===:" + userAccContr);
        boolean flag = true;

        //TRUSTED_TO_AUTH_FOR_DELEGATION - 允许该帐户进行委派
        if (userAccContr >= 16777216) {
            userAccContr = userAccContr - 16777216;
        }
        //PASSWORD_EXPIRED - (Windows 2000/Windows Server 2003) 用户的密码已过期
        if (userAccContr >= 8388608) {
            userAccContr = userAccContr - 8388608;
        }
        //DONT_REQ_PREAUTH
        if (userAccContr >= 4194304) {
            userAccContr = userAccContr - 4194304;
        }
        //USE_DES_KEY_ONLY - (Windows 2000/Windows Server 2003) 将此用户限制为仅使用数据加密标准 (DES) 加密类型的密钥
        if (userAccContr >= 2097152) {
            userAccContr = userAccContr - 2097152;
        }
        //NOT_DELEGATED - 设置此标志后，即使将服务帐户设置为信任其进行 Kerberos 委派，也不会将用户的安全上下文委派给该服务
        if (userAccContr >= 1048576) {
            userAccContr = userAccContr - 1048576;
        }
        //TRUSTED_FOR_DELEGATION - 设置此标志后，将信任运行服务的服务帐户（用户或计算机帐户）进行 Kerberos 委派。任何此类服务都可模拟请求该服务的客户端。若要允许服务进行 Kerberos 委派，必须在服务帐户的 userAccountControl 属性上设置此标志
        if (userAccContr >= 524288) {
            userAccContr = userAccContr - 524288;
        }
        //SMARTCARD_REQUIRED - 设置此标志后，将强制用户使用智能卡登录
        if (userAccContr >= 262144) {
            userAccContr = userAccContr - 262144;
        }
        //MNS_LOGON_ACCOUNT - 这是 MNS 登录帐户
        if (userAccContr >= 131072) {
            userAccContr = userAccContr - 131072;
        }
        //DONT_EXPIRE_PASSWORD-密码永不过期
        if (userAccContr >= 65536) {
            userAccContr = userAccContr - 65536;
        }
        //MNS_LOGON_ACCOUNT - 这是 MNS 登录帐户
        if (userAccContr >= 2097152) {
            userAccContr = userAccContr - 2097152;
        }
        //SERVER_TRUST_ACCOUNT - 这是属于该域的域控制器的计算机帐户
        if (userAccContr >= 8192) {
            userAccContr = userAccContr - 8192;
        }
        //WORKSTATION_TRUST_ACCOUNT - 这是运行 Microsoft Windows NT 4.0 Workstation、Microsoft Windows NT 4.0 Server、Microsoft Windows 2000 Professional 或 Windows 2000 Server 并且属于该域的计算机的计算机帐户
        if (userAccContr >= 4096) {
            userAccContr = userAccContr - 4096;
        }
        //INTERDOMAIN_TRUST_ACCOUNT - 对于信任其他域的系统域，此属性允许信任该系统域的帐户
        if (userAccContr >= 2048) {
            userAccContr = userAccContr - 2048;
        }
        //NORMAL_ACCOUNT - 这是表示典型用户的默认帐户类型
        if (userAccContr >= 512) {
            userAccContr = userAccContr - 512;
        }

        //TEMP_DUPLICATE_ACCOUNT - 此帐户属于其主帐户位于另一个域中的用户。此帐户为用户提供访问该域的权限，但不提供访问信任该域的任何域的权限。有时将这种帐户称为“本地用户帐户”
        if (userAccContr >= 256) {
            userAccContr = userAccContr - 256;
        }
        //ENCRYPTED_TEXT_PASSWORD_ALLOWED - 用户可以发送加密的密码
        if (userAccContr >= 128) {
            userAccContr = userAccContr - 128;
        }
        //PASSWD_CANT_CHANGE - 用户不能更改密码。可以读取此标志，但不能直接设置它
        if (userAccContr >= 64) {
            userAccContr = userAccContr - 64;
        }
        //PASSWD_NOTREQD - 不需要密码
        if (userAccContr >= 32) {
            userAccContr = userAccContr - 32;
        }
        //LOCKOUT
        if (userAccContr >= 16) {
            userAccContr = userAccContr - 16;
        }
        //HOMEDIR_REQUIRED - 需要主文件夹
        if (userAccContr >= 8) {
            userAccContr = userAccContr - 8;
        }
        if (userAccContr >= 2) {
            flag = false;
        }
        return flag;
    }

    /*
     * @ClassName Test
     * @Desc TODO   移除指定用户 ID
     * @Date 2019/8/31 14:58
     * @Version 1.0
     */
    public static String removeOne(String ids, Integer target) {
        // 返回结果
        String result = "";
        // 判断是否存在。如果存在，移除指定用户 ID；如果不存在，则直接返回空
        if(ids.indexOf(",") != -1) {
            // 拆分成数组
            String[] userIdArray = ids.split(",");
            // 数组转集合
            List<String> userIdList = new ArrayList<String>(Arrays.asList(userIdArray));
            // 移除指定用户 ID
            userIdList.remove(target.toString());
            // 把剩下的用户 ID 再拼接起来
            result = StringUtils.join(userIdList, ",");
        }
        // 返回
        return result;
    }

    public static String signureText(String content, String key){
        if(StringUtils.isAnyBlank(content, key)){
            return null;
        }

        String result = null;
        try {
            result = MD5(MD5(content) + key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean validPwdForXiaoxiang(String pwd){
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]{8,16}$");
        Matcher matcher = pattern.matcher(pwd);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(validPwdForXiaoxiang("23we1231234567811"));
    }
}
