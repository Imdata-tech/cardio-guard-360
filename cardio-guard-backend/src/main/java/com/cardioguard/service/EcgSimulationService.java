package com.cardioguard.service;

import com.cardioguard.config.HealthMonitorWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ECG实时数据模拟服务
 * 用于测试WebSocket实时推送功能
 */
@Slf4j
@Service
public class EcgSimulationService {
    
    /**
     * 存储每个用户的模拟状态,key为userId
     */
    private final Map<Long, Boolean> simulationStatus = new ConcurrentHashMap<>();
    
    /**
     * 启动ECG数据模拟
     * 
     * @param userId 用户ID
     */
    public void startSimulation(Long userId) {
        simulationStatus.put(userId, true);
        log.info("开始为用户 {} 模拟ECG数据", userId);
    }
    
    /**
     * 停止ECG数据模拟
     * 
     * @param userId 用户ID
     */
    public void stopSimulation(Long userId) {
        simulationStatus.put(userId, false);
        log.info("停止为用户 {} 模拟ECG数据", userId);
    }
    
    /**
     * 定时任务:每100ms推送一次ECG数据点
     * 模拟250Hz采样率(实际应该每4ms推送一次,这里为了演示降低频率)
     */
    @Scheduled(fixedRate = 100)
    public void pushEcgData() {
        simulationStatus.forEach((userId, isRunning) -> {
            if (isRunning) {
                Map<String, Object> ecgData = generateEcgDataPoint(userId);
                HealthMonitorWebSocketHandler.pushEcgData(userId, ecgData);
            }
        });
    }
    
    /**
     * 生成单个ECG数据点
     * 
     * @param userId 用户ID
     * @return ECG数据点
     */
    private Map<String, Object> generateEcgDataPoint(Long userId) {
        long timestamp = System.currentTimeMillis();
        
        // 模拟ECG波形:使用正弦波+噪声模拟P-QRS-T波形
        double time = timestamp / 1000.0;
        double baseWave = Math.sin(2 * Math.PI * 1.2 * time); // 基础心率约72bpm
        
        // 添加QRS复合波(尖峰)
        double qrsWave = 0;
        double phase = (time * 1.2) % 1.0; // 归一化到0-1
        if (phase > 0.35 && phase < 0.45) {
            // Q波
            qrsWave = -0.2 * Math.exp(-Math.pow((phase - 0.37) * 100, 2));
        } else if (phase > 0.45 && phase < 0.55) {
            // R波(主峰)
            qrsWave = 1.5 * Math.exp(-Math.pow((phase - 0.50) * 80, 2));
        } else if (phase > 0.55 && phase < 0.65) {
            // S波
            qrsWave = -0.3 * Math.exp(-Math.pow((phase - 0.60) * 100, 2));
        }
        
        // 添加T波
        double tWave = 0;
        if (phase > 0.70 && phase < 0.90) {
            tWave = 0.4 * Math.exp(-Math.pow((phase - 0.80) * 30, 2));
        }
        
        // 添加随机噪声
        double noise = (Math.random() - 0.5) * 0.05;
        
        // 最终电压值(mV)
        double voltage = baseWave * 0.1 + qrsWave + tWave + noise;
        
        // 计算瞬时心率
        double heartRate = 72 + Math.sin(time * 0.1) * 5 + (Math.random() - 0.5) * 2;
        
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("timestamp", timestamp);
        dataPoint.put("voltage", Math.round(voltage * 1000.0) / 1000.0); // 保留3位小数
        dataPoint.put("heartRate", Math.round(heartRate * 10.0) / 10.0); // 保留1位小数
        dataPoint.put("sampleRate", 250);
        
        return dataPoint;
    }
    
    /**
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        simulationStatus.clear();
        log.info("ECG模拟服务已清理");
    }
}
