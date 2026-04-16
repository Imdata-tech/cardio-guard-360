package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cardioguard.entity.EcgLeadConfig;
import com.cardioguard.mapper.EcgLeadConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ECG导联配置服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
class EcgLeadConfigServiceImplTest {
    
    @Mock
    private EcgLeadConfigMapper leadConfigMapper;
    
    @InjectMocks
    private EcgLeadConfigServiceImpl leadConfigService;
    
    private EcgLeadConfig testConfig;
    
    @BeforeEach
    void setUp() {
        testConfig = new EcgLeadConfig();
        testConfig.setId(1L);
        testConfig.setLeadName("II");
        testConfig.setLeadType("LIMB");
        testConfig.setDisplayColor("#00FF00");
        testConfig.setDisplayOrder(2);
        testConfig.setIsDefault(1);
        testConfig.setDescription("标准肢体导联 II");
    }
    
    @Test
    void testGetAllLeadConfigs() {
        // 准备测试数据
        List<EcgLeadConfig> configs = Arrays.asList(testConfig);
        when(leadConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(configs);
        
        // 执行测试
        List<EcgLeadConfig> result = leadConfigService.getAllLeadConfigs();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("II", result.get(0).getLeadName());
        verify(leadConfigMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testGetLeadConfigByName() {
        // 准备测试数据
        when(leadConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);
        
        // 执行测试
        EcgLeadConfig result = leadConfigService.getLeadConfigByName("II");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("II", result.getLeadName());
        assertEquals("LIMB", result.getLeadType());
        verify(leadConfigMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testGetLeadConfigByNameNotFound() {
        // 准备测试数据
        when(leadConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        // 执行测试
        EcgLeadConfig result = leadConfigService.getLeadConfigByName("INVALID");
        
        // 验证结果
        assertNull(result);
        verify(leadConfigMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testGetDefaultLeadConfigs() {
        // 准备测试数据
        List<EcgLeadConfig> configs = Arrays.asList(testConfig);
        when(leadConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(configs);
        
        // 执行测试
        List<EcgLeadConfig> result = leadConfigService.getDefaultLeadConfigs();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getIsDefault());
        verify(leadConfigMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
    
    @Test
    void testUpdateLeadConfig() {
        // 准备测试数据
        when(leadConfigMapper.updateById(any(EcgLeadConfig.class))).thenReturn(1);
        when(leadConfigMapper.selectById(1L)).thenReturn(testConfig);
        
        // 执行测试
        EcgLeadConfig result = leadConfigService.updateLeadConfig(testConfig);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("II", result.getLeadName());
        verify(leadConfigMapper, times(1)).updateById(any(EcgLeadConfig.class));
        verify(leadConfigMapper, times(1)).selectById(1L);
    }
}
