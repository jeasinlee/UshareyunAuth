package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author jixiang.li
 * @date 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
@Data
public class Employee extends Model<Employee> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 部门ID
     */
    @ApiModelProperty(value = "部门ID")
    @TableField("department_id")
    @JsonInclude(Include.NON_NULL)
    private Integer departmentId;

    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String departmentName;

    /**
     * 部门
     */
    @ApiModelProperty(value = "部门")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Department department;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    @TableField("full_name")
    @JsonInclude(Include.NON_NULL)
    private String fullName;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称")
    @TableField("nick_name")
    @JsonInclude(Include.NON_NULL)
    private String nickName;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "登录账号")
    @TableField("user_name")
    @JsonInclude(Include.NON_NULL)
    private String userName;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @JsonInclude(Include.NON_NULL)
    private String password;

    /**
     * 手机
     */
    @ApiModelProperty(value = "手机")
    @JsonInclude(Include.NON_NULL)
    private String phone;

    /**
     * 性别，0女，1男
     */
    @ApiModelProperty(value = "性别，0女，1男")
    @JsonInclude(Include.NON_NULL)
    private Integer sex;

    /**
     * 带宽策略ID
     */
    @ApiModelProperty(value = "带宽策略ID")
    @TableField("bandwidth_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandwidthId;

    /**
     * 终端数限制
     */
    @ApiModelProperty(value = "终端数限制")
    @TableField("terminal_num")
    @JsonInclude(Include.NON_NULL)
    private Integer terminalNum;

    /**
     * 是否限制终端数，0否，1是
     */
    @ApiModelProperty(value = "是否限制终端数")
    @TableField("is_terminal_num_limit")
    @JsonInclude(Include.NON_NULL)
    private Integer isTerminalNumLimit;

    /**
     * 绑定MAC，逗号分隔
     */
    @ApiModelProperty(value = "绑定MAC")
    @TableField(value = "bind_macs", updateStrategy = FieldStrategy.IGNORED)
    @JsonInclude(Include.NON_NULL)
    private String bindMacs;

    /**
     * 是否绑定MAC，0否，1是
     */
    @ApiModelProperty(value = "是否绑定MAC")
    @TableField("is_bind_mac")
    @JsonInclude(Include.NON_NULL)
    private Integer isBindMac;

    /**
     * 是否允许员工授权，0拒绝，1允许
     */
    @ApiModelProperty(value = "是否允许员工授权，0拒绝，1允许")
    @TableField("is_employee_auth_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isEmployeeAuthEnable;

    /**
     * 是否启用，0无效，1有效
     */
    @ApiModelProperty(value = "是否启用，0无效，1有效")
    @TableField("is_using")
    @JsonInclude(Include.NON_NULL)
    private Integer isUsing;

    /**
     * 是否有效，0无效，1有效
     */
    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    /**
     * 是否首登改密，0不改，1改
     */
    @ApiModelProperty(value = "是否首登改密，0不改，1改")
    @TableField("is_modify")
    @JsonInclude(Include.NON_NULL)
    private Integer isModify;

    /**
     * 是否完成首登改密，0没有，1已完成
     */
    @ApiModelProperty(value = "是否完成首登改密，0没有，1已完成")
    @TableField("is_finish")
    @JsonInclude(Include.NON_NULL)
    private Integer isFinish;

    /**
     * 创建人名称
     */
    @ApiModelProperty(value = "创建人名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String createUserName = "管理员";

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
