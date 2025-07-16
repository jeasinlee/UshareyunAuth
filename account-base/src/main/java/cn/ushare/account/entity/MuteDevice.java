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
 * @date 2021-12-20
 * @email jixiang.li@ushareyun.net
 */
@Data
public class MuteDevice extends Model<MuteDevice> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "设备名")
    @TableField("device_name")
    @JsonInclude(Include.NON_NULL)
    private String deviceName;

    @ApiModelProperty(value = "描述")
    @JsonInclude(Include.NON_NULL)
    private String description;

    @ApiModelProperty(value = "绑定目的：0免认证，1限制登录")
    @TableField("bind_purpose")
    @JsonInclude(Include.NON_NULL)
    private Integer bindPurpose;

    @ApiModelProperty(value = "绑定类型：0mac，1ip")
    @TableField("bind_type")
    @JsonInclude(Include.NON_NULL)
    private Integer bindType;

    @ApiModelProperty(value = "绑定mac")
    @TableField("bind_mac")
    @JsonInclude(Include.NON_NULL)
    private String bindMac;

    @ApiModelProperty(value = "绑定ip")
    @TableField("bind_ip")
    @JsonInclude(Include.NON_NULL)
    private String bindIp;

    @ApiModelProperty(value = "是否永久有效")
    @TableField("is_always")
    @JsonInclude(Include.NON_NULL)
    private Integer isAlways;

    @ApiModelProperty(value = "截止时间")
    @TableField("range_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date rangeTime;

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
