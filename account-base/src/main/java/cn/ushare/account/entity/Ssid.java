package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Ssid extends Model<Ssid> {

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
     * 所属控制器ID
     */
    @ApiModelProperty(value = "所属控制器ID")
    @TableField("ac_id")
    @JsonInclude(Include.NON_NULL)
    private Integer acId;

    /**
     * 控制器名称
     */
    @ApiModelProperty(value = "控制器名称")
    @TableField("ac_name")
    @JsonInclude(Include.NON_NULL)
    private String acName;

    /**
     * 品牌编码
     */
    @ApiModelProperty(value = "品牌编码")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String brandCode;

    /**
     * 所属apId
     */
    @ApiModelProperty(value = "所属apId")
    @TableField("ap_id")
    @JsonInclude(Include.NON_NULL)
    private Integer apId;

    /**
     * 所属部门Id
     */
    @ApiModelProperty(value = "所属部门Id")
    @TableField("department_id")
    @JsonInclude(Include.NON_NULL)
    private Integer departmentId;

    /**
     * 所属部门名称
     */
    @ApiModelProperty(value = "所属部门名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String departmentName;

    /**
     * 用户数
     */
    @ApiModelProperty(value = "用户数")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Long userNum;

    /**
     * AP数
     */
    @ApiModelProperty(value = "AP数")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private Long apNum;

    /**
     * 有效吞吐量，单位kbps
     */
    @ApiModelProperty(value = "有效吞吐量，单位kbps")
    @TableField("valid_throughput")
    @JsonInclude(Include.NON_NULL)
    private String validThroughput;

    /**
     * 帧数
     */
    @ApiModelProperty(value = "帧数")
    @TableField("frame_num")
    @JsonInclude(Include.NON_NULL)
    private String frameNum;

    /**
     * 下行重传率，百分比
     */
    @ApiModelProperty(value = "下行重传率，百分比")
    @TableField("down_retrans_rate")
    @JsonInclude(Include.NON_NULL)
    private Integer downRetransRate;

    /**
     * 下行丢包率，百分比
     */
    @ApiModelProperty(value = "下行丢包率，百分比")
    @TableField("down_packet_loss_rate")
    @JsonInclude(Include.NON_NULL)
    private Integer downPacketLossRate;

    /**
     * 是否有效，0无效，1有效
     */
    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    /**
     * 认证模板ID
     */
    @ApiModelProperty(value = "认证模板ID")
    @TableField("auth_template_id")
    @JsonInclude(Include.NON_NULL)
    private Integer authTemplateId;

    /**
     * 认证模板名称
     */
    @ApiModelProperty(value = "认证模板名称")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private String authTemplateName;

    /**
     * 认证模板
     */
    @ApiModelProperty(value = "认证模板")
    @TableField(exist = false)
    @JsonInclude(Include.NON_NULL)
    private AuthTemplate authTemplate;

    /**
     * 认证方式，列表，逗号分隔
     */
    @ApiModelProperty(value = "认证方式，列表，逗号分隔")
    @TableField("auth_method")
    @JsonInclude(Include.NON_NULL)
    private String authMethod;

    @ApiModelProperty(value = "无感知在员工表查询")
    @TableField("is_employee")
    @JsonInclude(Include.NON_NULL)
    private Integer isEmployee;

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
