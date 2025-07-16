package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.OnlineUserStatisticMapper;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.OnlineUserStatisticService;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.OnlineUserStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-04-23
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class OnlineUserStatisticServiceImpl extends ServiceImpl<OnlineUserStatisticMapper, OnlineUserStatistic> implements OnlineUserStatisticService {

    @Autowired
    AuthUserService authUserService;
    @Autowired
    OnlineUserStatisticMapper onlineUserStatisticMapper;

    @Override
    public BaseResult getStatistic() {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        String curTimeStr = sf.format(new Date());
        QueryWrapper<OnlineUserStatistic> queryWrapper = new QueryWrapper();
        queryWrapper.lt("record_time", curTimeStr);
        queryWrapper.orderByDesc("create_time");
        List<OnlineUserStatistic> list = onlineUserStatisticMapper.selectList(queryWrapper);

        // 只取9个时间点
        List<OnlineUserStatistic> filterList = new ArrayList<>();
        int step = list.size() / 9;
        if (step > 0) {
            // 防止当前时间的采样数量不足，造成step为0，会造成for死循环
            filterList = list.subList(0, 9);
        } else {
            filterList.addAll(list);
        }

        // 统计当前总数
        QueryWrapper<AuthUser> countQuery = new QueryWrapper();
        countQuery.eq("online_state", 1);
        countQuery.eq("is_valid", 1);
        long total = authUserService.count(countQuery);

        Collections.reverse(filterList);

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("list", filterList);

        return new BaseResult(map);
    }

    @Override
    public Page<OnlineUserStatistic> getList(Page<OnlineUserStatistic> page, QueryWrapper wrapper) {
        return page.setRecords(onlineUserStatisticMapper.getList(page, wrapper));
    }

}
