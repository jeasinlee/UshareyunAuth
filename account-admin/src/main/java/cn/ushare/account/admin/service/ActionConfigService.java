package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.ActionConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author jixiang.li
 * @date 2020-01-14
 * @email jixiang.li@ushareyun.net
 */
public interface ActionConfigService extends IService<ActionConfig> {

    BaseResult update(ActionConfig actionConfig);

    Page<ActionConfig> getList(Page<ActionConfig> page, QueryWrapper wrapper);
}
