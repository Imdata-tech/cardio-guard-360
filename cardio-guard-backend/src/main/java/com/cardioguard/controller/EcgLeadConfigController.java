package com.cardioguard.controller;

import com.cardioguard.common.Result;
import com.cardioguard.entity.EcgLeadConfig;
import com.cardioguard.service.EcgLeadConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ECG导联配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ecg/lead-config")
@Api(tags = "ECG导联配置管理")
public class EcgLeadConfigController {
    
    @Autowired
    private EcgLeadConfigService leadConfigService;
    
    @GetMapping
    @ApiOperation("获取所有导联配置")
    public Result<List<EcgLeadConfig>> getAllLeadConfigs() {
        try {
            List<EcgLeadConfig> configs = leadConfigService.getAllLeadConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取导联配置失败", e);
            return Result.error("获取导联配置失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{leadName}")
    @ApiOperation("根据导联名称获取配置")
    public Result<EcgLeadConfig> getLeadConfigByName(@PathVariable String leadName) {
        try {
            EcgLeadConfig config = leadConfigService.getLeadConfigByName(leadName);
            if (config != null) {
                return Result.success(config);
            } else {
                return Result.error("导联配置不存在");
            }
        } catch (Exception e) {
            log.error("获取导联配置失败", e);
            return Result.error("获取导联配置失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/default")
    @ApiOperation("获取默认导联配置")
    public Result<List<EcgLeadConfig>> getDefaultLeadConfigs() {
        try {
            List<EcgLeadConfig> configs = leadConfigService.getDefaultLeadConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取默认导联配置失败", e);
            return Result.error("获取默认导联配置失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @ApiOperation("更新导联配置")
    public Result<EcgLeadConfig> updateLeadConfig(
            @PathVariable Long id,
            @RequestBody EcgLeadConfig config) {
        try {
            config.setId(id);
            EcgLeadConfig result = leadConfigService.updateLeadConfig(config);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新导联配置失败", e);
            return Result.error("更新导联配置失败: " + e.getMessage());
        }
    }
}
