package com.cardioguard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardioguard.entity.EcgAnnotation;
import org.apache.ibatis.annotations.Mapper;

/**
 * ECG标注Mapper接口
 */
@Mapper
public interface EcgAnnotationMapper extends BaseMapper<EcgAnnotation> {
    // 使用MyBatis Plus标准方法,无需自定义SQL
}
