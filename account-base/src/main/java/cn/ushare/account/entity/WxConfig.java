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
@TableName("wx_config")
public class WxConfig extends Model<WxConfig> {

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
     * 门店ID
     */
    @ApiModelProperty(value = "门店ID")
    @TableField("shop_id")
    @JsonInclude(Include.NON_NULL)
    private String shopId;

    /**
     * AppId
     */
    @ApiModelProperty(value = "AppId")
    @TableField("app_id")
    @JsonInclude(Include.NON_NULL)
    private String appId;

    /**
     * AppSecret
     */
    @ApiModelProperty(value = "AppSecret")
    @TableField("app_secret")
    @JsonInclude(Include.NON_NULL)
    private String appSecret;

    /**
     * 临时放行时长，单位秒
     */
    @ApiModelProperty(value = "临时放行时长，单位秒")
    @TableField("temp_pass_time")
    @JsonInclude(Include.NON_NULL)
    private Integer tempPassTime;

    /**
     * SSID
     */
    @ApiModelProperty(value = "SSID")
    @JsonInclude(Include.NON_NULL)
    private String ssid;

    @ApiModelProperty(value = "SSID")
    @JsonInclude(Include.NON_NULL)
    private String keyword;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "描述")
    @JsonInclude(Include.NON_NULL)
    private String description;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("use_mini")
    @JsonInclude(Include.NON_NULL)
    private Integer useMini;

    @ApiModelProperty(value = "小程序首页地址")
    @TableField("mini_url")
    @JsonInclude(Include.NON_NULL)
    private String miniUrl;

    @ApiModelProperty(value = "图文地址")
    @TableField("article_url")
    @JsonInclude(Include.NON_NULL)
    private String articleUrl;

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
