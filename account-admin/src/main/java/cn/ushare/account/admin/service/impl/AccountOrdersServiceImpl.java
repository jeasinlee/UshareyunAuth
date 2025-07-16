package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountOrdersMapper;
import cn.ushare.account.admin.service.AccountOrdersService;
import cn.ushare.account.entity.AccountOrders;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2022-04-02
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AccountOrdersServiceImpl extends ServiceImpl<AccountOrdersMapper, AccountOrders> implements AccountOrdersService {

    @Autowired
    AccountOrdersMapper accountOrdersMapper;

    @Override
    public Page<AccountOrders> getList(Page<AccountOrders> page, QueryWrapper wrapper) {
        List<AccountOrders> ordersList = accountOrdersMapper.getList(page, wrapper);
        return page.setRecords(ordersList);
    }

    @Override
    public BaseResult saveObj(AccountOrders ap) {
        accountOrdersMapper.insert(ap);
        return new BaseResult();
    }

    @Override
    public AccountOrders getByOrderNum(String orderNum) {
        return accountOrdersMapper.getByOrderNum(orderNum);
    }

}
