package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountChargeRecordMapper;
import cn.ushare.account.admin.service.AccountChargeRecordService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.entity.AccountChargeRecord;
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
public class AccountChargeRecordServiceImpl extends ServiceImpl<AccountChargeRecordMapper, AccountChargeRecord> implements AccountChargeRecordService {

    @Autowired
    AccountChargeRecordMapper chargeRecordMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<AccountChargeRecord> getList(Page<AccountChargeRecord> page, QueryWrapper wrapper) {
        List<AccountChargeRecord> apList = chargeRecordMapper.getList(page, wrapper);
        return page.setRecords(apList);
    }

    @Override
    public BaseResult addOrUpdate(AccountChargeRecord ap) {
        chargeRecordMapper.insert(ap);
        return new BaseResult();
    }

    @Override
    public AccountChargeRecord getByOrderNum(String orderNum) {
        return chargeRecordMapper.getByOrderNum(orderNum);
    }

}
