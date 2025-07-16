package cn.ushare.account.util;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class JsonObjUtils {
    public static String obj2json(Object obj) throws Exception {
        return JSON.toJSONString(obj);
    }

    public static <T> T json2obj(String jsonStr, Class<T> clazz) throws Exception {
        return JSON.parseObject(jsonStr, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, ?> json2map(String jsonStr)     throws Exception {
            return JSON.parseObject(jsonStr, Map.class);
    }
  
    public static <T> T map2obj(Map<?, ?> map, Class<T> clazz) throws Exception {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T map2obj(Map<String, Object> map, String paramName, Class<T> clazz) throws Exception {
        return JSON.parseObject(JSON.toJSONString( map.get(paramName)), clazz);
    }

    public static <T> List map2List(Map<String, Object> map, String paramName, Class<T> clazz)throws Exception {
        return JSON.parseArray(JSON.toJSONString( map.get(paramName)), clazz);
    }
    
    public static <T> T str2obj(String jsonStr,   Class<T> clazz) throws Exception {
        return JSON.parseObject(jsonStr, clazz);
    }

    public static <T> List str2List(String jsonStr,  Class<T> clazz)throws Exception {
        return JSON.parseArray(jsonStr, clazz);
    }
}