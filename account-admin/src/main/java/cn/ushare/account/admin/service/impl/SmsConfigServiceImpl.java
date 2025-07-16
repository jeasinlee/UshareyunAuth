package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.SmsConfigService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.entity.SmsConfig;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.mapper.SmsConfigMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SmsConfigServiceImpl extends ServiceImpl<SmsConfigMapper, SmsConfig> implements SmsConfigService {

    @Autowired
    SmsConfigMapper smsConfigMapper;
    @Autowired
    SystemConfigService systemConfigService;

    @Override
    public Page<SmsConfig> getList(Page<SmsConfig> page, QueryWrapper wrapper) {
        return page.setRecords(smsConfigMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult update(SmsConfig smsConfig) {
        smsConfigMapper.updateById(smsConfig);
        systemConfigService.updateByCode("SMS-SERVER-ID", smsConfig.getId() + "");
        return new BaseResult(smsConfig);
    }

}
