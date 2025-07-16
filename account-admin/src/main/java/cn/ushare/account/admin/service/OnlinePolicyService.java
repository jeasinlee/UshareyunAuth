package cn.ushare.account.admin.service;

import cn.ushare.account.entity.OnlinePolicy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
public interface OnlinePolicyService extends IService<OnlinePolicy> {

    Page<OnlinePolicy> getList(Page<OnlinePolicy> page, QueryWrapper wrapper);

}
