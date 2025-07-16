package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("auth_template")
public class AuthTemplate extends Model<AuthTemplate> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 基础模板ID
     */
    @ApiModelProperty(value = "基础模板ID")
    @TableField("base_template_id")
    @JsonInclude(Include.NON_NULL)
    private Integer baseTemplateId;

    /**
     * 基础模板
     */
    @ApiModelProperty(value = "基础模板")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private AuthBaseTemplate baseTemplate;

    /**
     * 模板Url
     */
    @ApiModelProperty(value = "模板Url")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String templateUrl;

    /**
     * 手机模板Url
     */
    @ApiModelProperty(value = "手机模板Url")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String templateMobileUrl;

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
     * 欢迎语
     */
    @ApiModelProperty(value = "欢迎语")
    @JsonInclude(Include.NON_NULL)
    private String welcome;

    /**
     * LOGO
     */
    @ApiModelProperty(value = "LOGO")
    @TableField("logo_url")
    @JsonInclude(Include.NON_NULL)
    private String logoUrl;

    /**
     * 背景图
     */
    @ApiModelProperty(value = "背景图")
    @TableField("bg_image_url")
    @JsonInclude(Include.NON_NULL)
    private String bgImageUrl;

    @ApiModelProperty(value = "移动背景图")
    @TableField("mobile_bg_image_url")
    @JsonInclude(Include.NON_NULL)
    private String mobileBgImageUrl;

    /**
     * Banner图ID，逗号分隔
     */
    @ApiModelProperty(value = "Banner图ID，逗号分隔")
    @TableField("banner_image_ids")
    @JsonInclude(Include.NON_NULL)
    private String bannerImageIds;

    /**
     * Banner图列表
     */
    @ApiModelProperty(value = "Banner图列表")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private List<AdImage> bannerImageList;

    /**
     * 是否启用，0关闭，1启用
     */
    @ApiModelProperty(value = "是否启用，0关闭，1启用")
    @TableField("is_open")
    @JsonInclude(Include.NON_NULL)
    private Integer isOpen;

    /**
     * 终端类型，1PC，2手机
     */
    @ApiModelProperty(value = "终端类型，1PC，2手机")
    @TableField("terminal_type")
    @JsonInclude(Include.NON_NULL)
    private Integer terminalType;

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
