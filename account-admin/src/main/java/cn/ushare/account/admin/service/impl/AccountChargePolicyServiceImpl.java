package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountChargePolicyMapper;
import cn.ushare.account.admin.service.AccountChargePolicyService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.entity.AccountChargePolicy;
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
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AccountChargePolicyServiceImpl extends ServiceImpl<AccountChargePolicyMapper, AccountChargePolicy> implements AccountChargePolicyService {

    @Autowired
    AccountChargePolicyMapper accountChargePolicyMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<AccountChargePolicy> getList(Page<AccountChargePolicy> page, QueryWrapper wrapper) {
        List<AccountChargePolicy> apList = accountChargePolicyMapper.getList(page, wrapper);
        return page.setRecords(apList);
    }

    @Override
    public BaseResult addOrUpdate(AccountChargePolicy chargePolicy) {
        if(null == chargePolicy.getId()) {
            accountChargePolicyMapper.insert(chargePolicy);
        } else {
            accountChargePolicyMapper.updateById(chargePolicy);
        }

        return new BaseResult();
    }

}
