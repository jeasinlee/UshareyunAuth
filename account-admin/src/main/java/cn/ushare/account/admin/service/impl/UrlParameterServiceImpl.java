package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.UrlParameterMapper;
import cn.ushare.account.admin.service.UrlParameterService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.UrlParameter;
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
public class UrlParameterServiceImpl extends ServiceImpl<UrlParameterMapper, UrlParameter> implements UrlParameterService {

    @Autowired
    UrlParameterMapper urlParameterMapper;

    @Override
    public Page<UrlParameter> getList(Page<UrlParameter> page, QueryWrapper wrapper) {
        return page.setRecords(urlParameterMapper.getList(page, wrapper));
    }

}
