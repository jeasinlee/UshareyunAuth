package cn.ushare.account.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TreeUtil {

    /**
     * 构建树节点
     */
    public static List<TreeNode> list2tree(List<TreeNode> list) {
        List<TreeNode> treeList = new ArrayList<TreeNode>();
        for (TreeNode tree : list) {
            // 找到根
            if (tree.getParentId() == 0) {
                treeList.add(tree);
            }
            // 找到子
            for (TreeNode node : list) {
                if (node.getParentId().equals(tree.getId())) {
                    if (tree.getChildren() == null) {
                        tree.setChildren(new ArrayList<TreeNode>());
                    }
                    tree.getChildren().add(node);
                }
            }
        }
        return treeList;
    }

    /**
     * 构建树节点
     */
    public static Map<String, Object> buildTreeNode(List<Map<String, Object>> list, String idName, String pIdName,
            String childrenName, String orderName) {
        if (list != null && list.size() > 0) {
            Map<String, Object> root = new HashMap<String, Object>();
            // root.put("idName", "-1");

            Map<Object, Map<String, Object>> maps = new HashMap<>();
            for (Map<String, Object> temp : list) {
                maps.put(Integer.valueOf(String.valueOf(temp.get(idName))), temp);
            }

            for (Entry<Object, Map<String, Object>> entry : maps.entrySet()) {

                Map<String, Object> e = entry.getValue();
                Integer parentId = Integer.valueOf(String.valueOf(e.get(pIdName)));

                Map<String, Object> pnode = maps.get(parentId);
                if (pnode != null) {
                    if (pnode.get(childrenName) == null) {
                        List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
                        childList.add(e);
                        /*
                         * if(e.get(orderName)!=null){ int order =
                         * Integer.valueOf(String.valueOf(e.get(orderName)));
                         * childList.add(order,e); }else{ }
                         */
                        pnode.put(childrenName, childList);
                    } else {
                        List<Map<String, Object>> childList = (List<Map<String, Object>>) pnode.get(childrenName);
                        int size = childList.size();
                        for (int i = 0; i < size; i++) {
                            if (childList.get(i).get(orderName) == null) {
                                childList.add(e);
                                break;
                            }
                            int order1 = Integer.valueOf(String.valueOf(childList.get(i).get(orderName)));
                            int order2 = Integer.valueOf(String.valueOf(e.get(orderName)));
                            if (order2 < order1) {
                                childList.add(i, e);
                                break;
                            }
                            if (i == size - 1) {
                                childList.add(i + 1, e);
                            }
                        }
                    }
                } else {
                    root = e;

                }
            }

            return root;
        } else {
            return null;
        }
    }

    /**
     * 构建业务树
     */
    public static TreeNodeExtend buildTreeNode(List<Map<String, Object>> list, List<Map<String, Object>> businessList) {
        if (list != null && list.size() > 0) {
            TreeNodeExtend root = new TreeNodeExtend();
            Map<Integer, TreeNodeExtend> maps = new HashMap<>();
            // 遍历所有节点，将节点放入节点map中
            for (Map<String, Object> temp : list) {
                TreeNodeExtend node = new TreeNodeExtend();
                node.label = temp.get("DATA_NAME").toString();
                node.id = Integer.valueOf(String.valueOf(temp.get("DATA_ID")));
                node.parentId = Integer.valueOf(String.valueOf(temp.get("PARENT_ID")));
                maps.put(Integer.valueOf(String.valueOf(temp.get("DATA_ID"))), node);
            }
            for (Map<String, Object> map : businessList) {
                TreeNodeExtend node = new TreeNodeExtend();
                node.label = map.get("DATA_NAME").toString();
                node.id = Integer.valueOf(String.valueOf(map.get("DATA_ID")));
                node.parentId = Integer.valueOf(String.valueOf(map.get("PARENT_ID")));
                if (maps.get(Integer.valueOf(String.valueOf(map.get("PARENT_ID")))) != null) {
                    if (maps.get(Integer.valueOf(String.valueOf(map.get("PARENT_ID")))).children == null) {
                        maps.get(Integer.valueOf(
                                String.valueOf(map.get("PARENT_ID")))).children = new ArrayList<TreeNodeExtend>();
                    }
                    maps.get(Integer.valueOf(String.valueOf(map.get("PARENT_ID")))).children.add(node);
                    setLeftNum(maps, Integer.valueOf(String.valueOf(map.get("PARENT_ID"))));
                }
            }

            for (Map.Entry<Integer, TreeNodeExtend> entry : maps.entrySet()) {
                TreeNodeExtend e = entry.getValue();
                Integer parentId = e.parentId;

                TreeNodeExtend pnode = maps.get(parentId);

                if (e.leftNum > 0) {
                    if (pnode != null) {
                        if (pnode.children == null) {
                            pnode.children = new ArrayList<TreeNodeExtend>();
                        }
                        pnode.children.add(e);
                    } else {
                        root = e;
                    }
                }
            }
            return root;
        } else {
            return null;
        }
    }

    private static void setLeftNum(Map<Integer, TreeNodeExtend> maps, Integer deptId) {
        maps.get(deptId).leftNum++;
        if (maps.get(maps.get(deptId).parentId) != null) {
            setLeftNum(maps, maps.get(deptId).parentId);
        }
    }
}
