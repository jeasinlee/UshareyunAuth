package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AlarmSetting;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-30
 * @email jixiang.li@ushareyun.net
 */
public interface AlarmSettingService extends IService<AlarmSetting> {

    Page<AlarmSetting> getList(Page<AlarmSetting> page, QueryWrapper wrapper);

}
