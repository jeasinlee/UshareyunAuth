package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.SmsRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SmsRecordMapper extends BaseMapper<SmsRecord> {

    /**
     * 统计今天发送的条数
     */
    @Select("SELECT COUNT(1) FROM sms_record WHERE DATE(create_time) = CURDATE()")
    Integer countToday(String phone);

    @Select("SELECT * FROM sms_record ${ew.customSqlSegment}")
    List<SmsRecord> getList(Page<SmsRecord> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT * FROM sms_record ${ew.customSqlSegment}")
    List<SmsRecord> getSucList(@Param(Constants.WRAPPER) QueryWrapper wrapper);
}
