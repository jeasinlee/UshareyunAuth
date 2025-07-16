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
 * @date 2019-07-29
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("ding_talk_config")
public class DingTalkConfig extends Model<DingTalkConfig> {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String name;

    @ApiModelProperty(value = "AppId")
    @TableField("app_id")
    @JsonInclude(Include.NON_NULL)
    private String appId;

    @ApiModelProperty(value = "AppSecret")
    @TableField("app_secret")
    @JsonInclude(Include.NON_NULL)
    private String appSecret;

    @ApiModelProperty(value = "临时放行时长，单位秒")
    @TableField("temp_pass_time")
    @JsonInclude(Include.NON_NULL)
    private Integer tempPassTime;

    @ApiModelProperty(value = "SSID")
    @JsonInclude(Include.NON_NULL)
    private String ssid;

    @ApiModelProperty(value = "corp_id")
    @TableField("corp_id")
    @JsonInclude(Include.NON_NULL)
    private String corpId;

    @ApiModelProperty(value = "authUrl")
    @TableField("auth_url")
    @JsonInclude(Include.NON_NULL)
    private String authUrl;

    @ApiModelProperty(value = "代理ID")
    @TableField("agent_id")
    @JsonInclude(Include.NON_NULL)
    private String agentId;

    @ApiModelProperty(value = "token")
    @JsonInclude(Include.NON_NULL)
    private String token;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
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
