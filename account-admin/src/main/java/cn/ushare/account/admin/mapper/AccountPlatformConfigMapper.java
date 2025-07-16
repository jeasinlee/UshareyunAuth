package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountPlatformConfig;
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
public interface AccountPlatformConfigMapper extends BaseMapper<AccountPlatformConfig> {
    @Select("SELECT * FROM account_platform_config ${ew.customSqlSegment}")
    List<AccountPlatformConfig> getList(Page<AccountPlatformConfig> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
