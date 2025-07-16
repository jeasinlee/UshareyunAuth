package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountUserGroupMapper;
import cn.ushare.account.admin.service.AccountUserGroupService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.entity.AccountUserGroup;
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
public class AccountUserGroupServiceImpl extends ServiceImpl<AccountUserGroupMapper, AccountUserGroup> implements AccountUserGroupService {

    @Autowired
    AccountUserGroupMapper accountUserGroupMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<AccountUserGroup> getList(Page<AccountUserGroup> page, QueryWrapper wrapper) {
        List<AccountUserGroup> apList = accountUserGroupMapper.getList(page, wrapper);
        return page.setRecords(apList);
    }

    @Override
    public BaseResult addOrUpdate(AccountUserGroup accountUserGroup) {
       accountUserGroupMapper.insert(accountUserGroup);
        return new BaseResult();
    }

}
