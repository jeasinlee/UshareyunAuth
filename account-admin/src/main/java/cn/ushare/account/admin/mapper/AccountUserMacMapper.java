package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountUserMac;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2022-04-02
 * @email jixiang.li@ushareyun.net
 */
public interface AccountUserMacMapper extends BaseMapper<AccountUserMac> {
    @Select("SELECT * FROM account_user_mac ${ew.customSqlSegment}")
    List<AccountUserMac> getList(Page<AccountUserMac> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
