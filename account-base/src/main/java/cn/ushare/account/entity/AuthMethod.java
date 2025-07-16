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
 * @date 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("auth_method")
public class AuthMethod extends Model<AuthMethod> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    @JsonInclude(Include.NON_NULL)
    private String name;

    /**
     * 是否启用，0关闭，1打开
     */
    @ApiModelProperty(value = "是否启用，0关闭，1打开")
    @TableField("is_enable")
    @JsonInclude(Include.NON_NULL)
    private Integer isEnable;

    /**
     * 是否使用自定义上网策略
     */
    @ApiModelProperty(value = "是否使用自定义上网策略")
    @TableField("use_custom_policy")
    @JsonInclude(Include.NON_NULL)
    private Integer useCustomPolicy;

    /**
     * 自定义上网策略ID
     */
    @ApiModelProperty(value = "自定义上网策略ID")
    @TableField("custom_policy_id")
    @JsonInclude(Include.NON_NULL)
    private Integer customPolicyId;

    /**
     * 自定义上网策略
     */
    @ApiModelProperty(value = "自定义上网策略")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private OnlinePolicy customPolicy;


    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    @JsonInclude(Include.NON_NULL)
    private Integer sort;

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
