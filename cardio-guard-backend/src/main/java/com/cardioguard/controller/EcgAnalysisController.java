package com.cardioguard.controller;

import com.cardioguard.common.Result;
import com.cardioguard.entity.EcgAnalysisResult;
import com.cardioguard.service.EcgAnalysisService;
import com.cardioguard.service.EcgSimulationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECG心电图分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ecg")
@Api(tags = "ECG心电图分析")
public class EcgAnalysisController {
    
    @Autowired
    private EcgAnalysisService ecgAnalysisService;
    
    @Autowired
    private EcgSimulationService ecgSimulationService;
    
    @PostMapping("/analyze")
    @ApiOperation("分析ECG数据")
    public Result<EcgAnalysisResult> analyzeEcg(
            @RequestParam Long userId,
            @RequestParam Long deviceId,
            @RequestBody Map<String, Object> requestData) {
        try {
            String waveform = (String) requestData.get("waveform");
            Integer sampleRate = (Integer) requestData.get("sampleRate");
            
            if (waveform == null || waveform.isEmpty()) {
                return Result.error("波形数据不能为空");
            }
            
            EcgAnalysisResult result = ecgAnalysisService.analyzeEcg(userId, deviceId, waveform, sampleRate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("ECG分析失败", e);
            return Result.error("ECG分析失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch-analyze")
    @ApiOperation("批量分析ECG数据")
    public Result<List<EcgAnalysisResult>> batchAnalyze(@RequestBody List<Map<String, Object>> ecgDataList) {
        try {
            List<EcgAnalysisResult> results = ecgAnalysisService.batchAnalyze(ecgDataList);
            return Result.success(results);
        } catch (Exception e) {
            log.error("批量ECG分析失败", e);
            return Result.error("批量ECG分析失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/history")
    @ApiOperation("获取用户ECG分析历史")
    public Result<List<EcgAnalysisResult>> getHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<EcgAnalysisResult> results = ecgAnalysisService.getUserAnalysisHistory(userId, page, size);
            return Result.success(results);
        } catch (Exception e) {
            log.error("获取ECG分析历史失败", e);
            return Result.error("获取ECG分析历史失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/result/{resultId}")
    @ApiOperation("获取分析结果详情")
    public Result<EcgAnalysisResult> getResult(@PathVariable Long resultId) {
        try {
            EcgAnalysisResult result = ecgAnalysisService.getAnalysisResult(resultId);
            if (result == null) {
                return Result.error("分析结果不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分析结果失败", e);
            return Result.error("获取分析结果失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/review/{resultId}")
    @ApiOperation("医生审核分析结果")
    public Result<EcgAnalysisResult> reviewResult(
            @PathVariable Long resultId,
            @RequestParam Long doctorId,
            @RequestParam String reviewStatus,
            @RequestParam(required = false) String comment) {
        try {
            EcgAnalysisResult result = ecgAnalysisService.reviewResult(resultId, doctorId, reviewStatus, comment);
            return Result.success(result);
        } catch (Exception e) {
            log.error("审核ECG结果失败", e);
            return Result.error("审核ECG结果失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/report/{resultId}")
    @ApiOperation("生成ECG诊断报告")
    public Result<String> generateReport(@PathVariable Long resultId) {
        try {
            String report = ecgAnalysisService.generateDiagnosisReport(resultId);
            return Result.success(report);
        } catch (Exception e) {
            log.error("生成诊断报告失败", e);
            return Result.error("生成诊断报告失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics/abnormal")
    @ApiOperation("统计用户ECG异常情况")
    public Result<Map<String, Object>> getAbnormalStatistics(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = ecgAnalysisService.getAbnormalStatistics(userId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取ECG异常统计失败", e);
            return Result.error("获取ECG异常统计失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/simulation/start")
    @ApiOperation("启动ECG数据模拟")
    public Result<String> startSimulation(@RequestParam Long userId) {
        try {
            ecgSimulationService.startSimulation(userId);
            return Result.success("ECG数据模拟已启动");
        } catch (Exception e) {
            log.error("启动ECG模拟失败", e);
            return Result.error("启动ECG模拟失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/simulation/stop")
    @ApiOperation("停止ECG数据模拟")
    public Result<String> stopSimulation(@RequestParam Long userId) {
        try {
            ecgSimulationService.stopSimulation(userId);
            return Result.success("ECG数据模拟已停止");
        } catch (Exception e) {
            log.error("停止ECG模拟失败", e);
            return Result.error("停止ECG模拟失败: " + e.getMessage());
        }
    }
}
