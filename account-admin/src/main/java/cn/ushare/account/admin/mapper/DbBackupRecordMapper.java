package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.DbBackupRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author jixiang.li
 * @since 2019-04-30
 * @email jixiang.li@ushareyun.net
 */
public interface DbBackupRecordMapper extends BaseMapper<DbBackupRecord> {
    @Select("SELECT * FROM db_backup_record ${ew.customSqlSegment}")
    List<DbBackupRecord> getList(Page<DbBackupRecord> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
