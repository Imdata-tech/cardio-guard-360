package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardioguard.entity.EcgLeadConfig;
import com.cardioguard.mapper.EcgLeadConfigMapper;
import com.cardioguard.service.EcgLeadConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ECG导联配置服务实现类
 */
@Service
public class EcgLeadConfigServiceImpl extends ServiceImpl<EcgLeadConfigMapper, EcgLeadConfig> implements EcgLeadConfigService {

    @Override
    public List<EcgLeadConfig> getAllLeadConfigs() {
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(EcgLeadConfig::getDisplayOrder);
        return this.list(wrapper);
    }

    @Override
    public EcgLeadConfig getLeadConfigByName(String leadName) {
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgLeadConfig::getLeadName, leadName);
        return this.getOne(wrapper);
    }

    @Override
    public List<EcgLeadConfig> getDefaultLeadConfigs() {
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgLeadConfig::getIsDefault, 1)
                .orderByAsc(EcgLeadConfig::getDisplayOrder);
        return this.list(wrapper);
    }

    @Override
    public EcgLeadConfig updateLeadConfig(EcgLeadConfig config) {
        this.updateById(config);
        return this.getById(config.getId());
    }
}
