package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AuthUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-05-03
 * @email jixiang.li@ushareyun.net
 */
public interface AuthUserMapper extends BaseMapper<AuthUser> {

    @Update("UPDATE auth_user SET up_data_flow = 0, down_data_flow = 0 WHERE mac = #{userMac}")
    void resetFlowByMac(String userMac);

    @Select("SELECT id, auth_method, user_type, full_name, nick_name, terminal_type, user_name, phone, " +
            " show_user_name, phone, sex, ip, mac, bandwidth_id, ac_id,auth_employee_id, " +
            " auth_employee_name, ac_ip, ac_mac, ap_ip, ap_mac, ssid, is_wired, nas_ip, mac_prior," +
            " IFNULL(data_flow, 0) AS data_flow, IFNULL(up_data_flow, 0) AS up_data_flow ," +
            " IFNULL(down_data_flow, 0) AS down_data_flow, online_state, last_online_time," +
            " (CASE online_state WHEN 0 THEN IFNULL(last_online_duration, 0)" +
            "   ELSE (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(last_online_time)) END) AS last_online_duration" +
            " , wx_open_id FROM auth_user ${ew.customSqlSegment}")
    List<AuthUser> getList(Page<AuthUser> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT count(1) AS num, DATE_FORMAT(create_time, \"%Y-%m-%d\") AS createDate" +
            " FROM auth_user GROUP BY createDate ORDER BY create_time DESC")
    List statisticNewUserDaily(Page page, @Param(Constants.WRAPPER) QueryWrapper wrapper);
}
