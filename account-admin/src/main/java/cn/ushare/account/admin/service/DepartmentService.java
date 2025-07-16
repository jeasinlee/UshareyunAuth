package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Department;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DepartmentService extends IService<Department> {

    Page<Department> getList(Page<Department> page, QueryWrapper wrapper);

    String getChildrenIds(Integer id);

    BaseResult update(Department department);

}
