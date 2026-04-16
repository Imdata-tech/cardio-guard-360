package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardioguard.entity.EcgMultiLeadRecord;
import com.cardioguard.mapper.EcgMultiLeadRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 多导联ECG记录服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
class EcgMultiLeadRecordServiceImplTest {
    
    @Mock
    private EcgMultiLeadRecordMapper multiLeadRecordMapper;
    
    @InjectMocks
    private EcgMultiLeadRecordServiceImpl multiLeadRecordService;
    
    private EcgMultiLeadRecord testRecord;
    
    @BeforeEach
    void setUp() {
        testRecord = new EcgMultiLeadRecord();
        testRecord.setId(1L);
        testRecord.setUserId(100L);
        testRecord.setDeviceId(200L);
        testRecord.setRecordName("测试记录");
        testRecord.setLeadCount(12);
        testRecord.setSampleRate(500);
        testRecord.setDurationSeconds(60);
        testRecord.setDataStorageId("influx_12345");
        testRecord.setStatus("RECORDING");
        testRecord.setCreatedAt(LocalDateTime.now());
        testRecord.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testCreateRecord() {
        // 准备测试数据
        when(multiLeadRecordMapper.insert(any(EcgMultiLeadRecord.class))).thenReturn(1);
        
        // 执行测试
        EcgMultiLeadRecord result = multiLeadRecordService.createRecord(testRecord);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("RECORDING", result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(multiLeadRecordMapper, times(1)).insert(any(EcgMultiLeadRecord.class));
    }
    
    @Test
    void testUpdateRecordStatus() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(multiLeadRecordMapper.updateById(any(EcgMultiLeadRecord.class))).thenReturn(1);
        
        // 执行测试
        EcgMultiLeadRecord result = multiLeadRecordService.updateRecordStatus(1L, "COMPLETED");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(multiLeadRecordMapper, times(1)).selectById(1L);
        verify(multiLeadRecordMapper, times(1)).updateById(any(EcgMultiLeadRecord.class));
    }
    
    @Test
    void testUpdateRecordStatusNotFound() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(999L)).thenReturn(null);
        
        // 执行测试
        EcgMultiLeadRecord result = multiLeadRecordService.updateRecordStatus(999L, "COMPLETED");
        
        // 验证结果
        assertNull(result);
        verify(multiLeadRecordMapper, times(1)).selectById(999L);
        verify(multiLeadRecordMapper, never()).updateById(any(EcgMultiLeadRecord.class));
    }
    
    @Test
    void testGetRecordsByUserId() {
        // 准备测试数据
        Page<EcgMultiLeadRecord> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(testRecord));
        when(multiLeadRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        
        // 执行测试
        List<EcgMultiLeadRecord> result = multiLeadRecordService.getRecordsByUserId(100L, 1, 20);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getUserId());
        verify(multiLeadRecordMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testGetRecordById() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(1L)).thenReturn(testRecord);
        
        // 执行测试
        EcgMultiLeadRecord result = multiLeadRecordService.getRecordById(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试记录", result.getRecordName());
        verify(multiLeadRecordMapper, times(1)).selectById(1L);
    }
    
    @Test
    void testGetRecordByIdNotFound() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(999L)).thenReturn(null);
        
        // 执行测试
        EcgMultiLeadRecord result = multiLeadRecordService.getRecordById(999L);
        
        // 验证结果
        assertNull(result);
        verify(multiLeadRecordMapper, times(1)).selectById(999L);
    }
    
    @Test
    void testDeleteRecordSuccess() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(multiLeadRecordMapper.deleteById(1L)).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> multiLeadRecordService.deleteRecord(1L, 100L));
        
        // 验证结果
        verify(multiLeadRecordMapper, times(1)).selectById(1L);
        verify(multiLeadRecordMapper, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteRecordNoPermission() {
        // 准备测试数据 - 用户ID不匹配
        when(multiLeadRecordMapper.selectById(1L)).thenReturn(testRecord);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            multiLeadRecordService.deleteRecord(1L, 999L);
        });
        
        // 验证结果
        verify(multiLeadRecordMapper, times(1)).selectById(1L);
        verify(multiLeadRecordMapper, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteRecordNotFound() {
        // 准备测试数据
        when(multiLeadRecordMapper.selectById(999L)).thenReturn(null);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            multiLeadRecordService.deleteRecord(999L, 100L);
        });
        
        // 验证结果
        verify(multiLeadRecordMapper, times(1)).selectById(999L);
        verify(multiLeadRecordMapper, never()).deleteById(anyLong());
    }
}
