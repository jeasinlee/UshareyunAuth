package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.SystemConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    @Select("SELECT * FROM system_config ${ew.customSqlSegment}")
    List<SystemConfig> getList(Page<SystemConfig> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Update("UPDATE system_config SET value = #{value}, update_time = now()" +
            " WHERE code = #{code}")
    int updateByCode(@Param("code") String code, @Param("value") String value);

    @Select("SELECT `value` FROM system_config WHERE code = #{code} AND is_valid = 1")
    String getByCode(@Param("code") String code);

    @Select("SELECT `name`, code, `value` FROM system_config" +
            " WHERE code LIKE CONCAT('${code}', '%') AND is_valid = 1")
    List<Map<String, String>> getByLike(@Param("code") String code);

}
