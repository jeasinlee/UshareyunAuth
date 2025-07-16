package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountUserGroup;
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
public interface AccountUserGroupMapper extends BaseMapper<AccountUserGroup> {
    @Select("SELECT aug.*, count(au.id) AS total FROM account_user_group aug " +
            " LEFT JOIN account_user au ON au.account_group_id=aug.id ${ew.customSqlSegment} " +
            " GROUP BY aug.id")
    List<AccountUserGroup> getList(Page<AccountUserGroup> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
