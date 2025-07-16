package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AuthBaseTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-27
 * @email jixiang.li@ushareyun.net
 */
public interface AuthBaseTemplateMapper extends BaseMapper<AuthBaseTemplate> {
    @Select("SELECT * FROM auth_base_template ${ew.customSqlSegment}")
   List<AuthBaseTemplate> getList(Page<AuthBaseTemplate> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
