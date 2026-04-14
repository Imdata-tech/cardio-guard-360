package com.cardioguard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 健康监控WebSocket处理器
 * 用于实时推送心率、ECG等生理参数数据
 */
@Slf4j
public class HealthMonitorWebSocketHandler extends TextWebSocketHandler {
    
    /**
     * 存储所有活跃的WebSocket会话, key为userId
     */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数中获取userId
        String query = session.getUri().getQuery();
        Long userId = extractUserId(query);
        
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket连接建立成功: userId={}, sessionId={}", userId, session.getId());
            
            // 发送欢迎消息
            Map<String, Object> message = Map.of(
                "type", "connected",
                "message", "已成功连接到健康监测服务",
                "userId", userId
            );
            sendMessage(session, message);
        } else {
            session.close(CloseStatus.BAD_DATA);
            log.warn("WebSocket连接失败: 缺少userId参数");
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息(如心跳包)
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);
        
        // 可以在此处处理客户端上报的数据
        Map<String, Object> response = Map.of(
            "type", "ack",
            "message", "消息已接收"
        );
        sendMessage(session, response);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除断开的会话
        SESSIONS.values().remove(session);
        log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
        SESSIONS.values().remove(session);
    }
    
    /**
     * 向指定用户推送实时心率数据
     * 
     * @param userId 用户ID
     * @param data 数据对象
     */
    public static void pushData(Long userId, Object data) {
        pushRealtimeData(userId, "realtime-data", data);
    }
    
    /**
     * 向指定用户推送实时ECG数据
     * 
     * @param userId 用户ID
     * @param ecgData ECG数据对象
     */
    public static void pushEcgData(Long userId, Object ecgData) {
        pushRealtimeData(userId, "ecg-data", ecgData);
    }
    
    /**
     * 推送实时数据的通用方法
     * 
     * @param userId 用户ID
     * @param dataType 数据类型 (realtime-data, ecg-data等)
     * @param data 数据对象
     */
    private static void pushRealtimeData(Long userId, String dataType, Object data) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                    "type", dataType,
                    "timestamp", System.currentTimeMillis(),
                    "data", data
                );
                sendMessage(session, message);
                log.debug("{}推送成功: userId={}", dataType, userId);
            } catch (IOException e) {
                log.error("{}推送失败: userId={}", dataType, userId, e);
            }
        } else {
            log.warn("用户不在线或会话已关闭: userId={}", userId);
        }
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public static void broadcast(Object data) {
        SESSIONS.forEach((userId, session) -> {
            try {
                Map<String, Object> message = Map.of(
                    "type", "broadcast",
                    "data", data
                );
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("广播消息失败: userId={}", userId, e);
            }
        });
    }
    
    /**
     * 发送消息到WebSocket会话
     */
    private static void sendMessage(WebSocketSession session, Object message) throws IOException {
        String jsonMessage = OBJECT_MAPPER.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
    }
    
    /**
     * 从查询字符串中提取userId
     */
    private Long extractUserId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("userId=")) {
                try {
                    return Long.parseLong(param.substring(7));
                } catch (NumberFormatException e) {
                    log.error("解析userId失败: {}", param);
                    return null;
                }
            }
        }
        return null;
    }
}
