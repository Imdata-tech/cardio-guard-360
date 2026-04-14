package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardioguard.entity.EcgAnnotation;
import com.cardioguard.mapper.EcgAnnotationMapper;
import com.cardioguard.service.EcgAnnotationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ECG标注服务实现类
 */
@Slf4j
@Service
public class EcgAnnotationServiceImpl implements EcgAnnotationService {
    
    @Autowired
    private EcgAnnotationMapper ecgAnnotationMapper;
    
    @Override
    @Transactional
    public EcgAnnotation createAnnotation(EcgAnnotation annotation) {
        // 设置默认值
        if (annotation.getIsAiGenerated() == null) {
            annotation.setIsAiGenerated(0);
        }
        if (annotation.getCreatedAt() == null) {
            annotation.setCreatedAt(LocalDateTime.now());
        }
        
        int result = ecgAnnotationMapper.insert(annotation);
        if (result > 0) {
            log.info("创建ECG标注成功: annotationId={}, type={}", 
                annotation.getId(), annotation.getAnnotationType());
            return annotation;
        } else {
            throw new RuntimeException("创建ECG标注失败");
        }
    }
    
    @Override
    @Transactional
    public int batchCreateAnnotations(List<EcgAnnotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (EcgAnnotation annotation : annotations) {
            try {
                createAnnotation(annotation);
                count++;
            } catch (Exception e) {
                log.error("批量创建标注失败: {}", e.getMessage());
            }
        }
        
        log.info("批量创建ECG标注完成: 成功{}/{}", count, annotations.size());
        return count;
    }
    
    @Override
    @Transactional
    public EcgAnnotation updateAnnotation(EcgAnnotation annotation) {
        annotation.setUpdatedAt(LocalDateTime.now());
        
        int result = ecgAnnotationMapper.updateById(annotation);
        if (result > 0) {
            log.info("更新ECG标注成功: annotationId={}", annotation.getId());
            return annotation;
        } else {
            throw new RuntimeException("更新ECG标注失败,标注不存在");
        }
    }
    
    @Override
    @Transactional
    public void deleteAnnotation(Long annotationId, Long userId) {
        // 权限验证:只能删除自己创建的标注
        EcgAnnotation annotation = ecgAnnotationMapper.selectById(annotationId);
        if (annotation == null) {
            throw new RuntimeException("标注不存在");
        }
        
        if (!annotation.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权限删除此标注");
        }
        
        int result = ecgAnnotationMapper.deleteById(annotationId);
        if (result > 0) {
            log.info("删除ECG标注成功: annotationId={}", annotationId);
        } else {
            throw new RuntimeException("删除ECG标注失败");
        }
    }
    
    @Override
    public List<EcgAnnotation> getAnnotationsByResultId(Long analysisResultId) {
        LambdaQueryWrapper<EcgAnnotation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgAnnotation::getAnalysisResultId, analysisResultId)
               .orderByAsc(EcgAnnotation::getTimestampMs);
        
        return ecgAnnotationMapper.selectList(wrapper);
    }
    
    @Override
    public List<EcgAnnotation> getUserAnnotationHistory(Long userId, Integer page, Integer size) {
        Page<EcgAnnotation> pagination = new Page<>(page, size);
        
        LambdaQueryWrapper<EcgAnnotation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgAnnotation::getUserId, userId)
               .orderByDesc(EcgAnnotation::getCreatedAt);
        
        Page<EcgAnnotation> resultPage = ecgAnnotationMapper.selectPage(pagination, wrapper);
        return resultPage.getRecords();
    }
    
    @Override
    @Transactional
    public List<EcgAnnotation> aiAutoAnnotate(Long analysisResultId, Long userId) {
        log.info("开始AI自动标注: analysisResultId={}", analysisResultId);
        
        // TODO: 集成真实的AI模型进行ECG波形分析
        // 这里使用模拟算法生成标注
        
        List<EcgAnnotation> annotations = new ArrayList<>();
        
        // 模拟生成P/QRS/T波标注
        // 假设ECG记录时长10秒,采样率250Hz,心率72bpm
        double heartRate = 72.0;
        double beatInterval = 60000.0 / heartRate; // 每个心跳周期的毫秒数
        int numBeats = 10; // 10秒内约12个心跳
        
        for (int i = 0; i < numBeats; i++) {
            double baseTime = i * beatInterval;
            
            // P波 (心房去极化)
            EcgAnnotation pWave = new EcgAnnotation();
            pWave.setUserId(userId);
            pWave.setAnalysisResultId(analysisResultId);
            pWave.setTimestampMs((long)(baseTime + 80));
            pWave.setAnnotationType("P_WAVE");
            pWave.setLabel("P");
            pWave.setConfidence(0.92 + Math.random() * 0.06);
            pWave.setIsAiGenerated(1);
            pWave.setCreatedBy(userId);
            annotations.add(pWave);
            
            // QRS波群 (心室去极化)
            EcgAnnotation qrs = new EcgAnnotation();
            qrs.setUserId(userId);
            qrs.setAnalysisResultId(analysisResultId);
            qrs.setTimestampMs((long)(baseTime + 160));
            qrs.setAnnotationType("QRS_COMPLEX");
            qrs.setLabel("QRS");
            qrs.setConfidence(0.95 + Math.random() * 0.04);
            qrs.setIsAiGenerated(1);
            qrs.setCreatedBy(userId);
            annotations.add(qrs);
            
            // T波 (心室复极化)
            EcgAnnotation tWave = new EcgAnnotation();
            tWave.setUserId(userId);
            tWave.setAnalysisResultId(analysisResultId);
            tWave.setTimestampMs((long)(baseTime + 320));
            tWave.setAnnotationType("T_WAVE");
            tWave.setLabel("T");
            tWave.setConfidence(0.90 + Math.random() * 0.08);
            tWave.setIsAiGenerated(1);
            tWave.setCreatedBy(userId);
            annotations.add(tWave);
        }
        
        // 批量保存
        batchCreateAnnotations(annotations);
        
        log.info("AI自动标注完成: 生成{}个标注", annotations.size());
        return annotations;
    }
    
    @Override
    public Map<String, Object> getUserAnnotationStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计总标注数
        LambdaQueryWrapper<EcgAnnotation> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(EcgAnnotation::getUserId, userId);
        long totalCount = ecgAnnotationMapper.selectCount(totalWrapper);
        statistics.put("totalCount", totalCount);
        
        // 按类型统计
        String[] types = {"P_WAVE", "QRS_COMPLEX", "T_WAVE", "ST_SEGMENT", "ABNORMAL_POINT", "OTHER"};
        Map<String, Long> typeStats = new HashMap<>();
        
        for (String type : types) {
            Long count = ecgAnnotationMapper.countByType(userId, type);
            typeStats.put(type, count != null ? count : 0L);
        }
        statistics.put("typeStatistics", typeStats);
        
        // AI标注vs手动标注
        LambdaQueryWrapper<EcgAnnotation> aiWrapper = new LambdaQueryWrapper<>();
        aiWrapper.eq(EcgAnnotation::getUserId, userId)
                .eq(EcgAnnotation::getIsAiGenerated, 1);
        long aiCount = ecgAnnotationMapper.selectCount(aiWrapper);
        
        statistics.put("aiGeneratedCount", aiCount);
        statistics.put("manualCount", totalCount - aiCount);
        
        return statistics;
    }
}
