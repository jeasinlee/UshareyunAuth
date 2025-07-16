package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DeviceSoftwareVersion;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DeviceSoftwareVersionService extends IService<DeviceSoftwareVersion> {

    Page<DeviceSoftwareVersion> getList(Page<DeviceSoftwareVersion> page, QueryWrapper wrapper);

}
