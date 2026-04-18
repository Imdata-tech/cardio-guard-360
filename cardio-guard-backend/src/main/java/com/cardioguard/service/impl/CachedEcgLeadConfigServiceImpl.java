package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cardioguard.entity.EcgLeadConfig;
import com.cardioguard.mapper.EcgLeadConfigMapper;
import com.cardioguard.service.EcgLeadConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 带缓存的ECG导联配置服务实现
 */
@Slf4j
@Service
public class CachedEcgLeadConfigServiceImpl implements EcgLeadConfigService {
    
    @Autowired
    private EcgLeadConfigMapper leadConfigMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "ecg:lead:config:";
    private static final long CACHE_TTL_SECONDS = 24 * 3600; // 24小时
    
    @Override
    public List<EcgLeadConfig> getAllLeadConfigs() {
        String cacheKey = CACHE_KEY_PREFIX + "all";
        
        // 1. 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<EcgLeadConfig> cached = (List<EcgLeadConfig>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("缓存命中: {}", cacheKey);
            return cached;
        }
        
        log.debug("缓存未命中，查询数据库: {}", cacheKey);
        
        // 2. 缓存未命中，查询数据库
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(EcgLeadConfig::getDisplayOrder);
        List<EcgLeadConfig> configs = leadConfigMapper.selectList(wrapper);
        
        // 3. 写入缓存
        if (configs != null && !configs.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, configs, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.info("缓存已更新: {}, 数据量: {}", cacheKey, configs.size());
        }
        
        return configs;
    }
    
    @Override
    public EcgLeadConfig getLeadConfigByName(String leadName) {
        String cacheKey = CACHE_KEY_PREFIX + "name:" + leadName;
        
        // 1. 尝试从缓存获取
        EcgLeadConfig cached = (EcgLeadConfig) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("缓存命中: {}", cacheKey);
            return cached;
        }
        
        log.debug("缓存未命中，查询数据库: {}", cacheKey);
        
        // 2. 查询数据库
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgLeadConfig::getLeadName, leadName);
        EcgLeadConfig config = leadConfigMapper.selectOne(wrapper);
        
        // 3. 写入缓存
        if (config != null) {
            redisTemplate.opsForValue().set(cacheKey, config, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.info("缓存已更新: {}", cacheKey);
        }
        
        return config;
    }
    
    @Override
    public List<EcgLeadConfig> getDefaultLeadConfigs() {
        String cacheKey = CACHE_KEY_PREFIX + "default";
        
        // 1. 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<EcgLeadConfig> cached = (List<EcgLeadConfig>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("缓存命中: {}", cacheKey);
            return cached;
        }
        
        log.debug("缓存未命中，查询数据库: {}", cacheKey);
        
        // 2. 查询数据库
        LambdaQueryWrapper<EcgLeadConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgLeadConfig::getIsDefault, 1)
               .orderByAsc(EcgLeadConfig::getDisplayOrder);
        List<EcgLeadConfig> configs = leadConfigMapper.selectList(wrapper);
        
        // 3. 写入缓存
        if (configs != null && !configs.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, configs, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.info("缓存已更新: {}, 数据量: {}", cacheKey, configs.size());
        }
        
        return configs;
    }
    
    @Override
    public EcgLeadConfig updateLeadConfig(EcgLeadConfig config) {
        // 1. 更新数据库
        int rows = leadConfigMapper.updateById(config);
        if (rows == 0) {
            throw new RuntimeException("导联配置不存在或更新失败");
        }
        
        // 2. 清除相关缓存
        invalidateCache();
        log.info("导联配置已更新，缓存已失效: id={}", config.getId());
        
        // 3. 返回最新数据
        return leadConfigMapper.selectById(config.getId());
    }
    
    /**
     * 清除所有导联配置缓存
     */
    private void invalidateCache() {
        try {
            // 使用keys模式匹配删除（生产环境建议使用scan）
            String pattern = CACHE_KEY_PREFIX + "*";
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("已清除{}个导联配置缓存键", deletedCount);
            }
        } catch (Exception e) {
            log.error("清除缓存失败", e);
        }
    }
}
