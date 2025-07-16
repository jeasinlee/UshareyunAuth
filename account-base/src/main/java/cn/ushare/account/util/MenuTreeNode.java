package cn.ushare.account.util;

import java.util.List;

import lombok.Data;

/**
 * 树节点
 */
@Data
public class MenuTreeNode {

    public Integer id;
    public Integer parentId;
    public String label;
    public String path;
    public String name;
    public String title;
    public String icon;
    public Integer level;
    public String component;
    public String redirect;
    public List<MenuTreeNode> children;

}
