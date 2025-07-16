package cn.ushare.account.admin.service;

import cn.ushare.account.entity.SmsRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SmsRecordService extends IService<SmsRecord> {

    Integer countToday(String phone);

    Page<SmsRecord> getList(Page<SmsRecord> page, QueryWrapper wrapper);

    void excelExportRecord() throws Exception;
}
