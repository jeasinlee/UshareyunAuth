package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Administrator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AdministratorMapper extends BaseMapper<Administrator> {
    @Select("SELECT a.*, r.name as roleName FROM administrator a, role r" +
            " WHERE a.role_id = r.id AND a.is_valid = 1" +
            " AND r.is_valid = 1 ${ew.customSqlSegment}")
    List<Administrator> getList(Page<Administrator> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
