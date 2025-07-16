package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountPlatformConfigMapper;
import cn.ushare.account.admin.service.AccountPlatformConfigService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.entity.AccountPlatformConfig;
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
public class AccountPlatformConfigServiceImpl extends ServiceImpl<AccountPlatformConfigMapper, AccountPlatformConfig> implements AccountPlatformConfigService {

    @Autowired
    AccountPlatformConfigMapper accountPlatformConfigMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<AccountPlatformConfig> getList(Page<AccountPlatformConfig> page, QueryWrapper wrapper) {
        List<AccountPlatformConfig> apList = accountPlatformConfigMapper.getList(page, wrapper);
        return page.setRecords(apList);
    }

    @Override
    public BaseResult addOrUpdate(AccountPlatformConfig ap) {
        accountPlatformConfigMapper.insert(ap);
        return new BaseResult();
    }

}
