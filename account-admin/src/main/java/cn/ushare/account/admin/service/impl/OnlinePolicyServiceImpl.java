package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.OnlinePolicyMapper;
import cn.ushare.account.admin.service.OnlinePolicyService;
import cn.ushare.account.entity.OnlinePolicy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class OnlinePolicyServiceImpl extends ServiceImpl<OnlinePolicyMapper, OnlinePolicy> implements OnlinePolicyService {

    @Autowired
    OnlinePolicyMapper onlinePolicyMapper;

    @Override
    public Page<OnlinePolicy> getList(Page<OnlinePolicy> page, QueryWrapper wrapper) {
        return page.setRecords(onlinePolicyMapper.getList(page, wrapper));
    }

}
