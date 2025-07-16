package cn.ushare.account.admin.service;

import cn.ushare.account.entity.LogUploadRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-04-29
 * @email jixiang.li@ushareyun.net
 */
public interface LogUploadRecordService extends IService<LogUploadRecord> {

    Page<LogUploadRecord> getList(Page<LogUploadRecord> page, QueryWrapper wrapper);

}
