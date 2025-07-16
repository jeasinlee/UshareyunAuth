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
@TableName("sms_record")
public class SmsRecord extends Model<SmsRecord> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 手机
     */
    @ApiModelProperty(value = "手机")
    @JsonInclude(Include.NON_NULL)
    private String phone;

    /**
     * 业务类型，1访客认证，2找回密码
     */
    @ApiModelProperty(value = "业务类型，1访客认证，2找回密码，3：会员注册")
    @TableField("business_type")
    @JsonInclude(Include.NON_NULL)
    private Integer businessType;

    /**
     * 验证码
     */
    @ApiModelProperty(value = "验证码")
    @TableField("check_code")
    @JsonInclude(Include.NON_NULL)
    private String checkCode;

    /**
     * 结果，0失败，1成功
     */
    @ApiModelProperty(value = "结果，0失败，1成功")
    @JsonInclude(Include.NON_NULL)
    private Integer result;

    /**
     * 短信服务商ID
     */
    @ApiModelProperty(value = "短信服务商ID")
    @TableField("sms_company_id")
    @JsonInclude(Include.NON_NULL)
    private Integer smsCompanyId;

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
