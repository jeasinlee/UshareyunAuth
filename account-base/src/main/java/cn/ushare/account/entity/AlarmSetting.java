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
 * @date 2019-03-30
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("alarm_setting")
public class AlarmSetting extends Model<AlarmSetting> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 警报类型，1CPU，2内存，3硬盘，4认证数
     */
    @ApiModelProperty(value = "警报类型，1CPU，2内存，3硬盘，4认证数")
    @JsonInclude(Include.NON_NULL)
    private Integer type;

    /**
     * 是否自定义，0否，1是
     */
    @ApiModelProperty(value = "是否自定义，0否，1是")
    @TableField("is_custom")
    @JsonInclude(Include.NON_NULL)
    private Integer isCustom;

    /**
     * 阈值
     */
    @ApiModelProperty(value = "阈值")
    @JsonInclude(Include.NON_NULL)
    private String threshold;

    /**
     * 是否打开，0关闭，1打开
     */
    @ApiModelProperty(value = "是否打开，0关闭，1打开")
    @JsonInclude(Include.NON_NULL)
    private Integer status;

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
