package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cardioguard.entity.EcgAnnotation;
import com.cardioguard.mapper.EcgAnnotationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ECG标注服务单元测试
 */
class EcgAnnotationServiceImplTest {
    
    @Mock
    private EcgAnnotationMapper ecgAnnotationMapper;
    
    @InjectMocks
    private EcgAnnotationServiceImpl ecgAnnotationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testCreateAnnotation_Success() {
        // Arrange
        EcgAnnotation annotation = new EcgAnnotation();
        annotation.setUserId(1L);
        annotation.setAnalysisResultId(1L);
        annotation.setTimestampMs(1000L);
        annotation.setAnnotationType("P_WAVE");
        annotation.setLabel("P");
        annotation.setCreatedBy(1L);
        
        when(ecgAnnotationMapper.insert(any(EcgAnnotation.class))).thenReturn(1);
        
        // Act
        EcgAnnotation result = ecgAnnotationService.createAnnotation(annotation);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("P_WAVE", result.getAnnotationType());
        verify(ecgAnnotationMapper, times(1)).insert(any(EcgAnnotation.class));
    }
    
    @Test
    void testCreateAnnotation_WithDefaultValues() {
        // Arrange
        EcgAnnotation annotation = new EcgAnnotation();
        annotation.setUserId(1L);
        annotation.setAnalysisResultId(1L);
        annotation.setTimestampMs(1000L);
        annotation.setAnnotationType("QRS_COMPLEX");
        annotation.setCreatedBy(1L);
        // isAiGenerated and createdAt not set
        
        when(ecgAnnotationMapper.insert(any(EcgAnnotation.class))).thenReturn(1);
        
        // Act
        EcgAnnotation result = ecgAnnotationService.createAnnotation(annotation);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getIsAiGenerated()); // 默认值
        assertNotNull(result.getCreatedAt()); // 自动设置
    }
    
    @Test
    void testBatchCreateAnnotations_Success() {
        // Arrange
        List<EcgAnnotation> annotations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            EcgAnnotation annotation = new EcgAnnotation();
            annotation.setUserId(1L);
            annotation.setAnalysisResultId(1L);
            annotation.setTimestampMs((long)(i * 1000));
            annotation.setAnnotationType("P_WAVE");
            annotation.setCreatedBy(1L);
            annotations.add(annotation);
        }
        
        when(ecgAnnotationMapper.insert(any(EcgAnnotation.class))).thenReturn(1);
        
        // Act
        int count = ecgAnnotationService.batchCreateAnnotations(annotations);
        
        // Assert
        assertEquals(5, count);
        verify(ecgAnnotationMapper, times(5)).insert(any(EcgAnnotation.class));
    }
    
    @Test
    void testBatchCreateAnnotations_EmptyList() {
        // Act
        int count = ecgAnnotationService.batchCreateAnnotations(new ArrayList<>());
        
        // Assert
        assertEquals(0, count);
        verify(ecgAnnotationMapper, never()).insert(any(EcgAnnotation.class));
    }
    
    @Test
    void testUpdateAnnotation_Success() {
        // Arrange
        EcgAnnotation annotation = new EcgAnnotation();
        annotation.setId(1L);
        annotation.setLabel("Updated Label");
        
        when(ecgAnnotationMapper.updateById(any(EcgAnnotation.class))).thenReturn(1);
        
        // Act
        EcgAnnotation result = ecgAnnotationService.updateAnnotation(annotation);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Label", result.getLabel());
        assertNotNull(result.getUpdatedAt());
        verify(ecgAnnotationMapper, times(1)).updateById(any(EcgAnnotation.class));
    }
    
    @Test
    void testDeleteAnnotation_Success() {
        // Arrange
        Long annotationId = 1L;
        Long userId = 1L;
        
        EcgAnnotation annotation = new EcgAnnotation();
        annotation.setId(annotationId);
        annotation.setCreatedBy(userId);
        
        when(ecgAnnotationMapper.selectById(annotationId)).thenReturn(annotation);
        when(ecgAnnotationMapper.deleteById(annotationId)).thenReturn(1);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ecgAnnotationService.deleteAnnotation(annotationId, userId);
        });
        
        verify(ecgAnnotationMapper, times(1)).deleteById(annotationId);
    }
    
    @Test
    void testDeleteAnnotation_NotFound() {
        // Arrange
        Long annotationId = 999L;
        Long userId = 1L;
        
        when(ecgAnnotationMapper.selectById(annotationId)).thenReturn(null);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ecgAnnotationService.deleteAnnotation(annotationId, userId);
        });
        
        assertTrue(exception.getMessage().contains("标注不存在"));
    }
    
    @Test
    void testDeleteAnnotation_NoPermission() {
        // Arrange
        Long annotationId = 1L;
        Long userId = 1L;
        
        EcgAnnotation annotation = new EcgAnnotation();
        annotation.setId(annotationId);
        annotation.setCreatedBy(2L); // 不同用户
        
        when(ecgAnnotationMapper.selectById(annotationId)).thenReturn(annotation);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ecgAnnotationService.deleteAnnotation(annotationId, userId);
        });
        
        assertTrue(exception.getMessage().contains("无权限删除此标注"));
    }
    
    @Test
    void testGetAnnotationsByResultId() {
        // Arrange
        Long analysisResultId = 1L;
        List<EcgAnnotation> mockAnnotations = new ArrayList<>();
        EcgAnnotation ann1 = new EcgAnnotation();
        ann1.setAnnotationType("P_WAVE");
        EcgAnnotation ann2 = new EcgAnnotation();
        ann2.setAnnotationType("QRS_COMPLEX");
        mockAnnotations.add(ann1);
        mockAnnotations.add(ann2);
        
        when(ecgAnnotationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockAnnotations);
        
        // Act
        List<EcgAnnotation> result = ecgAnnotationService.getAnnotationsByResultId(analysisResultId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ecgAnnotationMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testGetUserAnnotationHistory() {
        // Arrange
        Long userId = 1L;
        Integer page = 1;
        Integer size = 10;
        
        List<EcgAnnotation> mockAnnotations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            EcgAnnotation ann = new EcgAnnotation();
            ann.setAnnotationType("P_WAVE");
            mockAnnotations.add(ann);
        }
        
        when(ecgAnnotationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockAnnotations);
        
        // Act
        List<EcgAnnotation> result = ecgAnnotationService.getUserAnnotationHistory(userId, page, size);
        
        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }
    
    @Test
    void testAiAutoAnnotate() {
        // Arrange
        Long analysisResultId = 1L;
        Long userId = 1L;
        
        when(ecgAnnotationMapper.insert(any(EcgAnnotation.class))).thenReturn(1);
        
        // Act
        List<EcgAnnotation> annotations = ecgAnnotationService.aiAutoAnnotate(analysisResultId, userId);
        
        // Assert
        assertNotNull(annotations);
        assertTrue(annotations.size() > 0);
        
        // 验证包含P/QRS/T波
        long pWaveCount = annotations.stream()
            .filter(a -> "P_WAVE".equals(a.getAnnotationType()))
            .count();
        long qrsCount = annotations.stream()
            .filter(a -> "QRS_COMPLEX".equals(a.getAnnotationType()))
            .count();
        long tWaveCount = annotations.stream()
            .filter(a -> "T_WAVE".equals(a.getAnnotationType()))
            .count();
        
        assertTrue(pWaveCount > 0, "应该包含P波标注");
        assertTrue(qrsCount > 0, "应该包含QRS波标注");
        assertTrue(tWaveCount > 0, "应该包含T波标注");
        
        // 验证都是AI生成的
        annotations.forEach(a -> {
            assertEquals(1, a.getIsAiGenerated());
            assertNotNull(a.getConfidence());
        });
    }
    
    @Test
    void testGetUserAnnotationStatistics() {
        // Arrange
        Long userId = 1L;
        
        when(ecgAnnotationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(ecgAnnotationMapper.countByType(any(Long.class), any(String.class))).thenReturn(2L);
        
        // Act
        Map<String, Object> statistics = ecgAnnotationService.getUserAnnotationStatistics(userId);
        
        // Assert
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalCount"));
        assertTrue(statistics.containsKey("typeStatistics"));
        assertTrue(statistics.containsKey("aiGeneratedCount"));
        assertTrue(statistics.containsKey("manualCount"));
    }
}
