package com.cardioguard.service;

import com.cardioguard.entity.EcgLeadConfig;

import java.util.List;

/**
 * ECG导联配置服务接口
 */
public interface EcgLeadConfigService {
    
    /**
     * 获取所有导联配置
     * 
     * @return 导联配置列表
     */
    List<EcgLeadConfig> getAllLeadConfigs();
    
    /**
     * 根据导联名称获取配置
     * 
     * @param leadName 导联名称
     * @return 导联配置
     */
    EcgLeadConfig getLeadConfigByName(String leadName);
    
    /**
     * 获取默认显示的导联配置
     * 
     * @return 默认导联配置列表
     */
    List<EcgLeadConfig> getDefaultLeadConfigs();
    
    /**
     * 更新导联配置
     * 
     * @param config 导联配置
     * @return 更新后的配置
     */
    EcgLeadConfig updateLeadConfig(EcgLeadConfig config);
}
