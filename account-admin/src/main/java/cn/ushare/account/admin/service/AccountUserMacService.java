package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountUserMac;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2022-04-12
 * @email jixiang.li@ushareyun.net
 */
public interface AccountUserMacService extends IService<AccountUserMac> {

    Page<AccountUserMac> getList(Page<AccountUserMac> page, QueryWrapper wrapper);

    BaseResult add(AccountUserMac userMac);

    BaseResult update(AccountUserMac userMac);
}
