package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountUserLocked;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountUserLockedService extends IService<AccountUserLocked> {

    Page<AccountUserLocked> getList(Page<AccountUserLocked> page, QueryWrapper wrapper);

}
