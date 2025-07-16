package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.BandwidthMapper;
import cn.ushare.account.admin.service.BandwidthService;
import cn.ushare.account.entity.Bandwidth;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class BandwidthServiceImpl extends ServiceImpl<BandwidthMapper, Bandwidth> implements BandwidthService {

    @Autowired
    BandwidthMapper bandwidthMapper;

    @Override
    public Page<Bandwidth> getList(Page<Bandwidth> page, QueryWrapper wrapper) {
        return page.setRecords(bandwidthMapper.getList(page, wrapper));
    }

}
