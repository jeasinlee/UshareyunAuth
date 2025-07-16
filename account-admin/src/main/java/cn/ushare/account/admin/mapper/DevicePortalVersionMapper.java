package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.DevicePortalVersion;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
public interface DevicePortalVersionMapper extends BaseMapper<DevicePortalVersion> {
    @Select("SELECT * FROM device_portal_version ${ew.customSqlSegment}")
    List<DevicePortalVersion> getList(Page<DevicePortalVersion> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
