package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.FuncResourceMapper;
import cn.ushare.account.admin.mapper.RoleResourceMapper;
import cn.ushare.account.admin.service.FuncResourceService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.NodeItem;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.FuncResource;
import cn.ushare.account.entity.RoleResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class FuncResourceServiceImpl extends ServiceImpl<FuncResourceMapper, FuncResource>
        implements FuncResourceService {

    @Autowired
    SessionService sessionService;
    @Autowired
    FuncResourceMapper funcResourceMapper;
    @Autowired
    RoleResourceMapper roleResourceMapper;

    @Override
    public Page<FuncResource> getList(Page<FuncResource> page, QueryWrapper wrapper) {
        return page.setRecords(funcResourceMapper.getList(page, wrapper));
    }

    /**
     * 菜单path列表，
     * 前端界面注意，一级菜单不能做过滤，因为子菜单没有全部勾选的情况下，
     * 后台接口不会返回父级菜单ID，会导致父菜单不显示
     */
    @Override
    public BaseResult getMenuPathList(boolean isAccount) {
        List<Map<String, Object>> list = null;
        if (sessionService.getAdminId() == null) {
            return new BaseResult("0", "主菜单Url获取失败，请登录", null);
        }
        // 超级管理员的userId为1，有全部菜单权限
        if (sessionService.getAdminId() == 1) {
            list = funcResourceMapper.getAllList();
        } else {
            Administrator admin = sessionService.getAdminInfo();
            // 查询角色对应的资源ids
            QueryWrapper<RoleResource> queryWrapper = new QueryWrapper();
            queryWrapper.eq("role_id", admin.getRoleId());
            queryWrapper.eq("is_valid", 1);
            RoleResource roleResource = roleResourceMapper.selectOne(queryWrapper);

            // 根据资源ids查资源详情
            String idsStr = roleResource.getResourceIds();
            String[] idArray = idsStr.split(",");
            list = funcResourceMapper.getListByIds(Arrays.asList(idArray));
        }
        log.debug(list.toString());

        if(!isAccount){
            list.removeIf(obj -> 44 == Integer.parseInt(obj.get("id").toString()) || 44 == Integer.parseInt(obj.get("parentId").toString()));
            list.removeIf(obj -> 45 == Integer.parseInt(obj.get("id").toString()) || 45 == Integer.parseInt(obj.get("parentId").toString()));
            list.removeIf(obj -> 46 == Integer.parseInt(obj.get("id").toString()) || 46 == Integer.parseInt(obj.get("parentId").toString()));
        }

        log.debug("======after:" + list.toString());

        if (list != null && list.size() > 0) {
            List<String> pathList = new ArrayList<>();
            for (Map<String, Object> item : list) {
                pathList.add((String) item.get("attr"));
            }
            return new BaseResult(pathList);
        } else {
            return new BaseResult("0", "没有接口权限", null);
        }
    }

    /**
     * 菜单树
     */
    @Override
    public BaseResult getTree(boolean isAccount) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (sessionService.getAdminId() == null) {
            return new BaseResult("0", "权限列表获取失败，请登录", null);
        }
        log.info("sessionService.getAdminId(): " + sessionService.getAdminId());
        // 超级管理员的userId为1，有全部菜单权限
        if (sessionService.getAdminId() == 1) {
            list = funcResourceMapper.getAllMenuList();
        } else {
            Administrator admin = sessionService.getAdminInfo();
            // 查询角色对应的资源ids
            QueryWrapper<RoleResource> queryWrapper = new QueryWrapper();
            queryWrapper.eq("role_id", admin.getRoleId());
            queryWrapper.eq("is_valid", 1);
            RoleResource roleResource = roleResourceMapper.selectOne(queryWrapper);

            // 根据资源ids查资源详情
            String idsStr = roleResource.getResourceIds();
            if (!StringUtils.isEmpty(idsStr)) {
                idsStr = idsStr + ",1";// 加上根菜单id
            } else {
                idsStr = "1";// 加上根菜单id
            }
            String[] idArray = idsStr.split(",");
            if(idArray.length>0) {
                list = funcResourceMapper.getMenuListByIds(Arrays.asList(idArray));
            }
        }
        log.debug(list.toString());
        if(!isAccount){
            list.removeIf(obj -> 44 == Integer.parseInt(obj.get("id").toString()) || 44 == Integer.parseInt(obj.get("parentId").toString()));
            list.removeIf(obj -> 45 == Integer.parseInt(obj.get("id").toString()) || 45 == Integer.parseInt(obj.get("parentId").toString()));
            list.removeIf(obj -> 46 == Integer.parseInt(obj.get("id").toString()) || 46 == Integer.parseInt(obj.get("parentId").toString()));
        }

        // 构造树形结构
        if (list != null && list.size() > 0) {
            List<NodeItem> tree = buildTreeNode(list);
            return new BaseResult(tree);
        } else {
            return new BaseResult("0", "没有接口权限", null);
        }
    }

    /**
     * 构建树
     */
    public static List<NodeItem> buildTreeNode(List<Map<String, Object>> list) {
        if (list != null && list.size() > 0) {
            List<NodeItem> root = new ArrayList<>();
            NodeItem item;

            // 遍历所有节点，将节点放入节点map中
            Map<Object, Map<String, Object>> maps = new HashMap<>();
            for (Map<String, Object> temp : list) {
                String id = String.valueOf(temp.get("id"));
                if(!id.equals("1")){
                    maps.put(id, temp);
                }
            }

            for (Entry<Object, Map<String, Object>> entry : maps.entrySet()) {
                log.info("===" + root);
                Map<String, Object> e = entry.getValue();
                String parentId = String.valueOf(e.get("parentId"));
                Map<String, Object> pnode = maps.get(parentId);
                if(null != pnode){
                    //判断父节点是否已经加入
                    boolean isJoin = root.stream().filter(n->n.getId().equals(parentId)).collect(Collectors.toList()).size()>0;
                    if(!isJoin) {
                        NodeItem child = new NodeItem();
                        child.setId(e.get("id").toString());
                        child.setLabel(e.get("label").toString());
                        child.setSort(Integer.parseInt(e.get("sort").toString()));

                        item = new NodeItem();
                        item.setId(parentId);
                        item.setLabel(pnode.get("label").toString());
                        item.setSort(Integer.parseInt(pnode.get("sort").toString()));
                        item.getChildren().add(child);

                        root.add(item);
                    } else {
                        NodeItem p = root.stream().filter(m -> m.getId().equals(parentId)).findFirst().get();
                        Optional<NodeItem> ex = p.getChildren().stream().filter(m -> m.getSort() < Integer.parseInt(pnode.get("sort").toString()))
                                .findFirst();
                        int index = p.getChildren().size();
                        if(null != ex && ex.isPresent()) {
                            index = p.getChildren().indexOf(ex.get());
                        }

                        item = new NodeItem();
                        item.setId(e.get("id").toString());
                        item.setLabel(e.get("label").toString());
                        item.setSort(Integer.parseInt(e.get("sort").toString()));
                        p.getChildren().add(index, item);
                    }
                } else {
                    boolean isExsit = root.stream().filter(n->n.getId().equals(e.get("id").toString()))
                            .collect(Collectors.toList()).size()>0;
                    if(!isExsit) {
                        Optional<NodeItem> ex = root.stream().filter(m -> m.getSort() < Integer.parseInt(e.get("sort").toString()))
                                .findFirst();
                        int index = root.size();
                        if(null != ex && ex.isPresent()) {
                            index = root.indexOf(ex.get());
                        }

                        item = new NodeItem();
                        item.setId(e.get("id").toString());
                        item.setLabel(e.get("label").toString());
                        item.setSort(Integer.parseInt(e.get("sort").toString()));
                        root.add(index, item);
                    }
                }
            }
            return root;
        } else {
            return null;
        }
    }

    /**
     * 根据userId查询对应的角色，查询角色拥有权限的2级菜单，查询菜单管辖的按钮权限uri列表
     */
    @Override
    public List<String> getUriListByUserId(int userId) {
        String ids = funcResourceMapper.getIdsByUserId(userId);
        String[] idArray = ids.split(",");
        if(idArray.length>0) {
            List<String> list = funcResourceMapper.getChildAttrsByIds(Arrays.asList(idArray));
            return list;
        } else {
            return new ArrayList<>();
        }
    }

}
