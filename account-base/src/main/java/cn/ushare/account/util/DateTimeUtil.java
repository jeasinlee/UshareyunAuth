package cn.ushare.account.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间格式工具类
 *
 * @author xie
 *
 */
public class DateTimeUtil {

    // 各种时间格式
    public static final SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat yyyyMMdd_ = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat MMdd = new SimpleDateFormat("MM-dd");
    public static final SimpleDateFormat date_sdf_wz = new SimpleDateFormat("yyyy年MM月dd日");
    public static final SimpleDateFormat date_sdf_ym = new SimpleDateFormat("yyyy年MM月");
    public static final SimpleDateFormat time_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat yyyymmddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat short_time_sdf = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat mm = new SimpleDateFormat("mm");
    public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat yyyyMMdd_HH = new SimpleDateFormat("yyyy-MM-dd HH");
    public static final SimpleDateFormat yyyMM = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat yyy_MM = new SimpleDateFormat("yyyyMM");
    public static final SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat dd = new SimpleDateFormat("dd");
    public static final SimpleDateFormat MM = new SimpleDateFormat("MM");
    public static final SimpleDateFormat Hms = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat MMddyyyy_HHMMSS = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public final static SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSS'Z'");
    
    // 以毫秒表示的时间
    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;
    private static final long HOUR_IN_MILLIS = 3600 * 1000;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long SECOND_IN_MILLIS = 1000;

    // 指定模式的时间格式
    private static SimpleDateFormat getSDFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * 当前日历，这里用中国时间表示
     *
     * @return 以当地时区表示的系统当前日历
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 指定毫秒数表示的日历
     *
     * @param millis
     *            毫秒数
     * @return 指定毫秒数表示的日历
     */
    public static Calendar getCalendar(long millis) {
        Calendar cal = Calendar.getInstance();
        // --------------------cal.setTimeInMillis(millis);
        cal.setTime(new Date(millis));
        return cal;
    }

    /**
     * 当前日期
     *
     * @return 系统当前时间
     */
    public static Date getDate() {
        return new Date();
    }

    /**
     * 指定毫秒数表示的日期
     *
     * @param millis
     *            毫秒数
     * @return 指定毫秒数表示的日期
     */
    public static Date getDate(long millis) {
        return new Date(millis);
    }

    /**
     * 时间戳转换为字符串
     *
     * @param time
     * @return
     */
    public static String timestamptoStr(Timestamp time) {
        Date date = null;
        if (null != time) {
            date = new Date(time.getTime());
        }
        return date2Str(date, date_sdf);
    }

    /**
     * 字符串转换时间戳
     *
     * @param str
     * @return
     */
    public static Timestamp str2Timestamp(String str) {
        Date date = str2Date(str, date_sdf);
        return new Timestamp(date.getTime());
    }

    public static Date str2Date(String str, SimpleDateFormat sdf) {
        if (null == str || "".equals(str)) {
            return null;
        }
        Date date = null;
        try {
            date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转换为字符串
     *
     * @param date_sdf 日期格式
     * @return 字符串
     */
    public static String date2Str(SimpleDateFormat date_sdf) {
        Date date = getDate();
        if (null == date) {
            return null;
        }
        return date_sdf.format(date);
    }

    /**
     * 格式化时间
     *
     * @param data
     * @param format
     * @return
     */
    public static String dataformat(String data, String format) {
        SimpleDateFormat sformat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sformat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sformat.format(date);
    }

    /**
     * 日期转换为字符串
     *
     * @param date_sdf 日期格式
     * @return 字符串
     */
    public static String date2Str(Date date, SimpleDateFormat date_sdf) {
        if (null == date) {
            return null;
        }
        return date_sdf.format(date);
    }

    /**
     * 日期转换为字符串
     *
     * @param format
     *            日期格式
     * @return 字符串
     */
    public static String getDate(String format) {
        Date date = new Date();
        /*
         * if (null == date) { return null; }
         */
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 指定毫秒数的时间戳
     *
     * @param millis
     *            毫秒数
     * @return 指定毫秒数的时间戳
     */
    public static Timestamp getTimestamp(long millis) {
        return new Timestamp(millis);
    }

    /**
     * 以字符形式表示的时间戳
     *
     * @param time
     *            毫秒数
     * @return 以字符形式表示的时间戳
     */
    public static Timestamp getTimestamp(String time) {
        return new Timestamp(Long.parseLong(time));
    }

    /**
     * 系统当前的时间戳
     *
     * @return 系统当前的时间戳
     */
    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 指定日期的时间戳
     *
     * @param date
     *            指定日期
     * @return 指定日期的时间戳
     */
    public static Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * 指定日历的时间戳
     *
     * @param cal
     *            指定日历
     * @return 指定日历的时间戳
     */
    public static Timestamp getCalendarTimestamp(Calendar cal) {
        // ---------------------return new Timestamp(cal.getTimeInMillis());
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp gettimestamp() {
        Date dt = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = df.format(dt);
        java.sql.Timestamp buydate = java.sql.Timestamp.valueOf(nowTime);
        return buydate;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getMillis
    // 各种方式获取的Millis
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 系统时间的毫秒数
     *
     * @return 系统时间的毫秒数
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 指定日历的毫秒数
     *
     * @param cal
     *            指定日历
     * @return 指定日历的毫秒数
     */
    public static long getMillis(Calendar cal) {
        // --------------------return cal.getTimeInMillis();
        return cal.getTime().getTime();
    }

    /**
     * 指定日期的毫秒数
     *
     * @param date
     *            指定日期
     * @return 指定日期的毫秒数
     */
    public static long getMillis(Date date) {
        return date.getTime();
    }

    /**
     * 指定时间戳的毫秒数
     *
     * @param ts
     *            指定时间戳
     * @return 指定时间戳的毫秒数
     */
    public static long getMillis(Timestamp ts) {
        return ts.getTime();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatDate
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：年-月-日
     *
     * @return 默认日期按“年-月-日“格式显示
     */
    public static String formatDate() {
        return date_sdf.format(getCalendar().getTime());
    }

    /**
     * 获取时间字符串
     */
    public static String getDataString(SimpleDateFormat formatstr) {
        return formatstr.format(getCalendar().getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日
     *
     * @param cal
     *            指定的日期
     * @return 指定日期按“年-月-日“格式显示
     */
    public static String formatDate(Calendar cal) {
        return date_sdf.format(cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日
     *
     * @param date
     *            指定的日期
     * @return 指定日期按“年-月-日“格式显示
     */
    public static String formatDate(Date date) {
        return date_sdf.format(date);
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：年-月-日
     *
     * @param millis
     *            指定的毫秒数
     * @return 指定毫秒数表示日期按“年-月-日“格式显示
     */
    public static String formatDate(long millis) {
        return date_sdf.format(new Date(millis));
    }

    /**
     * 默认日期按指定格式显示
     *
     * @param pattern
     *            指定的格式
     * @return 默认日期按指定格式显示
     */
    public static String formatDate(String pattern) {
        return getSDFormat(pattern).format(getCalendar().getTime());
    }

    /**
     * 指定日期按指定格式显示
     *
     * @param cal
     *            指定的日期
     * @param pattern
     *            指定的格式
     * @return 指定日期按指定格式显示
     */
    public static String formatDate(Calendar cal, String pattern) {
        return getSDFormat(pattern).format(cal.getTime());
    }

    /**
     * 指定日期按指定格式显示
     *
     * @param date
     *            指定的日期
     * @param pattern
     *            指定的格式
     * @return 指定日期按指定格式显示
     */
    public static String formatDate(Date date, String pattern) {
        return getSDFormat(pattern).format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatTime
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：年-月-日 时：分
     *
     * @return 默认日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime() {
        return time_sdf.format(getCalendar().getTime());
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param millis
     *            指定的毫秒数
     * @return 指定毫秒数表示日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(long millis) {
        return time_sdf.format(new Date(millis));
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param cal
     *            指定的日期
     * @return 指定日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(Calendar cal) {
        if (cal == null) {
            return "";
        }
        return time_sdf.format(cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param date
     *            指定的日期
     * @return 指定日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return time_sdf.format(date);
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日 时：分:秒
     *
     * @param date
     *            指定的日期
     * @return 指定日期按“年-月-日 时：分:秒“格式显示
     */
    public static String datetimeFormat(Date date) {
        if (date == null) {
            return "";
        }
        return datetimeFormat.format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatShortTime
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：时：分
     *
     * @return 默认日期按“时：分“格式显示
     */
    public static String formatShortTime() {
        return short_time_sdf.format(getCalendar().getTime());
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：时：分
     *
     * @param millis
     *            指定的毫秒数
     * @return 指定毫秒数表示日期按“时：分“格式显示
     */
    public static String formatShortTime(long millis) {
        return short_time_sdf.format(new Date(millis));
    }

    /**
     * 指定日期的默认显示，具体格式：时：分
     *
     * @param cal
     *            指定的日期
     * @return 指定日期按“时：分“格式显示
     */
    public static String formatShortTime(Calendar cal) {
        return short_time_sdf.format(cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：时：分
     *
     * @param date
     *            指定的日期
     * @return 指定日期按“时：分“格式显示
     */
    public static String formatShortTime(Date date) {
        return short_time_sdf.format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // parseDate
    // parseCalendar
    // parseTimestamp
    // 将字符串按照一定的格式转化为日期或时间
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src
     *            将要转换的原始字符窜
     * @param pattern
     *            转换的匹配格式
     * @return 如果转换成功则返回转换后的日期
     * @throws ParseException
     */
    public static Date parseDate(String src, String pattern){
        try {
            return getSDFormat(pattern).parse(src);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date();
    }

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src
     *            将要转换的原始字符窜
     * @param pattern
     *            转换的匹配格式
     * @return 如果转换成功则返回转换后的日期
     * @throws ParseException
     */
    public static Calendar parseCalendar(String src, String pattern)
            throws ParseException {

        Date date = parseDate(src, pattern);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static String formatAddDate(String src, String pattern, int amount)
            throws ParseException {
        Calendar cal;
        cal = parseCalendar(src, pattern);
        cal.add(Calendar.DATE, amount);
        return formatDate(cal);
    }

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src
     *            将要转换的原始字符窜
     * @param pattern
     *            转换的匹配格式
     * @return 如果转换成功则返回转换后的时间戳
     * @throws ParseException
     */
    public static Timestamp parseTimestamp(String src, String pattern)
            throws ParseException {
        Date date = parseDate(src, pattern);
        return new Timestamp(date.getTime());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // dateDiff
    // 计算两个日期之间的差值
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 计算两个时间之间的差值，根据标志的不同而不同
     *
     * @param flag
     *            计算标志，表示按照年/月/日/时/分/秒等计算
     * @param calSrc
     *            减数
     * @param calDes
     *            被减数
     * @return 两个日期之间的差值
     */
    public static int dateDiff(char flag, Calendar calSrc, Calendar calDes) {

        long millisDiff = getMillis(calSrc) - getMillis(calDes);

        if (flag == 'y') {
            return (calSrc.get(Calendar.YEAR) - calDes.get(Calendar.YEAR));
        }

        if (flag == 'd') {
            return (int) (millisDiff / DAY_IN_MILLIS);
        }

        if (flag == 'h') {
            return (int) (millisDiff / HOUR_IN_MILLIS);
        }

        if (flag == 'm') {
            return (int) (millisDiff / MINUTE_IN_MILLIS);
        }

        if (flag == 's') {
            return (int) (millisDiff / SECOND_IN_MILLIS);
        }

        return 0;
    }

    public static int getYear() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(getDate());
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 将long类型时间转化成指定格式的String类型时间
     *
     * @param date
     * @param fmt
     *            (格式，如： yyyy-MM-dd HH:mm:ss)
     * @return
     */
    public static String longToString(long date, String fmt) {
        Date dateTime = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        String timeFormat = sdf.format(dateTime);
        return timeFormat;
    }

    /**
     * 获得星期的最后一天
     *
     * @param year
     * @param month
     * @param week
     * @return
     */
    public static String getWeekEnd(int year, int month, int week) {
        String temp = year + "-" + month + "-" + "01";
        Date date = str2Date(temp, date_sdf);
        --month;
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(date);

        now.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.FRIDAY);
        now.add(GregorianCalendar.DAY_OF_MONTH, 7 * (week - 1));

        return date2Str(now.getTime(), date_sdf);
    }

    /**
     * 获得星期的第一天
     *
     * @param year
     * @param month
     * @param week
     * @return
     */
    public static String getWeekStart(int year, int month, int week) {
        String temp = year + "-" + month + "-" + "01";
        Date date = str2Date(temp, date_sdf);
        --month;
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(date);

        now.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.TUESDAY);
        now.add(GregorianCalendar.DAY_OF_MONTH, 7 * (week - 1));
        now.add(GregorianCalendar.DAY_OF_MONTH, -1);

        return date2Str(now.getTime(), date_sdf);
    }

    /**
     * 获取上个月
     *
     * @param date
     * @return
     */
    public static Date getLastMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /**
     * 格式化时间段
     *
     * @param duration
     * @return
     * @throws ParseException
     */
    public static String getDuration(Long duration) throws ParseException {
        String result = "00:00:00";
        Long plus = Hms.parse(result).getTime() + duration * 1000;
        Date date = new Date(plus);
        return Hms.format(date);
    }

    // 获得当前日期与本周一相差的天数
    private static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        cd.add(Calendar.DATE, -cd.get(Calendar.DAY_OF_WEEK) - 1);
        System.out.println(cd.getTime());
        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }

    // 获取上个礼拜 开始的周一的开始时间和周天的结束时间
    public static Map<String, Date> getPreviousMondayToSunday() {
        Map<String, Date> map = new HashMap<String, Date>();
        int mondayPlus = getMondayPlus();
        {
            GregorianCalendar currentDate = new GregorianCalendar();
            currentDate.add(Calendar.DATE,
                    -currentDate.get(Calendar.DAY_OF_WEEK) - 1);
            currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
            currentDate.set(currentDate.get(GregorianCalendar.YEAR),
                    currentDate.get(GregorianCalendar.MONTH),
                    currentDate.get(GregorianCalendar.DATE), 23, 59, 59);
            Date monday = currentDate.getTime();
            map.put("sunday", monday);// 星期天时间
        }
        {
            GregorianCalendar currentDate = new GregorianCalendar();
            currentDate.add(Calendar.DATE,
                    -currentDate.get(Calendar.DAY_OF_WEEK) - 1);
            currentDate.add(GregorianCalendar.DATE, mondayPlus);
            currentDate.set(currentDate.get(GregorianCalendar.YEAR),
                    currentDate.get(GregorianCalendar.MONTH),
                    currentDate.get(GregorianCalendar.DATE), 00, 00, 00);
            Date monday = currentDate.getTime();
            map.put("monday", monday);// 星期一时间
        }
        return map;
    }

    public static boolean isEqual(Date one, Date two) {
        boolean result = false;
        String oneTime = datetimeFormat(one);
        String twoTime = datetimeFormat(two);
        if (oneTime.equals(twoTime)) {
            result = true;
        }
        return result;
    }

    static int[] DAYS = { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    public static boolean isValidDate(String date) {
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            if (year <= 0) {
                return false;
            }
            int month = Integer.parseInt(date.substring(5, 7));
            if (month <= 0 || month > 12) {
                return false;
            }
            int day = Integer.parseInt(date.substring(8, 10));
            if (day <= 0 || day > DAYS[month]) {
                return false;
            }
            if (month == 2 && day == 29 && !isGregorianLeapYear(year)) {
                return false;
            }
            int hour = Integer.parseInt(date.substring(11, 13));
            if (hour < 0 || hour > 23) {
                return false;
            }
            int minute = Integer.parseInt(date.substring(14, 16));
            if (minute < 0 || minute > 59) {
                return false;
            }
            int second = Integer.parseInt(date.substring(17, 19));
            if (second < 0 || second > 59) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static final boolean isGregorianLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * 获取i天前后的时间
     * @param begin
     * @param field
     * @param i
     * @return
     */
    public static Date add(Date begin, int field, int i) {
        if (begin == null) {
            return null;
        }
        Calendar cal=Calendar.getInstance();
        cal.setTime(begin);
        cal.add(field, i);
        return cal.getTime();
    }

    /**
     * 判断两个时间是否在同一天
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        if (ds1.equals(ds2)) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(isValidDate("2100-02-28 26:00:01"));

        System.out.println(parseDate("2025-01-19", "yyyy-MM-dd"));

    } 
}
