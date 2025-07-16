package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DeviceBrand;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DeviceBrandService extends IService<DeviceBrand> {

    Page<DeviceBrand> getList(Page<DeviceBrand> page, QueryWrapper wrapper);

}
