package com.cardioguard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardioguard.entity.AnnotationTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标注模板Mapper接口
 */
@Mapper
public interface AnnotationTemplateMapper extends BaseMapper<AnnotationTemplate> {
    // 使用MyBatis Plus标准方法,无需自定义SQL
}
