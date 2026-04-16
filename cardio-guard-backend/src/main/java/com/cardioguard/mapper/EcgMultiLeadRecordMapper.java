package com.cardioguard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardioguard.entity.EcgMultiLeadRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 多导联ECG记录Mapper接口
 */
@Mapper
public interface EcgMultiLeadRecordMapper extends BaseMapper<EcgMultiLeadRecord> {
    // 使用MyBatis Plus标准方法,无需自定义SQL
}
