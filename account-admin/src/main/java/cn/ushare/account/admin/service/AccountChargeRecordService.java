package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AccountChargeRecord;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountChargeRecordService extends IService<AccountChargeRecord> {

    Page<AccountChargeRecord> getList(Page<AccountChargeRecord> page, QueryWrapper wrapper);

    BaseResult addOrUpdate(AccountChargeRecord ap);

    AccountChargeRecord getByOrderNum(String orderNum);
}
