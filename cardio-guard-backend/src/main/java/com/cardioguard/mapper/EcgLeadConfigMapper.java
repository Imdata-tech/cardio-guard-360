package com.cardioguard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardioguard.entity.EcgLeadConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * ECG导联配置Mapper接口
 */
@Mapper
public interface EcgLeadConfigMapper extends BaseMapper<EcgLeadConfig> {
    // 使用MyBatis Plus标准方法
}
