package com.cardioguard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ECG心电图标注实体类
 */
@Data
@TableName("ecg_annotation")
public class EcgAnnotation implements Serializable {
    
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
     * 关联的ECG分析结果ID
     */
    private Long analysisResultId;
    
    /**
     * 标注点的时间戳(毫秒,相对于记录开始)
     */
    private Long timestampMs;
    
    /**
     * 标注类型
     * P_WAVE - P波
     * QRS_COMPLEX - QRS波群
     * T_WAVE - T波
     * ST_SEGMENT - ST段
     * ABNORMAL_POINT - 异常点
     * OTHER - 其他
     */
    private String annotationType;
    
    /**
     * 标注标签/描述
     */
    private String label;
    
    /**
     * 标注置信度(0-1, AI自动标注时)
     */
    private Double confidence;
    
    /**
     * 是否AI自动生成(0-手动, 1-AI)
     */
    private Integer isAiGenerated;
    
    /**
     * 创建者ID(医生或用户)
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
