package com.cardioguard.service;

import com.cardioguard.entity.EcgAnnotation;

import java.util.List;
import java.util.Map;

/**
 * ECG标注服务接口
 */
public interface EcgAnnotationService {
    
    /**
     * 创建标注
     * 
     * @param annotation 标注对象
     * @return 创建的标注
     */
    EcgAnnotation createAnnotation(EcgAnnotation annotation);
    
    /**
     * 批量创建标注
     * 
     * @param annotations 标注列表
     * @return 创建的标注数量
     */
    int batchCreateAnnotations(List<EcgAnnotation> annotations);
    
    /**
     * 更新标注
     * 
     * @param annotation 标注对象
     * @return 更新后的标注
     */
    EcgAnnotation updateAnnotation(EcgAnnotation annotation);
    
    /**
     * 删除标注
     * 
     * @param annotationId 标注ID
     * @param userId 用户ID(权限验证)
     */
    void deleteAnnotation(Long annotationId, Long userId);
    
    /**
     * 查询指定分析结果的所有标注
     * 
     * @param analysisResultId 分析结果ID
     * @return 标注列表
     */
    List<EcgAnnotation> getAnnotationsByResultId(Long analysisResultId);
    
    /**
     * 查询用户的标注历史
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 标注列表
     */
    List<EcgAnnotation> getUserAnnotationHistory(Long userId, Integer page, Integer size);
    
    /**
     * AI自动标注ECG
     * 
     * @param analysisResultId 分析结果ID
     * @param userId 用户ID
     * @return AI生成的标注列表
     */
    List<EcgAnnotation> aiAutoAnnotate(Long analysisResultId, Long userId);
    
    /**
     * 统计用户标注情况
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserAnnotationStatistics(Long userId);
}
