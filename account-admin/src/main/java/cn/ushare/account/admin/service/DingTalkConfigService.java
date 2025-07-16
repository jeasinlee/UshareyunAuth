package cn.ushare.account.admin.service;

import cn.ushare.account.entity.DingTalkConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-07-29
 * @email jixiang.li@ushareyun.net
 */
public interface DingTalkConfigService extends IService<DingTalkConfig> {

    Page<DingTalkConfig> getList(Page<DingTalkConfig> page, QueryWrapper wrapper);

}
