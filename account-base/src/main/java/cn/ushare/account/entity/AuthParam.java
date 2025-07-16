package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author jixiang.li
 * @date 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Data
@ToString
@TableName("auth_param")
public class AuthParam extends Model<AuthParam> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "登录账号")
    @TableField("user_name")
    @JsonInclude(Include.NON_NULL)
    private String userName;

    /**
     * 登录密码
     */
    @ApiModelProperty(value = "登录密码")
    @JsonInclude(Include.NON_NULL)
    private String password;

    /**
     * 用户IP
     */
    @ApiModelProperty(value = "用户IP")
    @TableField("user_ip")
    @JsonInclude(Include.NON_NULL)
    private String userIp;

    /**
     * 用户MAC，无冒号
     */
    @ApiModelProperty(value = "用户MAC，无冒号")
    @TableField("user_mac")
    @JsonInclude(Include.NON_NULL)
    private String userMac;

    /**
     * 控制器IP
     */
    @ApiModelProperty(value = "控制器IP")
    @TableField("ac_ip")
    @JsonInclude(Include.NON_NULL)
    private String acIp;

    /**
     * 控制器MAC
     */
    @ApiModelProperty(value = "控制器MAC")
    @TableField("ac_mac")
    @JsonInclude(Include.NON_NULL)
    private String acMac;

    /**
     * 控制器ID
     */
    @ApiModelProperty(value = "控制器ID")
    @TableField("ac_id")
    @JsonInclude(Include.NON_NULL)
    private Integer acId;

    /**
     * 用户原始访问地址
     */
    @ApiModelProperty(value = "用户原始访问地址")
    @TableField("user_visit_url")
    @JsonInclude(Include.NON_NULL)
    private String userVisitUrl;

    /**
     * 认证类型，认证类型，1账号密码，2短信，3微信，4一键认证，5员工授权，6二维码
     */
    @ApiModelProperty(value = "认证类型，认证类型，1账号密码，2短信，3微信，4一键认证，5员工授权，6二维码")
    @TableField(value = "auth_method", updateStrategy = FieldStrategy.NOT_EMPTY, insertStrategy = FieldStrategy.NOT_EMPTY)
    @JsonInclude(Include.NON_NULL)
    private Integer authMethod;

    /**
     * 手机
     */
    @ApiModelProperty(value = "手机")
    @JsonInclude(Include.NON_NULL)
    @TableField(value = "phone", updateStrategy = FieldStrategy.NOT_EMPTY, insertStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    /**
     * 图片验证码
     */
    @ApiModelProperty(value = "图片验证码")
    @TableField("check_code")
    @JsonInclude(Include.NON_NULL)
    private String checkCode;

    /**
     * 短信验证码
     */
    @ApiModelProperty(value = "短信验证码")
    @TableField("sms_code")
    @JsonInclude(Include.NON_NULL)
    private String smsCode;

    /**
     * 二维码序列号
     */
    @ApiModelProperty(value = "二维码序列号")
    @TableField("qrcode_sn")
    @JsonInclude(Include.NON_NULL)
    private String qrcodeSn;

    /**
     * SSID
     */
    @ApiModelProperty(value = "SSID")
    @JsonInclude(Include.NON_NULL)
    private String ssid;

    /**
     * APIP
     */
    @ApiModelProperty(value = "APIP")
    @TableField("ap_ip")
    @JsonInclude(Include.NON_NULL)
    private String apIp;

    /**
     * APMAC
     */
    @ApiModelProperty(value = "APMAC")
    @TableField("ap_mac")
    @JsonInclude(Include.NON_NULL)
    private String apMac;

    /**
     * 访客姓名
     */
    @ApiModelProperty(value = "访客姓名")
    @TableField("guest_name")
    @JsonInclude(Include.NON_NULL)
    private String guestName;

    /**
     * Ruckus登录接口
     */
    @ApiModelProperty(value = "Ruckus登录接口")
    @TableField("ruckus_ac_login_url")
    @JsonInclude(Include.NON_NULL)
    private String ruckusAcLoginUrl;

    /**
     * Ruckus退出接口
     */
    @ApiModelProperty(value = "Ruckus退出接口")
    @TableField("ruckus_ac_logout_url")
    @JsonInclude(Include.NON_NULL)
    private String ruckusAcLogoutUrl;

    /**
     * 终端类型，1pc，2android，3ios
     */
    @ApiModelProperty(value = "终端类型")
    @TableField("terminal_type")
    @JsonInclude(Include.NON_NULL)
    private Integer terminalType;

    /**
     * 微信OpenId
     */
    @ApiModelProperty(value = "微信OpenId")
    @TableField("wx_open_id")
    @JsonInclude(Include.NON_NULL)
    private String wxOpenId;

    /**
     * 访客电话
     */
    @ApiModelProperty(value = "访客电话")
    @TableField("guest_phone")
    @JsonInclude(Include.NON_NULL)
    private String guestPhone;

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
     * 是否有线认证，0不是，1是
     */
    @ApiModelProperty(value = "是否有线认证，0不是，1是")
    @TableField("is_wired")
    @JsonInclude(Include.NON_NULL)
    private Integer isWired;

    /**
     * nas IP, radius服务器ip
     */
    @ApiModelProperty(value = "nas IP, radius服务器ip")
    @TableField("nas_ip")
    @JsonInclude(Include.NON_NULL)
    private String nasIp;

    /**
     * 授权认证员工ID
     */
    @ApiModelProperty(value = "授权认证员工ID")
    @TableField("auth_employee_id")
    @JsonInclude(Include.NON_NULL)
    private Integer authEmployeeId;

    @ApiModelProperty(value = "授权认证员工账户")
    @TableField("auth_employee_name")
    @JsonInclude(Include.NON_NULL)
    private String authEmployeeName;

    @ApiModelProperty(value = "无感知在员工表查询")
    @TableField("is_employee")
    @JsonInclude(Include.NON_NULL)
    private Integer isEmployee;

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

    @TableField(exist = false)
    private Integer isFirstLogin;

    @TableField(exist = false)
    private String callbackPath;

    @TableField(exist = false)
    private Integer employee; //是否是员工端
}
