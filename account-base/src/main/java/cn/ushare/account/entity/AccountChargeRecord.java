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
 * @date 2022-04-08
 * @email jixiang.li@ushareyun.net
 */
@Data
public class AccountChargeRecord extends Model<AccountChargeRecord> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "loginName")
    @TableField("login_name")
    @JsonInclude(Include.NON_NULL)
    private String loginName;

    @ApiModelProperty(value = "orderNum")
    @TableField("order_num")
    @JsonInclude(Include.NON_NULL)
    private String orderNum;

    @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    @JsonInclude(Include.NON_NULL)
    private Integer orderId;

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
