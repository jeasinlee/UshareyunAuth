package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountChargePolicy;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountChargePolicyService extends IService<AccountChargePolicy> {

    Page<AccountChargePolicy> getList(Page<AccountChargePolicy> page, QueryWrapper wrapper);

    BaseResult addOrUpdate(AccountChargePolicy ap);
}
