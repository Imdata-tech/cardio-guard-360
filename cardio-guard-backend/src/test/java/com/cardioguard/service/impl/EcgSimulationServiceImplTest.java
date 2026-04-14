package com.cardioguard.service.impl;

import com.cardioguard.service.EcgSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ECG模拟服务单元测试
 */
class EcgSimulationServiceImplTest {
    
    @InjectMocks
    private EcgSimulationService ecgSimulationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testStartSimulation() {
        // Arrange
        Long userId = 1L;
        
        // Act
        ecgSimulationService.startSimulation(userId);
        
        // Assert - 验证没有抛出异常
        assertDoesNotThrow(() -> {
            ecgSimulationService.startSimulation(userId);
        });
    }
    
    @Test
    void testStopSimulation() {
        // Arrange
        Long userId = 1L;
        ecgSimulationService.startSimulation(userId);
        
        // Act
        ecgSimulationService.stopSimulation(userId);
        
        // Assert - 验证没有抛出异常
        assertDoesNotThrow(() -> {
            ecgSimulationService.stopSimulation(userId);
        });
    }
    
    @Test
    void testStartAndStopSimulation_MultipleUsers() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;
        
        // Act - 启动多个用户模拟
        assertDoesNotThrow(() -> {
            ecgSimulationService.startSimulation(userId1);
            ecgSimulationService.startSimulation(userId2);
            ecgSimulationService.startSimulation(userId3);
        });
        
        // Act - 停止所有用户模拟
        assertDoesNotThrow(() -> {
            ecgSimulationService.stopSimulation(userId1);
            ecgSimulationService.stopSimulation(userId2);
            ecgSimulationService.stopSimulation(userId3);
        });
    }
    
    @Test
    void testGenerateEcgDataPoint_Structure() throws Exception {
        // Arrange
        Long userId = 1L;
        ecgSimulationService.startSimulation(userId);
        
        // 通过反射访问私有方法
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> dataPoint = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
        
        // Assert
        assertNotNull(dataPoint);
        assertTrue(dataPoint.containsKey("timestamp"));
        assertTrue(dataPoint.containsKey("voltage"));
        assertTrue(dataPoint.containsKey("heartRate"));
        assertTrue(dataPoint.containsKey("sampleRate"));
        
        // 验证数据类型
        assertTrue(dataPoint.get("timestamp") instanceof Long);
        assertTrue(dataPoint.get("voltage") instanceof Double);
        assertTrue(dataPoint.get("heartRate") instanceof Double);
        assertTrue(dataPoint.get("sampleRate") instanceof Integer);
    }
    
    @Test
    void testGenerateEcgDataPoint_VoltageRange() throws Exception {
        // Arrange
        Long userId = 1L;
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act & Assert - 生成多个数据点验证电压范围
        for (int i = 0; i < 100; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataPoint = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
            Double voltage = (Double) dataPoint.get("voltage");
            
            // ECG电压通常在 -2mV 到 +2mV 之间
            assertTrue(voltage >= -3.0 && voltage <= 3.0, 
                "电压值超出合理范围: " + voltage);
        }
    }
    
    @Test
    void testGenerateEcgDataPoint_HeartRateRange() throws Exception {
        // Arrange
        Long userId = 1L;
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act & Assert - 生成多个数据点验证心率范围
        for (int i = 0; i < 100; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataPoint = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
            Double heartRate = (Double) dataPoint.get("heartRate");
            
            // 心率应该在合理范围内 (60-100 bpm)
            assertTrue(heartRate >= 50.0 && heartRate <= 120.0, 
                "心率值超出合理范围: " + heartRate);
        }
    }
    
    @Test
    void testGenerateEcgDataPoint_SampleRate() throws Exception {
        // Arrange
        Long userId = 1L;
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> dataPoint = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
        
        // Assert
        assertEquals(250, dataPoint.get("sampleRate"));
    }
    
    @Test
    void testGenerateEcgDataPoint_TimestampIncrement() throws Exception {
        // Arrange
        Long userId = 1L;
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> dataPoint1 = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
        
        Thread.sleep(10); // 等待10ms
        
        @SuppressWarnings("unchecked")
        Map<String, Object> dataPoint2 = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
        
        // Assert
        Long timestamp1 = (Long) dataPoint1.get("timestamp");
        Long timestamp2 = (Long) dataPoint2.get("timestamp");
        
        assertTrue(timestamp2 > timestamp1, "时间戳应该递增");
    }
    
    @Test
    void testGenerateEcgDataPoint_QRSWaveform() throws Exception {
        // Arrange
        Long userId = 1L;
        Method method = EcgSimulationService.class.getDeclaredMethod("generateEcgDataPoint", Long.class);
        method.setAccessible(true);
        
        // Act & Assert - 生成大量数据点,验证是否存在QRS波峰
        boolean hasHighPeak = false;
        boolean hasLowValley = false;
        
        for (int i = 0; i < 1000; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataPoint = (Map<String, Object>) method.invoke(ecgSimulationService, userId);
            Double voltage = (Double) dataPoint.get("voltage");
            
            if (voltage > 1.0) {
                hasHighPeak = true; // R波峰值
            }
            if (voltage < -0.5) {
                hasLowValley = true; // Q或S波谷值
            }
            
            if (hasHighPeak && hasLowValley) {
                break;
            }
        }
        
        assertTrue(hasHighPeak, "应该存在R波高峰值");
        assertTrue(hasLowValley, "应该存在Q/S波低谷值");
    }
    
    @Test
    void testPushEcgData_NoException() {
        // Arrange
        Long userId = 1L;
        ecgSimulationService.startSimulation(userId);
        
        // Act & Assert - 定时任务不应该抛出异常
        assertDoesNotThrow(() -> {
            ecgSimulationService.pushEcgData();
        });
    }
    
    @Test
    void testPushEcgData_MultipleUsers() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;
        ecgSimulationService.startSimulation(userId1);
        ecgSimulationService.startSimulation(userId2);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ecgSimulationService.pushEcgData();
        });
    }
    
    @Test
    void testPushEcgData_StoppedUser() {
        // Arrange
        Long userId = 1L;
        ecgSimulationService.startSimulation(userId);
        ecgSimulationService.stopSimulation(userId);
        
        // Act & Assert - 停止的用户不应推送数据
        assertDoesNotThrow(() -> {
            ecgSimulationService.pushEcgData();
        });
    }
    
    @Test
    void testCleanup() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;
        ecgSimulationService.startSimulation(userId1);
        ecgSimulationService.startSimulation(userId2);
        
        // Act
        assertDoesNotThrow(() -> {
            ecgSimulationService.cleanup();
        });
    }
}
