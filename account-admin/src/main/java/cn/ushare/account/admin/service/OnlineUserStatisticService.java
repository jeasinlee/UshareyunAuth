package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.OnlineUserStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-04-23
 * @email jixiang.li@ushareyun.net
 */
public interface OnlineUserStatisticService extends IService<OnlineUserStatistic> {

    BaseResult getStatistic();

    Page<OnlineUserStatistic> getList(Page<OnlineUserStatistic> page, QueryWrapper wrapper);

}
