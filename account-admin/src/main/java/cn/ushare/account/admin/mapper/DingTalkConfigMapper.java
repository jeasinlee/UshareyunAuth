package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.DingTalkConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author jixiang.li
 * @since 2019-07-29
 * @email jixiang.li@ushareyun.net
 */
public interface DingTalkConfigMapper extends BaseMapper<DingTalkConfig> {
    @Select("SELECT * FROM ding_talk_config ${ew.customSqlSegment}")
    List<DingTalkConfig> getList(Page<DingTalkConfig> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
