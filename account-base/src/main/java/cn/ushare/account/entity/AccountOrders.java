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
 * @date 2022-04-02
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountOrders extends Model<AccountOrders> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "产品名称")
    @TableField("product_name")
    @JsonInclude(Include.NON_NULL)
    private String productName;

    @ApiModelProperty(value = "策略id")
    @TableField("policy_id")
    @JsonInclude(Include.NON_NULL)
    private Integer policyId;

    @ApiModelProperty(value = "充值方id")
    @TableField("from_user_id")
    @JsonInclude(Include.NON_NULL)
    private Integer fromUserId;

    @ApiModelProperty(value = "充值方账号")
    @TableField("from_login_name")
    @JsonInclude(Include.NON_NULL)
    private String fromLoginName;

    @ApiModelProperty(value = "受让方id")
    @TableField("to_user_id")
    @JsonInclude(Include.NON_NULL)
    private Integer toUserId;

    @ApiModelProperty(value = "受让方账号")
    @TableField("to_login_name")
    @JsonInclude(Include.NON_NULL)
    private String toLoginName;

    @ApiModelProperty(value = "订单编号")
    @TableField("order_num")
    @JsonInclude(Include.NON_NULL)
    private String orderNum;

    @ApiModelProperty(value = "平台订单编号")
    @TableField("platform_order_num")
    @JsonInclude(Include.NON_NULL)
    private String platformOrderNum;

    @ApiModelProperty(value = "订单支付状态:0未支付，1已支付，2已关闭")
    @TableField("order_status")
    @JsonInclude(Include.NON_NULL)
    private Integer orderStatus;

    @ApiModelProperty(value = "订单充值状态:0未充值，1已充值")
    @TableField("charge_status")
    @JsonInclude(Include.NON_NULL)
    private Integer chargeStatus;

    @ApiModelProperty(value = "订单金额")
    @TableField("total_fee")
    @JsonInclude(Include.NON_NULL)
    private Integer totalFee;

    @ApiModelProperty(value = "支付时间")
    @TableField(value="pay_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date payTime;

    @ApiModelProperty(value = "充值时间")
    @TableField(value="charge_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date chargeTime;

    @ApiModelProperty(value = "充值方式：0微信，1支付宝")
    @TableField("pay_type")
    @JsonInclude(Include.NON_NULL)
    private Integer payType;

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