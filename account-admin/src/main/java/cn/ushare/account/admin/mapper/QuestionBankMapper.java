package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.QuestionBank;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2021-09-01
 * @email jixiang.li@ushareyun.net
 */
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    @Select("SELECT * FROM question_bank ${ew.customSqlSegment}")
    List<QuestionBank> getList(Page<QuestionBank> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT * FROM question_bank ${ew.customSqlSegment}")
    List<QuestionBank> getAuthData(@Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT * FROM question_bank ORDER BY rand() LIMIT #{count}")
    List<QuestionBank> getAuthData2(Integer count);
}
