package com.cardioguard.config;

import com.cardioguard.entity.EcgLeadConfig;
import com.cardioguard.service.EcgLeadConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热组件
 * 应用启动时自动预热热点数据到Redis
 */
@Slf4j
@Component
public class CacheWarmer implements ApplicationListener<ApplicationReadyEvent> {
    
    @Autowired
    private EcgLeadConfigService leadConfigService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "ecg:lead:config:";
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("========== 开始缓存预热 ==========");
        
        try {
            // 1. 预热所有导联配置
            List<EcgLeadConfig> allConfigs = leadConfigService.getAllLeadConfigs();
            if (allConfigs != null && !allConfigs.isEmpty()) {
                redisTemplate.opsForValue().set(
                    CACHE_KEY_PREFIX + "all", 
                    allConfigs, 
                    24, 
                    TimeUnit.HOURS
                );
                log.info("✅ 已预热所有导联配置，共{}条", allConfigs.size());
            }
            
            // 2. 预热默认导联配置
            List<EcgLeadConfig> defaultConfigs = leadConfigService.getDefaultLeadConfigs();
            if (defaultConfigs != null && !defaultConfigs.isEmpty()) {
                redisTemplate.opsForValue().set(
                    CACHE_KEY_PREFIX + "default", 
                    defaultConfigs, 
                    24, 
                    TimeUnit.HOURS
                );
                log.info("✅ 已预热默认导联配置，共{}条", defaultConfigs.size());
            }
            
            // 3. 预热每个导联的单独配置
            for (EcgLeadConfig config : allConfigs) {
                String key = CACHE_KEY_PREFIX + "name:" + config.getLeadName();
                redisTemplate.opsForValue().set(key, config, 24, TimeUnit.HOURS);
            }
            log.info("✅ 已预热{}个单独导联配置", allConfigs.size());
            
            log.info("========== 缓存预热完成 ==========");
            
        } catch (Exception e) {
            log.error("❌ 缓存预热失败", e);
        }
    }
}
