#!/bin/bash

# CardioGuard 360 数据库优化执行脚本
# 版本: v1.5.1
# 日期: 2026-04-16

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
DB_USER="root"
DB_NAME="cardioguard"
BACKUP_DIR="./database/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.sql"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}CardioGuard 360 数据库优化执行脚本${NC}"
echo -e "${GREEN}版本: v1.5.1${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 步骤1: 创建备份目录
echo -e "${YELLOW}[步骤 1/5] 创建备份目录...${NC}"
mkdir -p "$BACKUP_DIR"
echo -e "${GREEN}✓ 备份目录已创建: ${BACKUP_DIR}${NC}"
echo ""

# 步骤2: 备份数据库
echo -e "${YELLOW}[步骤 2/5] 备份数据库...${NC}"
echo "备份文件: ${BACKUP_FILE}"

if command -v mysqldump &> /dev/null; then
    mysqldump -u "$DB_USER" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        "$DB_NAME" > "$BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        echo -e "${GREEN}✓ 数据库备份成功！大小: ${BACKUP_SIZE}${NC}"
    else
        echo -e "${RED}✗ 数据库备份失败！${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ mysqldump 命令未找到，请安装 MySQL 客户端工具${NC}"
    exit 1
fi
echo ""

# 步骤3: 验证备份文件
echo -e "${YELLOW}[步骤 3/5] 验证备份文件...${NC}"
if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
    LINE_COUNT=$(wc -l < "$BACKUP_FILE")
    echo -e "${GREEN}✓ 备份文件验证通过！行数: ${LINE_COUNT}${NC}"
else
    echo -e "${RED}✗ 备份文件验证失败！${NC}"
    exit 1
fi
echo ""

# 步骤4: 执行索引优化
echo -e "${YELLOW}[步骤 4/5] 执行索引优化...${NC}"
echo "优化脚本: database/optimization_indexes.sql"

if [ -f "database/optimization_indexes.sql" ]; then
    mysql -u "$DB_USER" "$DB_NAME" < database/optimization_indexes.sql
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 索引优化执行成功！${NC}"
    else
        echo -e "${RED}✗ 索引优化执行失败！${NC}"
        echo -e "${YELLOW}提示: 可以手动执行以下命令查看详细错误${NC}"
        echo "mysql -u $DB_USER $DB_NAME < database/optimization_indexes.sql"
        exit 1
    fi
else
    echo -e "${RED}✗ 优化脚本文件不存在: database/optimization_indexes.sql${NC}"
    exit 1
fi
echo ""

# 步骤5: 验证索引创建
echo -e "${YELLOW}[步骤 5/5] 验证索引创建结果...${NC}"

# 检查关键索引是否存在
mysql -u "$DB_USER" "$DB_NAME" -e "
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = '$DB_NAME'
    AND INDEX_NAME IN (
        'idx_role_status',
        'idx_user_status',
        'idx_user_created',
        'idx_status_created',
        'idx_result_type',
        'idx_user_severity'
    )
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✓ 数据库优化完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}后续步骤:${NC}"
echo "1. 重启应用以加载新的索引配置"
echo "2. 观察慢查询日志: tail -f logs/cardioguard.log | grep '慢查询检测'"
echo "3. 使用 EXPLAIN 分析典型查询，验证索引使用情况"
echo ""
echo -e "${YELLOW}回滚方案（如果需要）:${NC}"
echo "mysql -u $DB_USER $DB_NAME < $BACKUP_FILE"
echo ""
