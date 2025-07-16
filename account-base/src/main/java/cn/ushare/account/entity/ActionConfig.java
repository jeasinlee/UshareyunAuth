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
 * @date 2020-01-14
 * @email jixiang.li@ushareyun.net
 */
@Data
public class ActionConfig extends Model<ActionConfig> {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    /**
     * 厂商名称
     */
    @ApiModelProperty(value = "厂商名称")
    @TableField("merchant_name")
    @JsonInclude(Include.NON_NULL)
    private String merchantName;

    /**
     * 厂商编码
     */
    @ApiModelProperty(value = "厂商编码")
    @TableField("merchant_code")
    @JsonInclude(Include.NON_NULL)
    private String merchantCode;

    /**
     * 行为管理IP
     */
    @ApiModelProperty(value = "行为管理IP")
    @TableField("action_ip")
    @JsonInclude(Include.NON_NULL)
    private String actionIp = "";

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    @TableField("port")
    @JsonInclude(Include.NON_NULL)
    private String port = "";

    /**
     * 当前生效ID
     */
    @ApiModelProperty(value = "当前生效ID")
    @TableField("is_cur")
    @JsonInclude(Include.NON_NULL)
    private Integer isCur;

    /**
     * 是否显示
     */
    @ApiModelProperty(value = "是否显示")
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
