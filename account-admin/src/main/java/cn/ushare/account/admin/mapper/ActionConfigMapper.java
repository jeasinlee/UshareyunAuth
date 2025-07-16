package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.ActionConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2020-01-14
 * @email jixiang.li@ushareyun.net
 */
public interface ActionConfigMapper extends BaseMapper<ActionConfig> {
    @Select("SELECT * FROM action_config ${ew.customSqlSegment}")
    List<ActionConfig> getList(Page<ActionConfig> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Update("UPDATE action_config SET is_cur = 0 WHERE is_valid = 1")
    int clearDefault();
}
