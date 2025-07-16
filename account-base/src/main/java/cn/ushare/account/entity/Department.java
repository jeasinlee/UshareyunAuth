package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jixiang.li
 * @date 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
@Data
public class Department extends Model<Department> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 父ID
     */
    @ApiModelProperty(value = "父ID")
    @TableField("parent_id")
    @JsonInclude(Include.NON_NULL)
    private Integer parentId;

    /**
     * 父名称
     */
    @ApiModelProperty(value = "父名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String parentName;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String name;

    /**
     * 带宽策略ID
     */
    @ApiModelProperty(value = "带宽策略ID")
    @TableField("bandwidth_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandwidthId;

    /**
     * 员工人数
     */
    @ApiModelProperty(value = "员工人数")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Integer employeeNum = 0;

    /**
     * 是否允许员工授权，0拒绝，1允许
     */
    @ApiModelProperty(value = "是否允许员工授权，0拒绝，1允许")
    @TableField("is_employee_auth_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isEmployeeAuthEnable;

    /**
     * 是否使用AD域
     */
    @ApiModelProperty(value = "是否使用AD域")
    @TableField("is_ad_domain_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isAdDomainEnable;

    /**
     * 状态，0关闭，1启用
     */
    @ApiModelProperty(value = "状态，0关闭，1启用")
    @JsonInclude(Include.NON_NULL)
    private Integer state;

    /**
     * 是否有效，0无效，1有效
     */
    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "子节点")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    List<Department> child;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(value="create_time", fill=FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @TableField(value="update_time", fill=FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date updateTime;


}
