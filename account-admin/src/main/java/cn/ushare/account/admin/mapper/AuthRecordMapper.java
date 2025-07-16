package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AuthRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AuthRecordMapper extends BaseMapper<AuthRecord> {

    @Select("SELECT count(1) FROM auth_record WHERE" +
            " date_format(create_time, '%Y%m%d') = date_format(sysdate(), '%Y%m%d')" +
            " AND is_valid = 1")
    Long countToday();

    /**
     * 根据userIp查最新一条记录
     */
    @Select("SELECT * FROM auth_record WHERE" +
            " ip = #{ip} ORDER BY id DESC LIMIT 1")
    AuthRecord getTopOne(String userIp);

    /**
     * 根据userIp查最新一条记录
     */
    @Select("SELECT * FROM auth_record WHERE mac = #{mac}" +
            " ORDER BY id DESC LIMIT 1")
    AuthRecord getTopOneByMac(String userIp);

    @Select("SELECT m.name AS name, count(1) AS value FROM auth_record a, auth_method m" +
            " WHERE a.auth_method = m.id AND a.is_valid = 1 AND m.is_valid = 1" +
            " GROUP BY m. NAME ORDER BY m.id")
    List<Map<String, Object>> statisticAuthMethod();

    @Select("SELECT count(1) AS value, CASE terminal_type WHEN 1 THEN" +
            " 'PC' WHEN 2 THEN 'Android' WHEN 3 THEN 'iOS'" +
            " END AS name FROM auth_record WHERE is_valid = 1 GROUP BY terminal_type")
    List<Map<String, Object>> statisticTerminalType();

    @Select("SELECT id, auth_method, user_type, full_name, nick_name, phone, user_name, " +
            " show_user_name, phone, sex, ip, mac, bandwidth_id, terminal_type, ac_id, " +
            " ac_ip, ac_mac, ap_ip, ap_mac, ssid, data_flow, auth_employee_id, auth_employee_name," +
            " up_data_flow, down_data_flow, online_state, last_online_time, mac_prior," +
            " (CASE online_state" +
            "   WHEN 0 THEN last_online_duration" +
            "   ELSE (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(last_online_time))" +
            " END) AS last_online_duration FROM auth_record ${ew.customSqlSegment}")
    List<AuthRecord> getList(Page<AuthRecord> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT show_user_name, sum(down_data_flow) as down_data_flow, mac" +
            " FROM auth_record WHERE date_format(create_time, '%Y-%m-%d') &gt;= #{dayStr}" +
            " GROUP BY mac ORDER BY down_data_flow DESC LIMIT 5 ")
    List<AuthRecord> flowTop(String dayStr);

    @Select("SELECT show_user_name, sum(" +
            " (CASE online_state WHEN 0 THEN last_online_duration" +
            " ELSE (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(last_online_time))" +
            " END)) AS last_online_duration, mac FROM auth_record" +
            " WHERE date_format(create_time, '%Y-%m-%d') &gt;= #{dayStr}" +
            " GROUP BY mac ORDER BY last_online_duration DESC LIMIT 5 ")
    List<AuthRecord> periodTop(String dayStr);

    /**
     * 查某小时内的用户数量
     */
    @Select("SELECT count(distinct mac) AS num FROM auth_record WHERE " +
            "date_format(create_time, '%Y-%m-%d %H') = #{hourStr} AND is_valid = 1")
    Integer countByHour(String hourStr);

    /**
     * 查某天内的用户数量
     */
    @Select("SELECT count(distinct mac) AS num FROM auth_record WHERE" +
            " date_format(create_time, '%Y-%m-%d') = #{dayStr} AND is_valid = 1")
    Integer countByDay(String dayStr);

    /**
     * 查多天内的用户数量
     */
    @Select("SELECT count(1) FROM auth_record WHERE " +
            " date_format(create_time, '%Y-%m-%d') &gt;= #{startTime} AND " +
            " date_format(create_time, '%Y-%m-%d') &lt;= #{endTime} AND is_valid = 1")
    Integer countByDays(@Param("startTime") String startTime,
            @Param("endTime") String endTime);

}
