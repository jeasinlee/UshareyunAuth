package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.LogUploadRecordMapper;
import cn.ushare.account.admin.service.LogUploadRecordService;
import cn.ushare.account.entity.LogUploadRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @date 2019-04-29
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class LogUploadRecordServiceImpl extends ServiceImpl<LogUploadRecordMapper, LogUploadRecord> implements LogUploadRecordService {

    @Autowired
    LogUploadRecordMapper logUploadRecordMapper;

    @Override
    public Page<LogUploadRecord> getList(Page<LogUploadRecord> page, QueryWrapper wrapper) {
        return page.setRecords(logUploadRecordMapper.getList(page, wrapper));
    }

}
