package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountSalesStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2020-06-04
 * @email jixiang.li@ushareyun.net
 */
public interface AccountSalesStatisticMapper extends BaseMapper<AccountSalesStatistic> {

    @Select("SELECT * FROM account_sales_statistic ${ew.customSqlSegment}")
    List<AccountSalesStatistic> getTotalSales(@Param(Constants.WRAPPER) QueryWrapper wrapper);
}
