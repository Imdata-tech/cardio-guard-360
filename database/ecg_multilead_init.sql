-- CardioGuard 360 - 多导联ECG功能数据库初始化脚本
-- 版本: v1.5.0
-- 创建时间: 2026-04-16

-- 创建ECG导联配置表
CREATE TABLE IF NOT EXISTS `ecg_lead_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lead_name` VARCHAR(10) NOT NULL COMMENT '导联名称 (I, II, III, aVR, aVL, aVF, V1-V6)',
    `lead_type` VARCHAR(20) NOT NULL COMMENT '导联类型 (LIMB-肢体导联, PRECORDIAL-胸导联)',
    `display_color` VARCHAR(20) DEFAULT '#0000FF' COMMENT '显示颜色 (十六进制)',
    `display_order` INT DEFAULT 0 COMMENT '显示顺序',
    `is_default` TINYINT DEFAULT 1 COMMENT '是否默认显示 (0-否, 1-是)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lead_name` (`lead_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ECG导联配置表';

-- 插入标准12导联配置数据
INSERT INTO `ecg_lead_config` (`lead_name`, `lead_type`, `display_color`, `display_order`, `is_default`, `description`) VALUES
('I', 'LIMB', '#FF0000', 1, 1, '标准肢体导联 I'),
('II', 'LIMB', '#00FF00', 2, 1, '标准肢体导联 II'),
('III', 'LIMB', '#0000FF', 3, 1, '标准肢体导联 III'),
('aVR', 'LIMB', '#FFA500', 4, 0, '加压单极右上肢导联'),
('aVL', 'LIMB', '#800080', 5, 0, '加压单极左上肢导联'),
('aVF', 'LIMB', '#00FFFF', 6, 0, '加压单极左下肢导联'),
('V1', 'PRECORDIAL', '#FF1493', 7, 1, '胸导联 V1'),
('V2', 'PRECORDIAL', '#32CD32', 8, 1, '胸导联 V2'),
('V3', 'PRECORDIAL', '#FFD700', 9, 1, '胸导联 V3'),
('V4', 'PRECORDIAL', '#FF6347', 10, 1, '胸导联 V4'),
('V5', 'PRECORDIAL', '#4169E1', 11, 1, '胸导联 V5'),
('V6', 'PRECORDIAL', '#8A2BE2', 12, 1, '胸导联 V6');

-- 创建多导联ECG记录表
CREATE TABLE IF NOT EXISTS `ecg_multilead_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `record_name` VARCHAR(100) DEFAULT NULL COMMENT '记录名称',
    `lead_count` INT DEFAULT 12 COMMENT '导联数量 (标准12导联)',
    `sample_rate` INT DEFAULT 500 COMMENT '采样率 (Hz)',
    `duration_seconds` INT DEFAULT 0 COMMENT '记录时长 (秒)',
    `data_storage_id` VARCHAR(100) DEFAULT NULL COMMENT '数据存储ID (InfluxDB或其他时序数据库)',
    `status` VARCHAR(20) DEFAULT 'RECORDING' COMMENT '记录状态 (RECORDING-记录中, COMPLETED-已完成, ERROR-错误)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多导联ECG记录表';
