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
@TableName("auth_qrcode")
public class AuthQrcode extends Model<AuthQrcode> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * AC控制器ID
     */
    @ApiModelProperty(value = "AC控制器ID")
    @TableField("ac_id")
    @JsonInclude(Include.NON_NULL)
    private Integer acId;

    /**
     * AC控制器名称
     */
    @ApiModelProperty(value = "AC控制器名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String acName;

    /**
     * 是否默认
     */
    @ApiModelProperty(value = "是否默认")
    @TableField("is_default")
    @JsonInclude(Include.NON_NULL)
    private Integer isDefault;

    /**
     * 是否默认
     */
    @ApiModelProperty(value = "是否默认")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Boolean isDefaultChecked;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date endTime;

    /**
     * 带宽策略ID
     */
    @ApiModelProperty(value = "带宽策略ID")
    @TableField("bandwidth_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandwidthId;

    /**
     * 带宽策略名称
     */
    @ApiModelProperty(value = "带宽策略名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String bandwidthName;

    /**
     * 序列号
     */
    @ApiModelProperty(value = "序列号")
    @JsonInclude(Include.NON_NULL)
    private String sn;

    /**
     * URL
     */
    @ApiModelProperty(value = "URL")
    @JsonInclude(Include.NON_NULL)
    private String url;

    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    @TableField("image_url")
    @JsonInclude(Include.NON_NULL)
    private String imageUrl;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @JsonInclude(Include.NON_NULL)
    private String remark;

    /**
     * 是否有效，0无效，1有效
     */
    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "补充用户信息，0不需要，1需要")
    @TableField("supply_user_info")
    @JsonInclude(Include.NON_NULL)
    private Integer supplyUserInfo;

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
