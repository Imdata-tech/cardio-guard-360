package com.cardioguard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标注模板实体类
 */
@Data
@TableName("annotation_template")
public class AnnotationTemplate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 标注类型
     */
    private String annotationType;
    
    /**
     * 默认标签
     */
    private String defaultLabel;
    
    /**
     * 显示颜色(十六进制)
     */
    private String color;
    
    /**
     * 图标标识
     */
    private String icon;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 是否系统预设(0-用户自定义, 1-系统预设)
     */
    private Integer isSystem;
    
    /**
     * 创建者ID(系统预设为NULL)
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
