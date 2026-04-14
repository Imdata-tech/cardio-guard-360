package com.cardioguard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HealthMonitorWebSocketHandler单元测试
 */
@SpringBootTest
class HealthMonitorWebSocketHandlerTest {
    
    private HealthMonitorWebSocketHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new HealthMonitorWebSocketHandler();
    }
    
    @Test
    void testExtractUserId_ValidQuery() throws Exception {
        // 通过反射访问私有方法
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        // Act
        Long userId = (Long) method.invoke(handler, "userId=123&token=abc");
        
        // Assert
        assertEquals(123L, userId);
    }
    
    @Test
    void testExtractUserId_OnlyUserId() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "userId=456");
        
        assertEquals(456L, userId);
    }
    
    @Test
    void testExtractUserId_NullQuery() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, (String) null);
        
        assertNull(userId);
    }
    
    @Test
    void testExtractUserId_EmptyQuery() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "");
        
        assertNull(userId);
    }
    
    @Test
    void testExtractUserId_InvalidUserId() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "userId=abc");
        
        assertNull(userId);
    }
    
    @Test
    void testExtractUserId_NoUserIdParam() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "token=xyz&other=123");
        
        assertNull(userId);
    }
    
    @Test
    void testExtractUserId_MultipleParams() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "token=abc&userId=789&device=1");
        
        assertEquals(789L, userId);
    }
    
    @Test
    void testExtractUserId_NegativeUserId() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "userId=-1");
        
        assertEquals(-1L, userId);
    }
    
    @Test
    void testExtractUserId_ZeroUserId() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "userId=0");
        
        assertEquals(0L, userId);
    }
    
    @Test
    void testExtractUserId_LargeUserId() throws Exception {
        Method method = HealthMonitorWebSocketHandler.class.getDeclaredMethod("extractUserId", String.class);
        method.setAccessible(true);
        
        Long userId = (Long) method.invoke(handler, "userId=9999999999");
        
        assertEquals(9999999999L, userId);
    }
}
