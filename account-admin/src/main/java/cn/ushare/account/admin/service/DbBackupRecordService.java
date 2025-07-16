package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DbBackupRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-04-30
 * @email jixiang.li@ushareyun.net
 */
public interface DbBackupRecordService extends IService<DbBackupRecord> {

    BaseResult localBackup() throws InterruptedException;

    BaseResult localRestore(String fileName) throws InterruptedException;

    BaseResult cloudBackup() throws InterruptedException;

    Page<DbBackupRecord> getList(Page<DbBackupRecord> page, QueryWrapper wrapper);

}
