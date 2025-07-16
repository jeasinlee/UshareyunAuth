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

@Data
public class AccountChargePolicy extends Model<AccountChargePolicy> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "策略名称")
    @TableField("policy_name")
    @JsonInclude(Include.NON_NULL)
    private String policyName;

    @ApiModelProperty(value = "优先级")
    @JsonInclude(Include.NON_NULL)
    private Integer level;

    @ApiModelProperty(value = "价格")
    @TableField("total_fee")
    @JsonInclude(Include.NON_NULL)
    private Integer totalFee;

    @ApiModelProperty(value = "时长数量")
    @TableField("total_num")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNum;

    @ApiModelProperty(value = "时长单位：0天，1月,2年，3小时")
    @TableField("unit")
    @JsonInclude(Include.NON_NULL)
    private Integer unit;

    @ApiModelProperty(value = "总流量")
    @TableField("total_flow")
    @JsonInclude(Include.NON_NULL)
    private Integer totalFlow;

    @ApiModelProperty(value = "带宽ID")
    @TableField("band_id")
    @JsonInclude(Include.NON_NULL)
    private Integer bandId;

    @ApiModelProperty(value = "带宽名称")
    @TableField("band_name")
    @JsonInclude(Include.NON_NULL)
    private String bandName;

    @ApiModelProperty(value = "绑定MAC数量")
    @TableField("bind_mac_num")
    @JsonInclude(Include.NON_NULL)
    private Integer bindMacNum;

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
