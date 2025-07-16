package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.DeviceBrand;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DeviceBrandMapper extends BaseMapper<DeviceBrand> {



    @Select("SELECT * FROM device_brand WHERE id=#{id}")
    @Results(id = "deviceBrandMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "code", column = "code"),
        @Result(property = "name", column = "name"),
        @Result(property = "guideUrl", column = "guide_url"),
        @Result(property = "modelList", column = "id", many = @Many(select = "cn.ushare.account.admin.mapper.DeviceModelMapper.getListByBrandId")),
        @Result(property = "softwareList", column = "id", many = @Many(select = "cn.ushare.account.admin.mapper.DeviceSoftwareVersionMapper.getListByBrandId"))
    })
    DeviceBrand findDeviceBrandById(Long id);

    @Select("SELECT * FROM device_brand ${ew.customSqlSegment}")
    @ResultMap(value = "deviceBrandMap")
    List<DeviceBrand> getList(Page<DeviceBrand> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);
}
