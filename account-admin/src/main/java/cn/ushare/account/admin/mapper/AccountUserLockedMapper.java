package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountUserLocked;
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
public interface AccountUserLockedMapper extends BaseMapper<AccountUserLocked> {
    @Select("SELECT aul.*, aug.group_name AS accountGroupName FROM account_user_locked aul " +
            "LEFT JOIN account_user_group aug ON aul.account_group_id=aug.id ${ew.customSqlSegment}")
    List<AccountUserLocked> getList(Page<AccountUserLocked> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
