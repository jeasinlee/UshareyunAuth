package cn.ushare.account.util;

import com.alibaba.fastjson.JSONArray;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class GzhUtil {
    //生成加密后的密码
    /**
     * @param source
     * @return
     */
    public static String encodePwd(String source){
        return EncryptUtils.encodeMD5String(
                EncryptUtils.encodeBase64String(source));
    }

    public static String generateOrderNum(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        int randInt = (int) ((Math.random() * 9 + 1) * 100000);
        return format.format(new Date()) + randInt;
    }

    public static String generateMallOrderNum(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        int randInt = (int) ((Math.random() * 9 + 1) * 100000);
        return "M" + format.format(new Date()) + randInt;
    }

    public static String generateContractCode(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
        int randInt = (int) ((Math.random() * 9 + 1) * 100000);
        return "XY" + format.format(new Date()) + randInt;
    }

    public static String generateRedpackOrderNum(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        int randInt = (int) ((Math.random() * 9 + 1) * 100000);
        return "rd" + format.format(new Date()) + randInt;
    }

    //满减券：MJQ，满折券：MZQ，立减券：LJQ
    public static String generateCouponNum(String prefix){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        int randInt = (int) ((Math.random() * 9 + 1) * 10000000);
        return prefix + format.format(new Date()) + randInt;
    }

    public static String generateExchangeCode(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        int random = (int) Math.random() * 4;
        String code = uuid.substring(random * 8, (random + 1) * 8).toUpperCase();
        return code;
    }

    //生成第三方渠道券码
    public static String[] generateTicketCode(String channel, int length) {
        String[] tickets = new String[length];
        String uuid;
        String ticket;
        int random;
        for (int i = 0; i < length; i++) {
            random = (int) Math.random() * 2;
            uuid = UUID.randomUUID().toString().replaceAll("-", "");
            ticket = channel + uuid.substring(random * 16, (random + 1) * 16);
            tickets[i] = ticket;
        }

        return tickets;
    }

    //生成区间随机数
    public static int generateRedpackMoney(int min, int max) {
        int m = 0;
        if (min == max) {
            m = min;
        } else {
            m = (int) (Math.random()*(max-min)+min);
        }

        return m;
    }

    public static String convertToYuan(Integer price){
        BigDecimal result = new BigDecimal(price).divide(new BigDecimal(100).setScale(2, BigDecimal.ROUND_HALF_UP));
        return formatToNumber(result);
    }

    //@desc 1.0~1之间的BigDecimal小数，格式化后失去前面的0,则前面直接加上0。
    //2.传入的参数等于0，则直接返回字符串"0.00"
    // 3.大于1的小数，直接格式化返回字符串
    /**
     * @param obj
     * @return
     */
    public static String formatToNumber(BigDecimal obj) {
        DecimalFormat df = new DecimalFormat("#.00");
        if(obj.compareTo(BigDecimal.ZERO)==0) {
            return "0.00";
        }else if(obj.compareTo(BigDecimal.ZERO)>0&&obj.compareTo(new BigDecimal(1))<0){
            return "0"+df.format(obj);
        }else {
            return df.format(obj);
        }
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameDay(cal1, cal2);
        } else {
            return false;
        }
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if(cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
        } else {
            return false;
        }
    }

    public static Integer getRemainSecondsOneDay1(Date currentDate) {
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }

    public static void main(String[] args) {
        String arr = new String("\"h5_url\":\"https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx210916295306659d62ce01843267db0000&package=2820971796\"");

        System.out.println(arr);
        System.out.println(encodePwd("pb@123456"));

    }

}
