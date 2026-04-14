package com.cardioguard.controller;

import com.cardioguard.common.Result;
import com.cardioguard.entity.EcgAnnotation;
import com.cardioguard.service.EcgAnnotationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ECG标注控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ecg/annotation")
@Api(tags = "ECG标注管理")
public class EcgAnnotationController {
    
    @Autowired
    private EcgAnnotationService ecgAnnotationService;
    
    @PostMapping
    @ApiOperation("创建标注")
    public Result<EcgAnnotation> createAnnotation(@RequestBody EcgAnnotation annotation) {
        try {
            EcgAnnotation result = ecgAnnotationService.createAnnotation(annotation);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建标注失败", e);
            return Result.error("创建标注失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch")
    @ApiOperation("批量创建标注")
    public Result<Integer> batchCreateAnnotations(@RequestBody List<EcgAnnotation> annotations) {
        try {
            int count = ecgAnnotationService.batchCreateAnnotations(annotations);
            return Result.success(count);
        } catch (Exception e) {
            log.error("批量创建标注失败", e);
            return Result.error("批量创建标注失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{annotationId}")
    @ApiOperation("更新标注")
    public Result<EcgAnnotation> updateAnnotation(
            @PathVariable Long annotationId,
            @RequestBody EcgAnnotation annotation) {
        try {
            annotation.setId(annotationId);
            EcgAnnotation result = ecgAnnotationService.updateAnnotation(annotation);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新标注失败", e);
            return Result.error("更新标注失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{annotationId}")
    @ApiOperation("删除标注")
    public Result<String> deleteAnnotation(
            @PathVariable Long annotationId,
            @RequestParam Long userId) {
        try {
            ecgAnnotationService.deleteAnnotation(annotationId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除标注失败", e);
            return Result.error("删除标注失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/result/{analysisResultId}")
    @ApiOperation("查询指定分析结果的标注")
    public Result<List<EcgAnnotation>> getAnnotationsByResultId(
            @PathVariable Long analysisResultId) {
        try {
            List<EcgAnnotation> annotations = ecgAnnotationService.getAnnotationsByResultId(analysisResultId);
            return Result.success(annotations);
        } catch (Exception e) {
            log.error("查询标注失败", e);
            return Result.error("查询标注失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/history")
    @ApiOperation("查询用户标注历史")
    public Result<List<EcgAnnotation>> getUserAnnotationHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<EcgAnnotation> annotations = ecgAnnotationService.getUserAnnotationHistory(userId, page, size);
            return Result.success(annotations);
        } catch (Exception e) {
            log.error("查询标注历史失败", e);
            return Result.error("查询标注历史失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/ai-annotate/{analysisResultId}")
    @ApiOperation("AI自动标注")
    public Result<List<EcgAnnotation>> aiAutoAnnotate(
            @PathVariable Long analysisResultId,
            @RequestParam Long userId) {
        try {
            List<EcgAnnotation> annotations = ecgAnnotationService.aiAutoAnnotate(analysisResultId, userId);
            return Result.success(annotations);
        } catch (Exception e) {
            log.error("AI自动标注失败", e);
            return Result.error("AI自动标注失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @ApiOperation("查询用户标注统计")
    public Result<Map<String, Object>> getUserAnnotationStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> statistics = ecgAnnotationService.getUserAnnotationStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询标注统计失败", e);
            return Result.error("查询标注统计失败: " + e.getMessage());
        }
    }
}
