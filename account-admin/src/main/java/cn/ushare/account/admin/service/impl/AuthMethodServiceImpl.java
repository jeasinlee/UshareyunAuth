package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AuthMethodMapper;
import cn.ushare.account.admin.service.AuthMethodService;
import cn.ushare.account.entity.AuthMethod;
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
public class AuthMethodServiceImpl extends ServiceImpl<AuthMethodMapper, AuthMethod> implements AuthMethodService {

    @Autowired
    AuthMethodMapper authMethodMapper;

    @Override
    public Page<AuthMethod> getList(Page<AuthMethod> page, QueryWrapper wrapper) {
        return page.setRecords(authMethodMapper.getList(page, wrapper));
    }

}
