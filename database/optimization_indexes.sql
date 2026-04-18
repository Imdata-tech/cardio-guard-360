-- CardioGuard 360 数据库性能优化脚本 v1.5.1
-- 执行日期: 2026-04-16
-- 说明: 添加复合索引、优化查询性能

USE cardioguard;

-- ============================================
-- 1. 用户表索引优化
-- ============================================

-- 添加角色+状态复合索引（用于查询特定角色的活跃用户）
ALTER TABLE sys_user ADD INDEX idx_role_status (role, status);

-- 添加创建时间索引（用于按时间排序查询）
ALTER TABLE sys_user ADD INDEX idx_create_time (create_time);

-- ============================================
-- 2. 设备表索引优化
-- ============================================

-- 添加用户ID+状态复合索引（用于查询用户的在线设备）
ALTER TABLE device ADD INDEX idx_user_status (user_id, status);

-- 添加序列号唯一索引（确保设备唯一性）
ALTER TABLE device ADD UNIQUE INDEX uk_serial_number (serial_number);

-- 添加设备类型索引（用于按类型筛选设备）
ALTER TABLE device ADD INDEX idx_device_type (device_type);

-- ============================================
-- 3. ECG分析结果表索引优化
-- ============================================

-- 添加用户ID+创建时间复合索引（用于查询用户的ECG记录，按时间倒序）
ALTER TABLE ecg_analysis_result ADD INDEX idx_user_created (user_id, created_at DESC);

-- 添加状态+创建时间复合索引（用于查询待分析的ECG记录）
ALTER TABLE ecg_analysis_result ADD INDEX idx_status_created (status, created_at DESC);

-- ============================================
-- 4. ECG标注表索引优化
-- ============================================

-- 添加分析结果ID+标注类型复合索引（用于查询特定类型的标注）
ALTER TABLE ecg_annotation ADD INDEX idx_result_type (analysis_result_id, annotation_type);

-- 添加创建者+创建时间复合索引（用于查询医生的标注历史）
ALTER TABLE ecg_annotation ADD INDEX idx_user_created (created_by, created_at DESC);

-- ============================================
-- 5. 多导联记录表索引确认
-- ============================================

-- 以下索引已在ecg_multilead_init.sql中创建，此处仅做验证
-- idx_user_id (user_id)
-- idx_device_id (device_id)
-- idx_created_at (created_at)
-- idx_status (status)

-- 如果需要进一步优化，可以添加复合索引
ALTER TABLE ecg_multilead_record ADD INDEX idx_user_status_created (user_id, status, created_at DESC);

-- ============================================
-- 6. 健康告警表索引优化
-- ============================================

-- 添加用户ID+严重程度+创建时间复合索引（用于查询用户的高优先级告警）
ALTER TABLE health_alert ADD INDEX idx_user_severity (user_id, severity, created_at DESC);

-- 添加状态+创建时间复合索引（用于查询未处理的告警）
ALTER TABLE health_alert ADD INDEX idx_status_created (status, created_at DESC);

-- ============================================
-- 7. 心率数据表索引优化（如果存在）
-- ============================================

-- 检查heart_rate_data表是否存在
-- 如果存在，添加以下索引
-- ALTER TABLE heart_rate_data ADD INDEX idx_user_time (user_id, recorded_at DESC);
-- ALTER TABLE heart_rate_data ADD INDEX idx_device_time (device_id, recorded_at DESC);

-- ============================================
-- 8. 索引统计信息更新
-- ============================================

-- 更新所有表的统计信息，帮助优化器选择更好的执行计划
ANALYZE TABLE sys_user;
ANALYZE TABLE device;
ANALYZE TABLE ecg_analysis_result;
ANALYZE TABLE ecg_annotation;
ANALYZE TABLE ecg_lead_config;
ANALYZE TABLE ecg_multilead_record;
ANALYZE TABLE health_alert;

-- ============================================
-- 9. 验证索引创建结果
-- ============================================

-- 查看所有表的索引
SHOW INDEX FROM sys_user;
SHOW INDEX FROM device;
SHOW INDEX FROM ecg_analysis_result;
SHOW INDEX FROM ecg_annotation;
SHOW INDEX FROM ecg_multilead_record;
SHOW INDEX FROM health_alert;

-- ============================================
-- 优化完成提示
-- ============================================

SELECT '数据库索引优化完成！' AS message;
SELECT '新增索引数量: 12个' AS summary;
SELECT '建议: 执行EXPLAIN分析慢查询，验证索引使用情况' AS recommendation;
