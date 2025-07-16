package cn.ushare.account.admin.service;

import cn.ushare.account.entity.Bandwidth;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface BandwidthService extends IService<Bandwidth> {

    Page<Bandwidth> getList(Page<Bandwidth> page, QueryWrapper wrapper);

}
