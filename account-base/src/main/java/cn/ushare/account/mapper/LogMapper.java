package cn.ushare.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import cn.ushare.account.entity.SystemLog;


/**
 * 存储日志到数据库
 * 注意：要在MybatisPlusConfig中扫描该package，否则@Autowired失败
 */
@Mapper
public interface LogMapper {

   @Select("insert into system_log(ip, module, api, param, "
           + "result_code, result_data, duration, level, "
           + "is_valid, create_time) "
           + "values(#{ip}, #{module}, #{api}, #{param}, "
           + "#{resultCode}, #{resultData}, #{duration}, "
           + "#{level}, 1, sysdate())")
   void add(SystemLog systemLog);
    
}