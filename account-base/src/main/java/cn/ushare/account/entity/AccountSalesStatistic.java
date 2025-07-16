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
 * @date 2022-04-28
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountSalesStatistic extends Model<AccountSalesStatistic> {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "日期字符串")
    @TableField("day_str")
    @JsonInclude(Include.NON_NULL)
    private String dayStr;

    @ApiModelProperty(value = "订单总金额数")
    @TableField("total_amount")
    @JsonInclude(Include.NON_NULL)
    private Integer totalAmount;

    @ApiModelProperty(value = "总订单数")
    @TableField("total_num")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNum;

    @ApiModelProperty(value = "总成功订单数")
    @TableField("total_num_success")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNumSuccess;

    @ApiModelProperty(value = "总微信订单数")
    @TableField("total_num_weixin")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNumWeixin;

    @ApiModelProperty(value = "总淘宝订单数")
    @TableField("total_num_ali")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNumAli;

    @ApiModelProperty(value = "总退款订单数")
    @TableField("total_num_refund")
    @JsonInclude(Include.NON_NULL)
    private Integer totalNumRefund;

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
