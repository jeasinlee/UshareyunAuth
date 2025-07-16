package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.ApService;
import cn.ushare.account.entity.Ap;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.mapper.ApMapper;
import cn.ushare.account.admin.service.AuthUserService;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class ApServiceImpl extends ServiceImpl<ApMapper, Ap> implements ApService {

    @Autowired
    ApMapper apMapper;
    @Autowired
    AuthUserService authUserService;

    @Override
    public Page<Ap> getList(Page<Ap> page, QueryWrapper wrapper) {

        List<Ap> apList = apMapper.getList(page, wrapper);
        // 统计用户
        for (int i = 0; i < apList.size(); i++) {
            Ap item = apList.get(i);
            QueryWrapper<AuthUser> query = new QueryWrapper();
            query.eq("ap_mac", item.getMac());
            query.eq("is_valid", 1);
            long num = authUserService.count(query);
            item.setAuthNum(num);
        }

        return page.setRecords(apList);
    }

    @Override
    public BaseResult addOrUpdate(Ap ap) {
        QueryWrapper<Ap> query = new QueryWrapper();
        query.eq("mac", ap.getMac());
        query.eq("is_valid", 1);
        List<Ap> repeatAp = apMapper.selectList(query);
        if (CollectionUtils.isEmpty(repeatAp)) {
            Date date = new Date();
            ap.setCreateTime(date);
            ap.setUpdateTime(date);
            apMapper.insert(ap);
        }
        return new BaseResult();
    }

}
