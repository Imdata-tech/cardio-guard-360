-- CardioGuard 360 v1.4.0 ECG标注功能数据库脚本

-- ============================================
-- 1. ECG标注表
-- ============================================
CREATE TABLE IF NOT EXISTS `ecg_annotation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `analysis_result_id` BIGINT NOT NULL COMMENT '关联的ECG分析结果ID',
  `timestamp_ms` BIGINT NOT NULL COMMENT '标注点的时间戳(毫秒,相对于记录开始)',
  `annotation_type` VARCHAR(20) NOT NULL COMMENT '标注类型(P_WAVE- P波, QRS_COMPLEX- QRS波, T_WAVE- T波, ST_SEGMENT- ST段, ABNORMAL_POINT-异常点, OTHER-其他)',
  `label` VARCHAR(50) DEFAULT NULL COMMENT '标注标签/描述',
  `confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '标注置信度(0-1, AI自动标注时)',
  `is_ai_generated` TINYINT NOT NULL DEFAULT 0 COMMENT '是否AI自动生成(0-手动, 1-AI)',
  `created_by` BIGINT NOT NULL COMMENT '创建者ID(医生或用户)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_analysis_result_id` (`analysis_result_id`),
  KEY `idx_annotation_type` (`annotation_type`),
  KEY `idx_timestamp` (`timestamp_ms`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_annotation_result` FOREIGN KEY (`analysis_result_id`) REFERENCES `ecg_analysis_result` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ECG心电图标注表';

-- ============================================
-- 2. 标注模板表(常用标注预设)
-- ============================================
CREATE TABLE IF NOT EXISTS `annotation_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '模板名称',
  `annotation_type` VARCHAR(20) NOT NULL COMMENT '标注类型',
  `default_label` VARCHAR(50) DEFAULT NULL COMMENT '默认标签',
  `color` VARCHAR(20) DEFAULT '#409eff' COMMENT '显示颜色(十六进制)',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标标识',
  `description` TEXT COMMENT '模板描述',
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统预设(0-用户自定义, 1-系统预设)',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID(系统预设为NULL)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_annotation_type` (`annotation_type`),
  KEY `idx_is_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标注模板表';

-- ============================================
-- 3. 插入系统预设标注模板
-- ============================================
INSERT INTO `annotation_template` (`name`, `annotation_type`, `default_label`, `color`, `icon`, `description`, `is_system`) VALUES
('P波', 'P_WAVE', 'P', '#67c23a', 'circle', '心房去极化波', 1),
('QRS波群', 'QRS_COMPLEX', 'QRS', '#f56c6c', 'triangle', '心室去极化波群', 1),
('T波', 'T_WAVE', 'T', '#409eff', 'circle', '心室复极化波', 1),
('ST段', 'ST_SEGMENT', 'ST', '#e6a23c', 'line', 'ST段抬高/压低', 1),
('异常点', 'ABNORMAL_POINT', '异常', '#f56c6c', 'warning', '检测到的异常位置', 1),
('早搏', 'ABNORMAL_POINT', '早搏', '#f56c6c', 'warning', '期前收缩', 1),
('伪差', 'OTHER', '伪差', '#909399', 'close', '运动伪差或干扰', 1);

-- ============================================
-- 4. 插入测试标注数据
-- ============================================
INSERT INTO `ecg_annotation` 
(`user_id`, `analysis_result_id`, `timestamp_ms`, `annotation_type`, `label`, `confidence`, `is_ai_generated`, `created_by`) 
VALUES
-- 正常ECG的标注
(1, 1, 200, 'P_WAVE', 'P', 0.9500, 1, 100),
(1, 1, 280, 'QRS_COMPLEX', 'QRS', 0.9800, 1, 100),
(1, 1, 400, 'T_WAVE', 'T', 0.9200, 1, 100),
(1, 1, 1000, 'P_WAVE', 'P', 0.9400, 1, 100),
(1, 1, 1080, 'QRS_COMPLEX', 'QRS', 0.9700, 1, 100),
(1, 1, 1200, 'T_WAVE', 'T', 0.9300, 1, 100),

-- 房颤ECG的标注
(2, 2, 150, 'ABNORMAL_POINT', '不规则R-R间期', NULL, 0, 101),
(2, 2, 450, 'ABNORMAL_POINT', '缺失P波', NULL, 0, 101),
(2, 2, 750, 'ABNORMAL_POINT', '不规则R-R间期', NULL, 0, 101),

-- 医生手动标注
(3, 3, 320, 'QRS_COMPLEX', '宽大QRS', NULL, 0, 102),
(3, 3, 320, 'ABNORMAL_POINT', '室性早搏', NULL, 0, 102);

-- ============================================
-- 5. 查询验证
-- ============================================
SELECT 
    a.id,
    a.annotation_type,
    a.label,
    a.timestamp_ms,
    a.is_ai_generated,
    ar.diagnosis,
    arr.arrhythmia_type
FROM `ecg_annotation` a
JOIN `ecg_analysis_result` ar ON a.analysis_result_id = ar.id
LEFT JOIN (
    SELECT analysis_result_id, GROUP_CONCAT(annotation_type) as arr
    FROM `ecg_annotation`
    GROUP BY analysis_result_id
) arr ON a.analysis_result_id = arr.analysis_result_id
ORDER BY a.created_at DESC;

-- 查看标注模板
SELECT * FROM `annotation_template` WHERE is_system = 1;
