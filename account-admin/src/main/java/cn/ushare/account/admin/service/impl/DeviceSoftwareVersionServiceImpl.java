package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.DeviceSoftwareVersionMapper;
import cn.ushare.account.admin.service.DeviceSoftwareVersionService;
import cn.ushare.account.entity.DeviceSoftwareVersion;
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
public class DeviceSoftwareVersionServiceImpl extends ServiceImpl<DeviceSoftwareVersionMapper, DeviceSoftwareVersion> implements DeviceSoftwareVersionService {

    @Autowired
    DeviceSoftwareVersionMapper deviceSoftwareVersionMapper;

    @Override
    public Page<DeviceSoftwareVersion> getList(Page<DeviceSoftwareVersion> page, QueryWrapper wrapper) {
        return page.setRecords(deviceSoftwareVersionMapper.getList(page, wrapper));
    }

}
