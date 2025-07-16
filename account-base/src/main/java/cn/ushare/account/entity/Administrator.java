package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
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
public class Administrator extends Model<Administrator> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

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
     * 角色ID，一个管理员只对应一个角色
     */
    @ApiModelProperty(value = "角色ID，一个管理员只对应一个角色")
    @TableField("role_id")
    @JsonInclude(Include.NON_NULL)
    private Integer roleId;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String roleName;

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
     * 密码盐
     */
    @ApiModelProperty(value = "密码盐")
    @JsonInclude(Include.NON_NULL)
    private String salt;

    /**
     * 手机
     */
    @ApiModelProperty(value = "手机")
    @JsonInclude(Include.NON_NULL)
    private String phone;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    @JsonInclude(Include.NON_NULL)
    private String email;

    /**
     * 状态，0锁定，1正常
     */
    @ApiModelProperty(value = "状态，0锁定，1正常")
    @JsonInclude(Include.NON_NULL)
    private Integer state;

    /**
     * 是否有效，0无效，1有效
     */
    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

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
