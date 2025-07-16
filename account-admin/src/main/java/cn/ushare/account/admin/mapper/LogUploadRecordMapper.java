package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.LogUploadRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-04-29
 * @email jixiang.li@ushareyun.net
 */
public interface LogUploadRecordMapper extends BaseMapper<LogUploadRecord> {
    @Select("SELECT * FROM activity_coupons ${ew.customSqlSegment}")
    List<LogUploadRecord> getList(Page<LogUploadRecord> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
