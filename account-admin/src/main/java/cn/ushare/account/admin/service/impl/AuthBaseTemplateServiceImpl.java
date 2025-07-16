package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AuthBaseTemplateMapper;
import cn.ushare.account.admin.service.AuthBaseTemplateService;
import cn.ushare.account.entity.AuthBaseTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @since 2019-03-27
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AuthBaseTemplateServiceImpl extends ServiceImpl<AuthBaseTemplateMapper, AuthBaseTemplate> implements AuthBaseTemplateService {

    @Autowired
    AuthBaseTemplateMapper authBaseTemplateMapper;

    @Override
    public Page<AuthBaseTemplate> getList(Page<AuthBaseTemplate> page, QueryWrapper wrapper) {
        return page.setRecords(authBaseTemplateMapper.getList(page, wrapper));
    }

}
