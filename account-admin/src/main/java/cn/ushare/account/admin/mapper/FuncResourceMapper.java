package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.FuncResource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface FuncResourceMapper extends BaseMapper<FuncResource> {

    /**
     * 根据userId查询对应的角色，查询角色拥有权限资源Id
     */
    @Select("SELECT rr.resource_ids FROM administrator a, role r, role_resource rr" +
            " WHERE a.id = #{id} AND r.id = a.role_id AND rr.role_id = r.id ")
    String getIdsByUserId(int userId);

    @Select("SELECT id, parent_id AS parentId, name AS label, attr, level, icon_url, sort " +
            " FROM func_resource WHERE is_valid = 1 ORDER BY level, sort")
    List<Map<String, Object>> getAllList();

    /**
     * 查全部菜单，只查level为0,1,2级的菜单
     */
    @Select("SELECT id, parent_id AS parentId, name AS label, attr, level, icon_url, sort" +
            " FROM func_resource WHERE level<3 AND is_valid = 1 ORDER BY level, sort")
    List<Map<String, Object>> getAllMenuList();

    /**
     * 根据主键列表查询
     */
    @Select("<script>" +
            "SELECT id, parent_id AS parentId, name AS label, attr, `level`, icon_url, `sort`" +
            " FROM func_resource WHERE id IN" +
            "   <foreach collection='list' item='id' index='index' open='(' close=')' separator=','>" +
            "      #{id}" +
            "   </foreach>" +
            " AND is_valid = 1 ORDER BY level, `sort`" +
            "</script>")
    List<Map<String, Object>> getListByIds(@Param("list") List<String> ids);

    /**
     * 根据主键列表查询，只查level为0,1,2级的菜单
     */
    @Select("<script>" +
            "SELECT id, parent_id AS parentId, name AS label, attr, `level`, icon_url, `sort`" +
            " FROM func_resource WHERE id IN" +
            "   <foreach collection='list' item='id' index='index' open='(' close=')' separator=','>" +
            "      #{id}" +
            "   </foreach>" +
            " AND is_valid = 1 ORDER BY `level`, `sort`" +
            "</script>")
    List<Map<String, Object>> getMenuListByIds(@Param("list") List<String> ids);

    /**
     * 根据主键id查询子菜单attr
     */
    @Select("<script>SELECT attr" +
            " FROM func_resource WHERE parent_id IN" +
            "   <foreach collection='list' item='id' index='index' open='(' close=')' separator=','>" +
            "      #{id}" +
            "   </foreach>" +
            " AND is_valid = 1 ORDER BY level, `sort`" +
            "</script>")
    List<String> getChildAttrsByIds(@Param("list") List<String> ids);

    @Select("SELECT * FROM func_resource ${ew.customSqlSegment}")
    List<FuncResource> getList(Page<FuncResource> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
