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
 * @date 2022-04-02
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountPlatformConfig extends Model<AccountPlatformConfig> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "支付宝状态：启用/关闭")
    @TableField("alipay_status")
    @JsonInclude(Include.NON_NULL)
    private Integer alipayStatus;

    @ApiModelProperty(value = "支付宝校验方式: RSA/MD5")
    @TableField("alipay_appid")
    @JsonInclude(Include.NON_NULL)
    private String alipayAppid;

    @ApiModelProperty(value = "支付宝校验方式: RSA/MD5")
    @TableField("alipay_check_type")
    @JsonInclude(Include.NON_NULL)
    private String alipayCheckType;

    @ApiModelProperty(value = "支付宝合作方ID")
    @TableField("alipay_partner_id")
    @JsonInclude(Include.NON_NULL)
    private String alipayPartnerId;

    @ApiModelProperty(value = "支付宝安全校验码")
    @TableField("alipay_valid_code")
    @JsonInclude(Include.NON_NULL)
    private String alipayValidCode;

    @ApiModelProperty(value = "支付宝公钥")
    @TableField("alipay_public_key")
    @JsonInclude(Include.NON_NULL)
    private String alipayPublicKey;

    @ApiModelProperty(value = "支付宝私钥")
    @TableField("alipay_private_key")
    @JsonInclude(Include.NON_NULL)
    private String alipayPrivateKey;

    @ApiModelProperty(value = "财付通状态：启用/关闭")
    @TableField("tenpay_status")
    @JsonInclude(Include.NON_NULL)
    private Integer tenpayStatus;

    @ApiModelProperty(value = "财付通应用ID")
    @TableField("tenpay_appid")
    @JsonInclude(Include.NON_NULL)
    private String tenpayAppid;

    @ApiModelProperty(value = "财付通密钥")
    @TableField("tenpay_private_key")
    @JsonInclude(Include.NON_NULL)
    private String tenpayPrivateKey;

    @ApiModelProperty(value = "财付通证书序列号")
    @TableField("tenpay_serial_number")
    @JsonInclude(Include.NON_NULL)
    private String tenpaySerialNumber;

    @ApiModelProperty(value = "财付通合作方id")
    @TableField("tenpay_merchant_id")
    @JsonInclude(Include.NON_NULL)
    private String tenpayMerchantId;

    @ApiModelProperty(value = "财付通安全校验码")
    @TableField("tenpay_valid_code")
    @JsonInclude(Include.NON_NULL)
    private String tenpayValidCode;

    @ApiModelProperty(value = "是否有效")
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
