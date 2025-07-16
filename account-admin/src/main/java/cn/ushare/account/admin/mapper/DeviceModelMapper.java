package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.DeviceModel;
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
public interface DeviceModelMapper extends BaseMapper<DeviceModel> {
    @Select("SELECT * FROM device_model ${ew.customSqlSegment}")
    List<DeviceModel> getList(Page<DeviceModel> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT * FROM device_model WHERE brand_id = #{brandId}")
    List<DeviceModel> getListByBrandId(Long brandId);
}
