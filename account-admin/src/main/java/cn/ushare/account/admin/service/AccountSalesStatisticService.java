package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountSalesStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-06-02
 * @email jixiang.li@ushareyun.net
 */
public interface AccountSalesStatisticService extends IService<AccountSalesStatistic> {

    List<AccountSalesStatistic> getTotalSales(QueryWrapper wrapper);
}
