package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.AdClickService;
import cn.ushare.account.entity.AdClick;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.mapper.AdClickMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

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
public class AdClickServiceImpl extends ServiceImpl<AdClickMapper, AdClick> implements AdClickService {

    @Autowired
    AdClickMapper adClickMapper;

    @Override
    public List<Map<String, Integer>> statisticToday() {
        return adClickMapper.statisticToday();
    }

    @Override
    public List<Map<String, Integer>> statisticInDays(Integer days) {
        return adClickMapper.statisticInDays(days);
    }

    @Override
    public Page<AdClick> getList(Page<AdClick> page, QueryWrapper wrapper) {
        return page.setRecords(adClickMapper.getList(page, wrapper));
    }

}
