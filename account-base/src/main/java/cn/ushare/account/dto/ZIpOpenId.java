package cn.ushare.account.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * 【请填写功能名称】对象 z_ip_open_id
 *
 * @author ruoyi
 * @date 2024-05-23
 */
@Data

public class ZIpOpenId {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */

    private Long id;
    /**
     * 
     */
    private String ip;
    /**
     * 
     */
    private String openId;


    private String createTime;


    private String updateTime;


}
