package com.cardioguard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 多导联ECG记录实体类
 */
@Data
@TableName("ecg_multilead_record")
public class EcgMultiLeadRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 记录名称
     */
    private String recordName;
    
    /**
     * 导联数量 (标准12导联)
     */
    private Integer leadCount;
    
    /**
     * 采样率 (Hz)
     */
    private Integer sampleRate;
    
    /**
     * 记录时长 (秒)
     */
    private Integer durationSeconds;
    
    /**
     * 数据存储ID (InfluxDB或其他时序数据库)
     */
    private String dataStorageId;
    
    /**
     * 记录状态 (RECORDING-记录中, COMPLETED-已完成, ERROR-错误)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
