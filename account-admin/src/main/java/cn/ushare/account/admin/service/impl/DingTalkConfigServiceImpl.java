package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.DingTalkConfigMapper;
import cn.ushare.account.admin.service.DingTalkConfigService;
import cn.ushare.account.entity.DingTalkConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @since 2019-07-29
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class DingTalkConfigServiceImpl extends ServiceImpl<DingTalkConfigMapper, DingTalkConfig> implements DingTalkConfigService {

    @Autowired
    DingTalkConfigMapper dingTalkConfigMapper;

    @Override
    public Page<DingTalkConfig> getList(Page<DingTalkConfig> page, QueryWrapper wrapper) {
        return page.setRecords(dingTalkConfigMapper.getList(page, wrapper));
    }

}
