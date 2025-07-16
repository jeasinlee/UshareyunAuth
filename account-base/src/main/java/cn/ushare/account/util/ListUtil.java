package cn.ushare.account.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {


    /**
     * 将逗号分隔的字符串，转换成List<Long>
     *
     * @return
     */
    public static List<Long> getListFromString(String id) {

        List<Long> lists = new ArrayList<Long>();
        String[] ids = id.split(",");
        for (String idtemp : ids) {
            lists.add(new Long(idtemp));
        }
        return lists;
    }

    /**
     * 将逗号分隔的字符串，转换成List<String>
     * @param id
     * @return
     */
    public static List<String> getListStrFromString(String id) {

        List<String> lists = new ArrayList<String>();
        String[] ids = id.split(",");
        for (String idtemp : ids) {
            lists.add(idtemp);
        }
        return lists;
    }

    /**
     * 将逗号分隔的字符串，转换成List<Integer>
     *
     * @return
     */
    public static List<Integer> getListIntFromString(String id) {

        List<Integer> lists = new ArrayList<Integer>();
        String[] ids = id.split(",");
        for (String idtemp : ids) {
            lists.add(Integer.parseInt(idtemp));
        }
        return lists;
    }
}
