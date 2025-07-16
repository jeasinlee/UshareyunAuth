package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.MuteDeviceMapper;
import cn.ushare.account.admin.service.MuteDeviceService;
import cn.ushare.account.entity.MuteDevice;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @since 2021-12-20
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class MuteDeviceServiceImpl extends ServiceImpl<MuteDeviceMapper, MuteDevice> implements MuteDeviceService {

    @Autowired
    MuteDeviceMapper muteDeviceMapper;

    @Override
    public Page<MuteDevice> getList(Page<MuteDevice> page, QueryWrapper wrapper) {
           return page.setRecords(muteDeviceMapper.getList(page, wrapper));
    }

}
