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
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class Ac extends Model<Ac> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String name;

    @ApiModelProperty(value = "Proxy")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String proxy = "";

    @ApiModelProperty(value = "品牌ID")
    @TableField("brand_id")
    @JsonInclude(Include.NON_NULL)
    private Integer brandId;

    @ApiModelProperty(value = "品牌名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String brandName;

    @ApiModelProperty(value = "品牌编码")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String brandCode;

    @ApiModelProperty(value = "品牌")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private DeviceBrand brand;

    @ApiModelProperty(value = "型号ID")
    @TableField("model_id")
    @JsonInclude(Include.NON_NULL)
    private Integer modelId;

    @ApiModelProperty(value = "型号名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String modelName;

    @ApiModelProperty(value = "软件版本Id")
    @TableField("software_version_id")
    @JsonInclude(Include.NON_NULL)
    private Integer softwareVersionId;

    @ApiModelProperty(value = "软件版本")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String softwareVersion;

    @ApiModelProperty(value = "Portal协议版本")
    @TableField("portal_version")
    @JsonInclude(Include.NON_NULL)
    private String portalVersion;

    @ApiModelProperty(value = "共享密钥")
    @TableField("share_key")
    @JsonInclude(Include.NON_NULL)
    private String shareKey;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    @JsonInclude(Include.NON_NULL)
    private String ip;

    @ApiModelProperty(value = "端口")
    @JsonInclude(Include.NON_NULL)
    private Integer port;

    @ApiModelProperty(value = "认证类型，0 chap，1 pap")
    @TableField("auth_type")
    @JsonInclude(Include.NON_NULL)
    private Integer authType;

    @ApiModelProperty(value = "认证模板ID")
    @TableField("auth_template_id")
    @JsonInclude(Include.NON_NULL)
    private Integer authTemplateId;

    @ApiModelProperty(value = "认证模板名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String authTemplateName;

    @ApiModelProperty(value = "认证模板")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private AuthTemplate authTemplate;

    @ApiModelProperty(value = "认证方式，列表，逗号分隔")
    @TableField("auth_method")
    @JsonInclude(Include.NON_NULL)
    private String authMethod;

    @ApiModelProperty(value = "微信门店配置ID")
    @TableField("wx_shop_config_id")
    @JsonInclude(Include.NON_NULL)
    private Integer wxShopConfigId;

    @ApiModelProperty(value = "微信门店配置")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String wxShopConfig;

    @ApiModelProperty(value = "钉钉认证配置ID")
    @TableField("ding_talk_config_id")
    @JsonInclude(Include.NON_NULL)
    private Integer dingTalkConfigId;

    @ApiModelProperty(value = "超时时间，单位秒")
    @TableField("expire_time")
    @JsonInclude(Include.NON_NULL)
    private Integer expireTime;

    @ApiModelProperty(value = "是否允许电脑登录，0拒绝，1允许")
    @TableField("is_pc_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isPcEnable;

    @ApiModelProperty(value = "是否使用白名单")
    @TableField("is_whitelist_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isWhitelistEnable;

    @ApiModelProperty(value = "Portal地址")
    @TableField("portal_url")
    @JsonInclude(Include.NON_NULL)
    private String portalUrl;

    @ApiModelProperty(value = "认证成功回调地址，Ruckus专用")
    @TableField("auth_success_callback")
    @JsonInclude(Include.NON_NULL)
    private String authSuccessCallback;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "是否有线认证，0不是(默认)，1是")
    @TableField("is_wired")
    @JsonInclude(Include.NON_NULL)
    private Integer isWired;

    @ApiModelProperty(value = "nas IP, radius服务器ip")
    @TableField("nas_ip")
    @JsonInclude(Include.NON_NULL)
    private String nasIp;

    @ApiModelProperty(value = "nas IP, radius服务器ip")
    @TableField("nas_ip_bak")
    @JsonInclude(Include.NON_NULL)
    private String nasIpBak;

    @ApiModelProperty(value = "是否云端部署")
    @TableField("is_remote")
    @JsonInclude(Include.NON_NULL)
    private Integer isRemote;

    @ApiModelProperty(value = "是否仅首次发送：1是，0不是(默认)")
    @TableField("send_once")
    @JsonInclude(Include.NON_NULL)
    private Integer sendOnce;

    @ApiModelProperty(value = "是否开启行为管理：1是，0不是(默认)")
    @TableField("action_manage")
    @JsonInclude(Include.NON_NULL)
    private Integer actionManage;

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
