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
 * @date 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("online_policy")
public class OnlinePolicy extends Model<OnlinePolicy> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 带宽ID
     */
    @ApiModelProperty(value = "带宽ID")
    @TableField("bandwidth_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandwidthId;

    /**
     * 上网时长，单位分钟
     */
    @ApiModelProperty(value = "上网时长，单位分钟")
    @TableField("online_period")
    @JsonInclude(Include.NON_NULL)
    private Integer onlinePeriod;

    /**
     * 是否限制上网时长，0否，1是
     */
    @ApiModelProperty(value = "是否限制上网时长")
    @TableField("is_period_limit")
    @JsonInclude(Include.NON_NULL)
    private Integer isPeriodLimit;

    /**
     * 周一开始时间
     */
    @ApiModelProperty(value = "周一开始时间")
    @TableField("day_1_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day1StartTime;

    /**
     * 周一结束时间
     */
    @ApiModelProperty(value = "周一结束时间")
    @TableField("day_1_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day1EndTime;

    /**
     * 周二开始时间
     */
    @ApiModelProperty(value = "周二开始时间")
    @TableField("day_2_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day2StartTime;

    /**
     * 周二结束时间
     */
    @ApiModelProperty(value = "周二结束时间")
    @TableField("day_2_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day2EndTime;

    /**
     * 周三开始时间
     */
    @ApiModelProperty(value = "周三开始时间")
    @TableField("day_3_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day3StartTime;

    /**
     * 周三结束时间
     */
    @ApiModelProperty(value = "周三结束时间")
    @TableField("day_3_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day3EndTime;

    /**
     * 周四开始时间
     */
    @ApiModelProperty(value = "周四开始时间")
    @TableField("day_4_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day4StartTime;

    /**
     * 周四结束时间
     */
    @ApiModelProperty(value = "周四结束时间")
    @TableField("day_4_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day4EndTime;

    /**
     * 周五开始时间
     */
    @ApiModelProperty(value = "周五开始时间")
    @TableField("day_5_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day5StartTime;

    /**
     * 周五结束时间
     */
    @ApiModelProperty(value = "周五结束时间")
    @TableField("day_5_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day5EndTime;

    /**
     * 周六开始时间
     */
    @ApiModelProperty(value = "周六开始时间")
    @TableField("day_6_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day6StartTime;

    /**
     * 周六结束时间
     */
    @ApiModelProperty(value = "周六结束时间")
    @TableField("day_6_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day6EndTime;

    /**
     * 周日开始时间
     */
    @ApiModelProperty(value = "周日开始时间")
    @TableField("day_7_start_time")
    @JsonInclude(Include.NON_NULL)
    private String day7StartTime;

    /**
     * 周日结束时间
     */
    @ApiModelProperty(value = "周日结束时间")
    @TableField("day_7_end_time")
    @JsonInclude(Include.NON_NULL)
    private String day7EndTime;

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
