package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.AdministratorService;
import cn.ushare.account.admin.service.RoleService;
import cn.ushare.account.entity.Role;
import cn.ushare.account.entity.RoleResource;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.mapper.RoleMapper;
import cn.ushare.account.admin.mapper.RoleResourceMapper;
import cn.ushare.account.admin.session.SessionService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    SessionService sessionService;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    RoleResourceMapper roleResourceMapper;
    @Autowired
    AdministratorService adminService;

    @Override
    public Page<Role> getList(Page<Role> page, QueryWrapper wrapper) {
        return page.setRecords(roleMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult add(Role role) {
        role.setIsValid(1);
        role.setUpdateTime(new Date());
        if(StringUtils.isEmpty(role.getName())){
            return new BaseResult("0", "角色名称不能为空", null);
        }
        roleMapper.insertReturnId(role);

        // 新增roleResource
        RoleResource roleResource = new RoleResource();
        roleResource.setRoleId(role.getId());
        roleResource.setResourceIds(role.getResourceIds());
        roleResource.setType(1);
        roleResourceMapper.insert(roleResource);

        return new BaseResult();
    }

    @Override
    public BaseResult update(Role role) {
        // 更新roleResource
        QueryWrapper<RoleResource> queryWrapper = new QueryWrapper();
        queryWrapper.eq("role_id", role.getId());
        queryWrapper.eq("is_valid", 1);
        RoleResource roleResource = roleResourceMapper.selectOne(queryWrapper);
        if (roleResource == null) {
            roleResource = new RoleResource();
            roleResource.setRoleId(role.getId());
            roleResource.setResourceIds(role.getResourceIds());
            roleResource.setType(1);
            roleResourceMapper.insert(roleResource);
        } else {
            roleResource.setRoleId(role.getId());
            roleResource.setResourceIds(role.getResourceIds());
            roleResource.setType(1);
            roleResourceMapper.updateById(roleResource);
        }

        roleMapper.updateById(role);
        return new BaseResult();
    }

    @Override
    public BaseResult delete(Integer id) {
        if (id == 1) {
            return new BaseResult("0", "不能删除超级管理员角色", null);
        }

        // 是否有账号使用该角色
        QueryWrapper<Administrator> adminQuery = new QueryWrapper();
        adminQuery.eq("role_id", id);
        adminQuery.eq("is_valid", 1);
        long count = adminService.count(adminQuery);
        if (count > 0) {
            return new BaseResult("0", "已有用户使用该角色，请先删除用户", null);
        }

        QueryWrapper<RoleResource> queryWrapper = new QueryWrapper();
        queryWrapper.eq("role_id", id);
        queryWrapper.eq("is_valid", 1);
        RoleResource roleResource = roleResourceMapper.selectOne(queryWrapper);
        if (roleResource != null) {
            roleResourceMapper.deleteById(roleResource.getId());
        }

        roleMapper.deleteById(id);

        return new BaseResult();
    }

    @Override
    public BaseResult<Role> getInfo(Integer id) {
        Role role = roleMapper.selectById(id);
        QueryWrapper<RoleResource> queryWrapper = new QueryWrapper();
        queryWrapper.eq("role_id", role.getId());
        queryWrapper.eq("is_valid", 1);
        RoleResource roleResource = roleResourceMapper.selectOne(queryWrapper);
        if (roleResource != null) {
            role.setResourceIds(roleResource.getResourceIds());
        }
        return new BaseResult(role);
    }

}
