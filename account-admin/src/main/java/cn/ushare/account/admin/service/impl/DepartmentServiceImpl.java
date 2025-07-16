package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.DepartmentService;
import cn.ushare.account.entity.Department;
import cn.ushare.account.util.TreeNode;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.mapper.DepartmentMapper;
import cn.ushare.account.admin.session.SessionService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    SessionService sessionService;
    @Autowired
    DepartmentMapper departmentMapper;

    @Override
    public BaseResult update(Department department) {
        // 除总部外，其他部门必须设置父级部门
        if (department.getId() != 1 && department.getParentId() == null) {
            return new BaseResult("0", "请选择上级部门", null);
        }

        // 父id不能是子部门id或者自己的id，会造成查询子部门时的死循环
        Integer id = department.getId();
        Integer parentId = department.getParentId();
        String childrenIds = departmentMapper.getChildrenIds(id);
        if (childrenIds.contains(parentId + "")) {
            return new BaseResult("0", "不能选择下级或同级部门作为上级部门", null);
        }

        // 名称不能重复
        QueryWrapper<Department> repeatQuery = new QueryWrapper();
        repeatQuery.eq("name", department.getName());
        repeatQuery.eq("is_valid", 1);
        Department repeatOne = departmentMapper.selectOne(repeatQuery);
        if (repeatOne != null && !repeatOne.getId().equals(department.getId())) {
            return new BaseResult("0", "已有该名称的部门", null);
        }

        // 带宽ID为0时，存null
        if (department.getBandwidthId() != null
                && department.getBandwidthId() == 0) {
            department.setBandwidthId(null);
            departmentMapper.setBandwidthNull(department.getId());
        }

        departmentMapper.updateById(department);
        return new BaseResult(department);
    }

    @Override
    public String getChildrenIds(Integer id){
        return departmentMapper.getChildrenIds(id);
    }

    @Override
    public Page<Department> getList(Page<Department> page, QueryWrapper wrapper) {
        List<Department> list = departmentMapper.getList(page, wrapper);
        for (int i = 0; i < list.size(); i++) {
            Department item = list.get(i);
            int num = departmentMapper.countEmployeeByDepartment(item.getId());
            item.setEmployeeNum(num);
        }
        List<Department> tree = buildTreeNode(list, null);
        return page.setRecords(tree);
    }

    /**
     * 构建树节点
     */
    public List<Department> buildTreeNode(List<Department> list, Department obj) {
//        if (list != null && list.size() > 0) {
//            TreeNode root = new TreeNode();
//            Map<Integer, TreeNode> maps = new HashMap<>();
//            // 遍历所有节点，将节点放入节点map中
//            for (Map<String, Object> temp : list) {
//                TreeNode node = new TreeNode();
//                if(temp.get("name")!=null){
//                    node.label = temp.get("name").toString();
//                }
//                if(temp.get("id")!=null){
//                    node.id = Integer.valueOf(String.valueOf(temp.get("id")));
//                }
//                if(temp.get("parent_id")!=null){
//                    node.parentId = Integer.valueOf(String.valueOf(temp.get("parent_id")));
//                }
//                maps.put(Integer.valueOf(String.valueOf(temp.get("id"))), node);
//            }
//            for (Entry<Integer, TreeNode> entry : maps.entrySet()) {
//                TreeNode e = entry.getValue();
//                Integer parentId = e.parentId;
//                TreeNode pnode = maps.get(parentId);
//                if (pnode != null) {
//                    if (pnode.children == null) {
//                        pnode.children = new ArrayList<TreeNode>();
//                    }
//                    pnode.children.add(e);
//                } else {
//                    root = e;
//                }
//            }
//            ArrayList<TreeNode> arrayNodes = new ArrayList<TreeNode>();
//            arrayNodes.add(root);
//            return arrayNodes;
//        } else {
//            return null;
//        }
        List<Department> result = new ArrayList<>();
        if(null == obj){
            //获得根节点
            List<Department> rootList = list.stream().filter(p -> p.getParentId()==-1).collect(Collectors.toList());
            for (Department one : rootList){
                result = buildTreeNode(list, one);
                result.add(one);
            }
        } else {
            //获得下级节点
            List<Department> clist = list.stream().filter(p -> p.getParentId().equals(obj.getId())).collect(Collectors.toList());
            if(clist.size() == 0){
                return result;
            }

            for(Department one : clist){
                result = buildTreeNode(list, one);
                List<Department> children = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(obj.getChild())){
                    children = obj.getChild();
                }
                children.add(one);
                obj.setChild(children);
            }
        }

        return result;
    }



}
