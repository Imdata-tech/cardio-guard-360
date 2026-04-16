package com.cardioguard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * ECG导联配置实体类
 */
@Data
@TableName("ecg_lead_config")
public class EcgLeadConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 导联名称 (I, II, III, aVR, aVL, aVF, V1-V6)
     */
    private String leadName;
    
    /**
     * 导联类型 (LIMB-肢体导联, PRECORDIAL-胸导联)
     */
    private String leadType;
    
    /**
     * 显示颜色 (十六进制)
     */
    private String displayColor;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 是否默认显示
     */
    private Integer isDefault;
    
    /**
     * 描述
     */
    private String description;
}
