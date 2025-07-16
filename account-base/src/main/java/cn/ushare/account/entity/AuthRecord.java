package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("auth_record")
public class AuthRecord extends Model<AuthRecord> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Long id;

    /**
     * 认证方式，1账号密码，2短信，3微信，4一键认证，5员工授权，6二维码
     */
    @ApiModelProperty(value = "认证方式，1账号密码，2短信，3微信，4一键认证，5员工授权，6二维码")
    @TableField("auth_method")
    @JsonInclude(Include.NON_NULL)
    private Integer authMethod;

    /**
     * 用户类型，0员工，1访客
     */
    @ApiModelProperty(value = "用户类型，0员工，1访客")
    @TableField("user_type")
    @JsonInclude(Include.NON_NULL)
    private Integer userType;

    /**
     * 用户姓名
     */
    @ApiModelProperty(value = "用户姓名")
    @TableField("full_name")
    @JsonInclude(Include.NON_NULL)
    private String fullName;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
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
     * 用于展示的用户名，微信认证时存openId，员工授权时存访客姓名，一键登录时存默认账户
     */
    @ApiModelProperty(value = "用于展示的用户名，微信认证时存openId，员工授权时存访客姓名，一键登录时存默认账户")
    @TableField("show_user_name")
    @JsonInclude(Include.NON_NULL)
    private String showUserName;

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
     * IP
     */
    @ApiModelProperty(value = "IP")
    @JsonInclude(Include.NON_NULL)
    private String ip;

    /**
     * MAC
     */
    @ApiModelProperty(value = "MAC")
    @JsonInclude(Include.NON_NULL)
    private String mac;

    /**
     * 微信OpenId
     */
    @ApiModelProperty(value = "微信OpenId")
    @TableField("wx_open_id")
    @JsonInclude(Include.NON_NULL)
    private String wxOpenId;

    /**
     * 加密手机号码，由微信服务器传递
     */
    @ApiModelProperty(value = "微信Tid")
    @TableField("wx_tid")
    @JsonInclude(Include.NON_NULL)
    private String wxTid;

    /**
     * 钉钉OpenId
     */
    @ApiModelProperty(value = "钉钉OpenId")
    @TableField("ding_talk_open_id")
    @JsonInclude(Include.NON_NULL)
    private String dingTalkOpenId;

    /**
     * 钉钉UnionId
     */
    @ApiModelProperty(value = "钉钉UnionId")
    @TableField("ding_talk_union_id")
    @JsonInclude(Include.NON_NULL)
    private String dingTalkUnionId;

    /**
     * 钉钉Nick
     */
    @ApiModelProperty(value = "钉钉Nick")
    @TableField("ding_talk_nick")
    @JsonInclude(Include.NON_NULL)
    private String dingTalkNick;

    /**
     * 带宽策略ID
     */
    @ApiModelProperty(value = "带宽策略ID")
    @TableField("bandwidth_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandwidthId;

    @ApiModelProperty(value = "在线终端数")
    @TableField("online_terminal_num")
    @JsonInclude(Include.NON_NULL)
    private Integer onlineTerminalNum;

    @ApiModelProperty(value = "终端类型，1pc，2android，3ios")
    @TableField("terminal_type")
    @JsonInclude(Include.NON_NULL)
    private Integer terminalType;

    @ApiModelProperty(value = "授权认证员工ID")
    @TableField("auth_employee_id")
    @JsonInclude(Include.NON_NULL)
    private Integer authEmployeeId;

    @ApiModelProperty(value = "授权认证员工账户")
    @TableField("auth_employee_name")
    @JsonInclude(Include.NON_NULL)
    private String authEmployeeName;

    @ApiModelProperty(value = "AC控制器ID")
    @TableField("ac_id")
    @JsonInclude(Include.NON_NULL)
    private Integer acId;

    @ApiModelProperty(value = "AC控制器IP")
    @TableField("ac_ip")
    @JsonInclude(Include.NON_NULL)
    private String acIp;

    @ApiModelProperty(value = "AC控制器MAC")
    @TableField("ac_mac")
    @JsonInclude(Include.NON_NULL)
    private String acMac;

    @ApiModelProperty(value = "ApIP")
    @TableField("ap_ip")
    @JsonInclude(Include.NON_NULL)
    private String apIp;

    @ApiModelProperty(value = "ApMAC")
    @TableField("ap_mac")
    @JsonInclude(Include.NON_NULL)
    private String apMac;

    @ApiModelProperty(value = "SSID")
    @JsonInclude(Include.NON_NULL)
    private String ssid;

    @ApiModelProperty(value = "流量，单位kb")
    @TableField("data_flow")
    @JsonInclude(Include.NON_NULL)
    private Long dataFlow;

    @ApiModelProperty(value = "上行流量，单位kb")
    @TableField("up_data_flow")
    @JsonInclude(Include.NON_NULL)
    private Long upDataFlow;

    @ApiModelProperty(value = "下行流量，单位kb")
    @TableField("down_data_flow")
    @JsonInclude(Include.NON_NULL)
    private Long downDataFlow;

    @ApiModelProperty(value = "在线状态，0下线，1在线")
    @TableField("online_state")
    @JsonInclude(Include.NON_NULL)
    private Integer onlineState;

    @ApiModelProperty(value = "最后上线时间")
    @TableField("last_online_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date lastOnlineTime;

    @ApiModelProperty(value = "最后上线时长，单位秒")
    @TableField("last_online_duration")
    @JsonInclude(Include.NON_NULL)
    private Integer lastOnlineDuration;

    @ApiModelProperty(value = "最后上线时长，单位小时")
    @TableField(exist=false)
    @JsonInclude(Include.NON_NULL)
    private Float lastOnlineDurationHour;

    @ApiModelProperty(value = "Radius计费会话标识")
    @TableField("acct_session_id")
    @JsonInclude(Include.NON_NULL)
    private String acctSessionId;

    @ApiModelProperty(value = "用户原始访问页面")
    @TableField("user_visit_url")
    @JsonInclude(Include.NON_NULL)
    private String userVisitUrl;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "mac优先，0非mac优先，1mac优先")
    @TableField("mac_prior")
    @JsonInclude(Include.NON_NULL)
    private Integer macPrior;

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
