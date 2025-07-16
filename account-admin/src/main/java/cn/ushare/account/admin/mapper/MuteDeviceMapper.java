package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.MuteDevice;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2021-12-20
 * @email jixiang.li@ushareyun.net
 */
public interface MuteDeviceMapper extends BaseMapper<MuteDevice> {

    @Select("SELECT * FROM mute_device ${ew.customSqlSegment}")
    List<MuteDevice> getList(Page<MuteDevice> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
