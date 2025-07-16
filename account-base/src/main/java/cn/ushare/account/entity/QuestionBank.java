package cn.ushare.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author jixiang.li
 * @date 2021-09-01
 * @email jixiang.li@ushareyun.net
 */
@Data
@TableName("question_bank")
public class QuestionBank extends Model<QuestionBank> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="id", type= IdType.INPUT)
    @JsonInclude(Include.NON_NULL)
    private Integer id;

    @ApiModelProperty(value = "问题")
    @JsonInclude(Include.NON_NULL)
    private String question;

    @ApiModelProperty(value = "选项")
    @JsonInclude(Include.NON_NULL)
    private String options;

    @ApiModelProperty(value = "答案")
    @JsonInclude(Include.NON_NULL)
    private String answer;

    @ApiModelProperty(value = "是否有效，0无效，1有效")
    @TableField("is_valid")
    @JsonInclude(Include.NON_NULL)
    private Integer isValid;

    @ApiModelProperty(value = "创建时间")
    @TableField(value="create_time", fill= FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value="update_time", fill= FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonInclude(Include.NON_NULL)
    private Date updateTime;

}
