package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountChargePolicy;
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
public interface AccountChargePolicyMapper extends BaseMapper<AccountChargePolicy> {
    @Select("SELECT * FROM account_charge_policy ${ew.customSqlSegment}")
    List<AccountChargePolicy> getList(Page<AccountChargePolicy> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
