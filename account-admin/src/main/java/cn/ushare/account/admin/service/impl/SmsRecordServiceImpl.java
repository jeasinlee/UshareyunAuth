package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.SmsRecordMapper;
import cn.ushare.account.admin.service.SmsRecordService;
import cn.ushare.account.entity.SmsRecord;
import cn.ushare.account.util.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SmsRecordServiceImpl extends ServiceImpl<SmsRecordMapper, SmsRecord> implements SmsRecordService {

    @Autowired
    SmsRecordMapper smsRecordMapper;
    @Autowired
    HttpServletResponse response;

    @Override
    public Page<SmsRecord> getList(Page<SmsRecord> page, QueryWrapper wrapper) {
        return page.setRecords(smsRecordMapper.getList(page, wrapper));
    }

    /**
     * 统计今天发送的条数
     */
    @Override
    public Integer countToday(String phone) {
        return smsRecordMapper.countToday(phone);
    }

    @Override
    public void excelExportRecord() throws Exception {
        SimpleDateFormat fullFormatter = new SimpleDateFormat("yyyy-MM-ddHHmmss");

        QueryWrapper<SmsRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("result", 1);
        wrapper.orderByDesc("id");
        List<SmsRecord> smsRecords = smsRecordMapper.getSucList(wrapper);
        List<List<String>> excelData = new ArrayList<>();

        List<String> head = new ArrayList<>();
        head.add("序号");
        head.add("手机号");

        String sheetName = "短信记录";
        StringBuffer fileNameBuff = new StringBuffer("短信记录报表-");
        fileNameBuff.append(fullFormatter.format(new Date()));

        smsRecords.stream().forEach( o-> {
            List<String> data = new ArrayList<>();
            data.add(excelData.size() + 1 + "");
            data.add(o.getPhone());
            excelData.add(data);
        });

        ExcelUtil.exportExcel(response, head, excelData, sheetName, fileNameBuff.toString(), 25);
    }
}
