package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class AccountUserGroup extends Model<AccountUserGroup> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "groupName")
    @TableField("group_name")
    @JsonInclude(Include.NON_NULL)
    private String groupName;

    @ApiModelProperty(value = "名称")
    @TableField("change_pwd_first")
    @JsonInclude(Include.NON_NULL)
    private Integer changePwdFirst;

    @ApiModelProperty(value = "名称")
    @TableField("is_bind_mobile")
    @JsonInclude(Include.NON_NULL)
    private Integer isBindMobile;

    @ApiModelProperty(value = "名称")
    @TableField("is_bind_email")
    @JsonInclude(Include.NON_NULL)
    private Integer isBindEmail;

    @ApiModelProperty(value = "是否允许删除或编辑")
    @TableField("allow_opt")
    @JsonInclude(Include.NON_NULL)
    private Integer allowOpt;

    @ApiModelProperty(value = "总用户数")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Integer total;

    @ApiModelProperty(value = "创建时间")
    @TableField(value="create_time", fill=FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value="update_time", fill=FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date updateTime;

}
