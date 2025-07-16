package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Employee;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author jixiang.li
 * @email jixiang.li@ushareyun.net
 * @since 2019-03-15
 */
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Update("UPDATE employee SET bandwidth_id = null WHERE id = #{id}")
    Integer setBandwidthNull(Integer id);

    @Select("SELECT e.*, dp.name as departmentName FROM employee e LEFT JOIN department dp ON (" +
            " e.department_id = dp.id AND dp.is_valid = 1) ${ew.customSqlSegment}")
    List<Employee> getList(Page<Employee> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
