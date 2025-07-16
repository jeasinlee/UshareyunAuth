package cn.ushare.account.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeItem {
    String id;
    String label;
    Integer sort;
    List<NodeItem> children = new ArrayList<>();
}
