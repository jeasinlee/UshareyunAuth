package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountUserLockedMapper;
import cn.ushare.account.admin.service.AccountUserLockedService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.entity.AccountUserLocked;
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
public class AccountUserLockedServiceImpl extends ServiceImpl<AccountUserLockedMapper, AccountUserLocked> implements AccountUserLockedService {

    @Autowired
    AccountUserLockedMapper accountUserLockedMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<AccountUserLocked> getList(Page<AccountUserLocked> page, QueryWrapper wrapper) {
        List<AccountUserLocked> apList = accountUserLockedMapper.getList(page, wrapper);
        return page.setRecords(apList);
    }
}
