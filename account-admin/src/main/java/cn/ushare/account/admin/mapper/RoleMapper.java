package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Role;
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
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface RoleMapper extends BaseMapper<Role> {

    @Insert("INSERT INTO role(`name`, is_valid, create_time) VALUES (" +
            " #{name},#{isValid}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReturnId(Role role);

    @Select("SELECT * FROM role ${ew.customSqlSegment}")
    List<Role> getList(Page<Role> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
