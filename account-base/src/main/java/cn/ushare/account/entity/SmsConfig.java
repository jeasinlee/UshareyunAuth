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
@TableName("sms_config")
public class SmsConfig extends Model<SmsConfig> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String name;

    /**
     * 公司名称
     */
    @ApiModelProperty(value = "公司名称")
    @TableField("company_name")
    @JsonInclude(Include.NON_NULL)
    private String companyName;

    /**
     * 服务商
     */
    @ApiModelProperty(value = "服务商")
    @TableField("service_name")
    @JsonInclude(Include.NON_NULL)
    private String serviceName;

    /**
     * AccessKey
     */
    @ApiModelProperty(value = "AccessKey")
    @TableField("access_key")
    @JsonInclude(Include.NON_NULL)
    private String accessKey;

    /**
     * AccessSecret
     */
    @ApiModelProperty(value = "AccessSecret")
    @TableField("access_secret")
    @JsonInclude(Include.NON_NULL)
    private String accessSecret;

    /**
     * 模板ID
     */
    @ApiModelProperty(value = "模板ID")
    @TableField("template_id")
    @JsonInclude(Include.NON_NULL)
    private String templateId;

    /**
     * 签名
     */
    @ApiModelProperty(value = "签名")
    @JsonInclude(Include.NON_NULL)
    private String sign;

    /**
     * 过期时间，单位秒
     */
    @ApiModelProperty(value = "过期时间，单位秒")
    @TableField("expire_time")
    @JsonInclude(Include.NON_NULL)
    private Integer expireTime;

    /**
     * 短信内容
     */
    @ApiModelProperty(value = "短信内容")
    @TableField("msg_text")
    @JsonInclude(Include.NON_NULL)
    private String msgText;

    /**
     * 警报模板ID
     */
    @ApiModelProperty(value = "警报模板ID")
    @TableField("alarm_template_id")
    @JsonInclude(Include.NON_NULL)
    private String alarmTemplateId;

    /**
     * 警报短信内容
     */
    @ApiModelProperty(value = "警报短信内容")
    @TableField("alarm_msg_text")
    @JsonInclude(Include.NON_NULL)
    private String alarmMsgText;

    /**
     * URL
     */
    @ApiModelProperty(value = "URL")
    @JsonInclude(Include.NON_NULL)
    private String url;

    /**
     * 是否防恶意下发，0不防止，1防止
     */
    @ApiModelProperty(value = "是否防恶意下发，0不防止，1防止")
    @TableField("is_excessive_send_forbid")
    @JsonInclude(Include.NON_NULL)
    private Integer isExcessiveSendForbid;

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
