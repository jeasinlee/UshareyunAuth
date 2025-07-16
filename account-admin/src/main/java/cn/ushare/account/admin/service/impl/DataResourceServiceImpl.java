package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.DataResourceMapper;
import cn.ushare.account.admin.service.DataResourceService;
import cn.ushare.account.entity.DataResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class DataResourceServiceImpl extends ServiceImpl<DataResourceMapper, DataResource> implements DataResourceService {

    @Autowired
    DataResourceMapper dataResourceMapper;

    @Override
    public Page<DataResource> getList(Page<DataResource> page, QueryWrapper wrapper) {
        return page.setRecords(dataResourceMapper.getList(page, wrapper));
    }

}
