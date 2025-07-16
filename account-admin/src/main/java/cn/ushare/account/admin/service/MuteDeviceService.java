package cn.ushare.account.admin.service;

import cn.ushare.account.entity.MuteDevice;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2021-12-20
 * @email jixiang.li@ushareyun.net
 */
public interface MuteDeviceService extends IService<MuteDevice> {
    Page<MuteDevice> getList(Page<MuteDevice> page, QueryWrapper wrapper);
}
