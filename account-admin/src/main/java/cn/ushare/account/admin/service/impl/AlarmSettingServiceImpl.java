package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AlarmSettingMapper;
import cn.ushare.account.admin.service.AlarmSettingService;
import cn.ushare.account.entity.AlarmSetting;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @date 2019-03-30
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AlarmSettingServiceImpl extends ServiceImpl<AlarmSettingMapper, AlarmSetting> implements AlarmSettingService {

    @Autowired
    AlarmSettingMapper alarmSettingMapper;

    @Override
    public Page<AlarmSetting> getList(Page<AlarmSetting> page, QueryWrapper wrapper) {
        return page.setRecords(alarmSettingMapper.getList(page, wrapper));
    }

}
