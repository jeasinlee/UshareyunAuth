package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DataResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DataResourceService extends IService<DataResource> {

    Page<DataResource> getList(Page<DataResource> page, QueryWrapper wrapper);

}
