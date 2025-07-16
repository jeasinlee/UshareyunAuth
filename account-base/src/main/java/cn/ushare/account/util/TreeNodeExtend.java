package cn.ushare.account.util;

import java.util.List;

public class TreeNodeExtend {
    
    public List<TreeNodeExtend> children ;
    public String label;
    public Integer id;
    public Integer parentId;
    public String unitTag;
    public int leftNum; // 层级，从1-3
    public int sequence;// 排序
    
    public List<TreeNodeExtend> getChildren() {
        return children;
    }
    public void setChildren(List<TreeNodeExtend> children) {
        this.children = children;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getParentId() {
        return parentId;
    }
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    public String getUnitTag() {
        return unitTag;
    }
    public void setUnitTag(String unitTag) {
        this.unitTag = unitTag;
    }
    public int getLeftNum() {
        return leftNum;
    }
    public void setLeftNum(int leftNum) {
        this.leftNum = leftNum;
    }
    public int getSequence() {
        return sequence;
    }
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    
}
