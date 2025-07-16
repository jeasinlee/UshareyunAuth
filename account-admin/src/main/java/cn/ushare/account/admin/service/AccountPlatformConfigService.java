package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountPlatformConfig;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountPlatformConfigService extends IService<AccountPlatformConfig> {

    Page<AccountPlatformConfig> getList(Page<AccountPlatformConfig> page, QueryWrapper wrapper);

    BaseResult addOrUpdate(AccountPlatformConfig ap);
}
