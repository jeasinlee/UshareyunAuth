package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountOrders;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountOrdersService extends IService<AccountOrders> {

    Page<AccountOrders> getList(Page<AccountOrders> page, QueryWrapper wrapper);

    BaseResult saveObj(AccountOrders accountOrders);

    AccountOrders getByOrderNum(String orderNum);
}
