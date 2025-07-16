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
public class AccountUser extends Model<AccountUser> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "登录名")
    @TableField("login_name")
    @JsonInclude(Include.NON_NULL)
    private String loginName;

    @ApiModelProperty(value = "昵称")
    @TableField("nick_name")
    @JsonInclude(Include.NON_NULL)
    private String nickName;

    @ApiModelProperty(value = "密码")
    @TableField(value = "pwd", updateStrategy = FieldStrategy.IGNORED)
    @JsonInclude(Include.NON_NULL)
    private String pwd;

    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String mobile;

    @ApiModelProperty(value = "email")
    @JsonInclude(Include.NON_NULL)
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private String email;

    @ApiModelProperty(value = "MAC")
    @JsonInclude(Include.NON_NULL)
    private String idcard;

    @ApiModelProperty(value = "地点")
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    @JsonInclude(Include.NON_NULL)
    private String address;

    @ApiModelProperty(value = "最近上线时间")
    @TableField(value="last_online_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date lastOnlineTime;

    @ApiModelProperty(value = "过期时间")
    @TableField(value="expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date expireTime;

    @ApiModelProperty(value = "是否改密")
    @TableField("change_pwd")
    @JsonInclude(Include.NON_NULL)
    private Integer changePwd;

    @ApiModelProperty(value = "账户分组ID")
    @TableField("account_group_id")
    @JsonInclude(Include.NON_NULL)
    private Integer accountGroupId;

    @ApiModelProperty(value = "账户分组名称")
    @TableField("account_group_name")
    @JsonInclude(Include.NON_NULL)
    private String accountGroupName;

    @ApiModelProperty(value = "策略套餐ID")
    @TableField("charge_policy_id")
    @JsonInclude(Include.NON_NULL)
    private Integer chargePolicyId;

    @ApiModelProperty(value = "带宽ID")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Integer bandId;

    @ApiModelProperty(value = "带宽名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String bandName;

    @ApiModelProperty(value = "策略套餐名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String chargePolicyName;

    @ApiModelProperty(value = "短信验证码")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String smsCode;

    @ApiModelProperty(value = "是否支持授权")
    @TableField("auth_guest")
    @JsonInclude(Include.NON_NULL)
    private Integer authGuest;

    @ApiModelProperty(value = "绑定MAC数量，大于0即为绑定数量，等于0即不绑定mac")
    @TableField("bind_mac_num")
    @JsonInclude(Include.NON_NULL)
    private Integer bindMacNum;

    @ApiModelProperty(value = "是否欠费")
    @TableField("is_debt")
    @JsonInclude(Include.NON_NULL)
    private Integer isDebt;

    @ApiModelProperty(value = "是否锁定")
    @TableField("is_locked")
    @JsonInclude(Include.NON_NULL)
    private Integer isLocked;

    @ApiModelProperty(value = "锁定原因")
    @TableField("locked_reason")
    @JsonInclude(Include.NON_NULL)
    private String lockedReason;

    @ApiModelProperty(value = "是否是注册：0忘记密码，1注册")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Integer isReg;

    @ApiModelProperty(value = "是否删除")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

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
