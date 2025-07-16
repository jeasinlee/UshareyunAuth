package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SmsConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SmsConfigService extends IService<SmsConfig> {

    BaseResult update(SmsConfig smsConfig);

    Page<SmsConfig> getList(Page<SmsConfig> page, QueryWrapper wrapper);

}
