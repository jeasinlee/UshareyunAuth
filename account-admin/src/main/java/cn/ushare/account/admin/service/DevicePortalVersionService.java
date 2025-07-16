package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DevicePortalVersion;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
public interface DevicePortalVersionService extends IService<DevicePortalVersion> {

    Page<DevicePortalVersion> getList(Page<DevicePortalVersion> page, QueryWrapper wrapper) ;

}
