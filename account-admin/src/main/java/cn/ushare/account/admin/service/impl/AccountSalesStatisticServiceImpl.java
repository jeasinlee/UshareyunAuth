package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountSalesStatisticMapper;
import cn.ushare.account.admin.service.AccountSalesStatisticService;
import cn.ushare.account.entity.AccountSalesStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-06-02
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AccountSalesStatisticServiceImpl extends ServiceImpl<AccountSalesStatisticMapper, AccountSalesStatistic> implements AccountSalesStatisticService {

    @Autowired
    AccountSalesStatisticMapper statisticMapper;


    @Override
    public List<AccountSalesStatistic> getTotalSales(QueryWrapper wrapper) {
        List<AccountSalesStatistic> totalSales = statisticMapper.getTotalSales(wrapper);
        if(CollectionUtils.isEmpty(totalSales)){
            totalSales = new ArrayList<>();
        }

        return totalSales;
    }
}
