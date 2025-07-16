package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.OnlinePolicy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
public interface OnlinePolicyMapper extends BaseMapper<OnlinePolicy> {
    @Select("SELECT * FROM online_policy ${ew.customSqlSegment}")
    List<OnlinePolicy> getList(Page<OnlinePolicy> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
