@echo off
REM CardioGuard 360 数据库优化执行脚本 (Windows版本)
REM 版本: v1.5.1
REM 日期: 2026-04-16

setlocal enabledelayedexpansion

echo ========================================
echo CardioGuard 360 数据库优化执行脚本
echo 版本: v1.5.1
echo ========================================
echo.

REM 配置
set DB_USER=root
set DB_NAME=cardioguard
set BACKUP_DIR=database\backups
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\backup_%TIMESTAMP%.sql

REM 步骤1: 创建备份目录
echo [步骤 1/5] 创建备份目录...
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
echo ✓ 备份目录已创建: %BACKUP_DIR%
echo.

REM 步骤2: 备份数据库
echo [步骤 2/5] 备份数据库...
echo 备份文件: %BACKUP_FILE%

mysqldump -u %DB_USER% ^
    --single-transaction ^
    --routines ^
    --triggers ^
    --events ^
    --hex-blob ^
    %DB_NAME% > "%BACKUP_FILE%"

if %errorlevel% neq 0 (
    echo ✗ 数据库备份失败！
    exit /b 1
)

for %%A in ("%BACKUP_FILE%") do set BACKUP_SIZE=%%~zA
echo ✓ 数据库备份成功！大小: %BACKUP_SIZE% bytes
echo.

REM 步骤3: 验证备份文件
echo [步骤 3/5] 验证备份文件...
if exist "%BACKUP_FILE%" (
    if %BACKUP_SIZE% gtr 0 (
        echo ✓ 备份文件验证通过！
    ) else (
        echo ✗ 备份文件为空！
        exit /b 1
    )
) else (
    echo ✗ 备份文件不存在！
    exit /b 1
)
echo.

REM 步骤4: 执行索引优化
echo [步骤 4/5] 执行索引优化...
echo 优化脚本: database\optimization_indexes.sql

if exist "database\optimization_indexes.sql" (
    mysql -u %DB_USER% %DB_NAME% < database\optimization_indexes.sql
    
    if %errorlevel% neq 0 (
        echo ✗ 索引优化执行失败！
        echo 提示: 可以手动执行以下命令查看详细错误
        echo mysql -u %DB_USER% %DB_NAME% ^< database\optimization_indexes.sql
        exit /b 1
    )
    echo ✓ 索引优化执行成功！
) else (
    echo ✗ 优化脚本文件不存在: database\optimization_indexes.sql
    exit /b 1
)
echo.

REM 步骤5: 验证索引创建
echo [步骤 5/5] 验证索引创建结果...
mysql -u %DB_USER% %DB_NAME% -e "SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = '%DB_NAME%' AND INDEX_NAME IN ('idx_role_status', 'idx_user_status', 'idx_user_created', 'idx_status_created', 'idx_result_type', 'idx_user_severity') ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;"

echo.
echo ========================================
echo ✓ 数据库优化完成！
echo ========================================
echo.
echo 后续步骤:
echo 1. 重启应用以加载新的索引配置
echo 2. 观察慢查询日志: tail -f logs/cardioguard.log | grep "慢查询检测"
echo 3. 使用 EXPLAIN 分析典型查询，验证索引使用情况
echo.
echo 回滚方案（如果需要）:
echo mysql -u %DB_USER% %DB_NAME% ^< %BACKUP_FILE%
echo.

pause
