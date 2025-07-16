package cn.ushare.account.util;

import java.util.List;

import lombok.Data;

/**
 * 树节点
 */
@Data
public class TreeNode {

    public List<TreeNode> children;
    public Integer id;
    public Integer parentId;
    public String label;
    //public String value; 

}
