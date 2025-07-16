package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.RoleResourceMapper;
import cn.ushare.account.admin.service.RoleResourceService;
import cn.ushare.account.entity.RoleResource;
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
public class RoleResourceServiceImpl extends ServiceImpl<RoleResourceMapper, RoleResource> implements RoleResourceService {

    @Autowired
    RoleResourceMapper roleResourceMapper;

    @Override
    public Page<RoleResource> getList(Page<RoleResource> page, QueryWrapper wrapper) {
        return page.setRecords(roleResourceMapper.getList(page, wrapper));
    }

}
