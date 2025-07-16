package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Bandwidth;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface BandwidthMapper extends BaseMapper<Bandwidth> {
    @Select("SELECT * FROM bandwidth ${ew.customSqlSegment}")
    List<Bandwidth> getList(Page<Bandwidth> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
