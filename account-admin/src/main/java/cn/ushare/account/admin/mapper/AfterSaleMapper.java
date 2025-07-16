package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AfterSale;
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
public interface AfterSaleMapper extends BaseMapper<AfterSale> {
    @Select("SELECT * FROM after_sale ${ew.customSqlSegment}")
    List<AfterSale> getList(Page<AfterSale> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
