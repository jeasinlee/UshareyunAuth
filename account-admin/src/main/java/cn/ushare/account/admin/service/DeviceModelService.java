package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DeviceModel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DeviceModelService extends IService<DeviceModel> {

    Page<DeviceModel> getList(Page<DeviceModel> page, QueryWrapper wrapper);

}
