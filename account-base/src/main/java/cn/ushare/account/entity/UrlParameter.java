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
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class UrlParameter extends Model<UrlParameter> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    @TableField("basname")
    @JsonInclude(Include.NON_NULL)
    private String basName;

    /**
     * 用户IP
     */
    @ApiModelProperty(value = "用户IP")
    @TableField("userip")
    @JsonInclude(Include.NON_NULL)
    private String userIp;

    /**
     * 用户mac地址
     */
    @ApiModelProperty(value = "用户mac地址")
    @TableField("usermac")
    @JsonInclude(Include.NON_NULL)
    private String userMac;

    /**
     * 访问URL
     */
    @ApiModelProperty(value = "访问URL")
    @JsonInclude(Include.NON_NULL)
    private String url;

    /**
     * 设备IP
     */
    @ApiModelProperty(value = "设备IP")
    @TableField("basip")
    @JsonInclude(Include.NON_NULL)
    private String basIp;

    /**
     * 热点名称
     */
    @ApiModelProperty(value = "热点名称")
    @JsonInclude(Include.NON_NULL)
    private String ssid;

    /**
     * AP的MAC地址
     */
    @ApiModelProperty(value = "AP的MAC地址")
    @TableField("apmac")
    @JsonInclude(Include.NON_NULL)
    private String apMac;

    /**
     * AP的IP
     */
    @ApiModelProperty(value = "AP的IP")
    @TableField("apip")
    @JsonInclude(Include.NON_NULL)
    private String apIp;

    /**
     * AC的ID
     */
    @ApiModelProperty(value = "AC的ID")
    @TableField("ac_id")
    @JsonInclude(Include.NON_NULL)
    private Integer acId;

    /**
     * AC的名称
     */
    @ApiModelProperty(value = "AC的名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Integer acName;

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
