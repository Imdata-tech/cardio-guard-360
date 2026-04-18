-- CardioGuard 360 索引验证脚本
-- 用于验证优化后的索引是否正常工作

USE cardioguard;

-- ============================================
-- 1. 查看所有新增的索引
-- ============================================

SELECT 
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    SEQ_IN_INDEX AS '列顺序',
    INDEX_TYPE AS '索引类型'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'cardioguard'
    AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 2. 验证用户表索引
-- ============================================

-- 测试 idx_role_status 索引
EXPLAIN SELECT * FROM sys_user WHERE role = 'PATIENT' AND status = 1;
-- 预期: key = idx_role_status

-- 测试 idx_create_time 索引
EXPLAIN SELECT * FROM sys_user ORDER BY create_time DESC LIMIT 10;
-- 预期: key = idx_create_time

-- ============================================
-- 3. 验证设备表索引
-- ============================================

-- 测试 idx_user_status 索引
EXPLAIN SELECT * FROM device WHERE user_id = 3 AND status = 1;
-- 预期: key = idx_user_status

-- 测试 uk_serial_number 唯一索引
EXPLAIN SELECT * FROM device WHERE serial_number = 'ECG20240001';
-- 预期: key = uk_serial_number

-- ============================================
-- 4. 验证ECG分析结果表索引
-- ============================================

-- 测试 idx_user_created 索引
EXPLAIN SELECT * FROM ecg_analysis_result WHERE user_id = 100 ORDER BY created_at DESC LIMIT 20;
-- 预期: key = idx_user_created

-- 测试 idx_status_created 索引
EXPLAIN SELECT * FROM ecg_analysis_result WHERE status = 'PENDING' ORDER BY created_at DESC;
-- 预期: key = idx_status_created

-- ============================================
-- 5. 验证ECG标注表索引
-- ============================================

-- 测试 idx_result_type 索引
EXPLAIN SELECT * FROM ecg_annotation WHERE analysis_result_id = 1 AND annotation_type = 'QRS';
-- 预期: key = idx_result_type

-- 测试 idx_user_created 索引（标注表）
EXPLAIN SELECT * FROM ecg_annotation WHERE created_by = 2 ORDER BY created_at DESC LIMIT 20;
-- 预期: key = idx_user_created

-- ============================================
-- 6. 验证多导联记录表索引
-- ============================================

-- 测试 idx_user_status_created 复合索引
EXPLAIN SELECT * FROM ecg_multilead_record WHERE user_id = 100 AND status = 'COMPLETED' ORDER BY created_at DESC LIMIT 20;
-- 预期: key = idx_user_status_created

-- ============================================
-- 7. 验证健康告警表索引
-- ============================================

-- 测试 idx_user_severity 索引
EXPLAIN SELECT * FROM health_alert WHERE user_id = 100 AND severity = 'HIGH' ORDER BY created_at DESC;
-- 预期: key = idx_user_severity

-- 测试 idx_status_created 索引
EXPLAIN SELECT * FROM health_alert WHERE status = 'UNREAD' ORDER BY created_at DESC;
-- 预期: key = idx_status_created

-- ============================================
-- 8. 索引使用统计
-- ============================================

SELECT 
    TABLE_NAME AS '表名',
    COUNT(DISTINCT INDEX_NAME) AS '索引数量'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'cardioguard'
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

-- ============================================
-- 9. 表大小统计
-- ============================================

SELECT 
    TABLE_NAME AS '表名',
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS '数据大小(MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS '索引大小(MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS '总大小(MB)',
    TABLE_ROWS AS '行数'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'cardioguard'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- ============================================
-- 验证完成提示
-- ============================================

SELECT '索引验证完成！请检查上述EXPLAIN输出，确认key列使用了预期的索引。' AS message;
