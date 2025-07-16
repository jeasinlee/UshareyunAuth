package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AuthParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
public interface AuthParamMapper extends BaseMapper<AuthParam> {
    @Select("SELECT * FROM auth_param ${ew.customSqlSegment}")
    List<AuthParam> getList(Page<AuthParam> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Insert("INSERT INTO auth_param(id, user_ip, user_mac, ac_ip, ac_id, auth_employee_id, auth_employee_name, user_visit_url, " +
            " ssid, ap_mac, ruckus_ac_login_url, ruckus_ac_logout_url, create_time, auth_method) VALUES (" +
            " #{id}, #{userIp},#{userMac}, #{acIp}, #{acId}, #{authEmployeeId}, #{authEmployeeName}, #{userVisitUrl}, " +
            " #{ssid}, #{apMac}, #{ruckusAcLoginUrl}, #{ruckusAcLogoutUrl}, #{createTime}, #{authMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertParam(AuthParam authParam);
}
