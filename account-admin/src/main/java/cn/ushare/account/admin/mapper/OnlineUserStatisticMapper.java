package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.OnlineUserStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-04-23
 * @email jixiang.li@ushareyun.net
 */
public interface OnlineUserStatisticMapper extends BaseMapper<OnlineUserStatistic> {

    @Select("SELECT * FROM online_user_statistic ${ew.customSqlSegment}")
    List<OnlineUserStatistic> getList(Page<OnlineUserStatistic> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
