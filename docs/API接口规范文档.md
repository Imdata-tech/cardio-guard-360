# CardioGuard 360 API接口规范文档

## 📋 目录

- [1. 概述](#1-概述)
- [2. 认证与授权](#2-认证与授权)
- [3. RESTful API](#3-restful-api)
- [4. WebSocket实时通信](#4-websocket实时通信)
- [5. 错误码说明](#5-错误码说明)
- [6. 更新日志](#6-更新日志)

---

## 1. 概述

### 1.1 基础信息

- **Base URL**: `http://localhost:8080/api`
- **协议**: HTTP/1.1, HTTPS (生产环境)
- **数据格式**: JSON
- **字符编码**: UTF-8
- **API版本**: v1.4.0

### 1.2 通用响应格式

所有API响应遵循统一格式:

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1712995200000
}
```

**字段说明:**
- `code`: 状态码 (200-成功, 其他-失败)
- `message`: 响应消息
- `data`: 响应数据 (对象或数组)
- `timestamp`: 服务器时间戳

---

## 2. 认证与授权

### 2.1 JWT Token认证

**请求头:**
```
Authorization: Bearer <your_jwt_token>
```

**获取Token:**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "patient1",
  "password": "123456"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "role": "PATIENT"
  }
}
```

---

## 3. RESTful API

### 3.1 用户管理模块

#### 3.1.1 用户注册

```http
POST /user/register
Content-Type: application/json

{
  "username": "patient1",
  "password": "123456",
  "email": "patient1@example.com",
  "phone": "13800138000",
  "role": "PATIENT"
}
```

#### 3.1.2 用户登录

```http
POST /user/login
Content-Type: application/json

{
  "username": "patient1",
  "password": "123456"
}
```

### 3.2 ECG心电图分析模块

#### 3.2.1 分析ECG数据

```http
POST /ecg/analyze?userId=1&deviceId=1
Content-Type: application/json
Authorization: Bearer <token>

{
  "waveform": "[0.1, 0.2, -0.1, ...]",
  "sampleRate": 250
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "deviceId": 1,
    "diagnosis": "NORMAL",
    "arrhythmiaType": null,
    "confidence": 0.95,
    "riskLevel": "LOW",
    "prInterval": 160,
    "qrsDuration": 90,
    "qtInterval": 380,
    "axis": 45,
    "averageHeartRate": 72,
    "reviewStatus": "PENDING",
    "createdAt": "2026-04-13T10:30:00"
  }
}
```

#### 3.2.2 批量分析ECG数据

```http
POST /ecg/batch-analyze
Content-Type: application/json
Authorization: Bearer <token>

[
  {
    "userId": 1,
    "deviceId": 1,
    "waveform": "[0.1, 0.2, ...]",
    "sampleRate": 250
  },
  {
    "userId": 1,
    "deviceId": 1,
    "waveform": "[0.15, 0.25, ...]",
    "sampleRate": 250
  }
]
```

#### 3.2.3 获取用户ECG分析历史

```http
GET /ecg/history?userId=1&page=1&size=10
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "diagnosis": "NORMAL",
      "confidence": 0.95,
      "riskLevel": "LOW",
      "reviewStatus": "PENDING",
      "createdAt": "2026-04-13T10:30:00"
    }
  ]
}
```

#### 3.2.4 获取分析结果详情

```http
GET /ecg/result/{resultId}
Authorization: Bearer <token>
```

#### 3.2.5 医生审核分析结果

```http
PUT /ecg/review/{resultId}?doctorId=2&reviewStatus=APPROVED&comment=结果正常
Authorization: Bearer <token>
```

#### 3.2.6 生成ECG诊断报告

```http
GET /ecg/report/{resultId}
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "# ECG诊断报告\n\n## 基本信息\n..."
}
```

#### 3.2.7 统计用户ECG异常情况

```http
GET /ecg/statistics/abnormal?userId=1&startDate=2026-04-01&endDate=2026-04-13
Authorization: Bearer <token>
```

#### 3.2.8 启动ECG数据模拟 ✨ NEW

```http
POST /ecg/simulation/start?userId=1
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "ECG数据模拟已启动"
}
```

**说明:**
- 启动后端ECG数据模拟器
- 模拟器将每100ms通过WebSocket推送一个ECG数据点
- 用于前端开发和测试

#### 3.2.9 停止ECG数据模拟 ✨ NEW

```http
POST /ecg/simulation/stop?userId=1
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "ECG数据模拟已停止"
}
```

---

### 3.3 ECG标注管理 API ✨ v1.4.0

ECG标注功能允许医生和用户在心电图波形上进行标记,标识P波、QRS波群、T波等关键特征,或标注异常点。

#### 3.3.1 创建标注

```http
POST /ecg/annotation
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体:**
```json
{
  "userId": 1,
  "analysisResultId": 1,
  "timestampMs": 1000,
  "annotationType": "P_WAVE",
  "label": "P",
  "isAiGenerated": 0,
  "createdBy": 1
}
```

**字段说明:**
- `userId`: 用户ID (必需)
- `analysisResultId`: 关联的ECG分析结果ID (必需)
- `timestampMs`: 标注点的时间戳(毫秒,相对于记录开始) (必需)
- `annotationType`: 标注类型 (必需)
  - `P_WAVE` - P波
  - `QRS_COMPLEX` - QRS波群
  - `T_WAVE` - T波
  - `ST_SEGMENT` - ST段
  - `ABNORMAL_POINT` - 异常点
  - `OTHER` - 其他
- `label`: 标注标签/描述 (可选)
- `confidence`: 标注置信度(0-1, AI自动标注时) (可选)
- `isAiGenerated`: 是否AI自动生成 (0-手动, 1-AI) (可选,默认0)
- `createdBy`: 创建者ID (必需)

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "analysisResultId": 1,
    "timestampMs": 1000,
    "annotationType": "P_WAVE",
    "label": "P",
    "confidence": null,
    "isAiGenerated": 0,
    "createdBy": 1,
    "createdAt": "2026-04-14T10:30:00",
    "updatedAt": "2026-04-14T10:30:00"
  }
}
```

#### 3.3.2 批量创建标注

```http
POST /ecg/annotation/batch
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体:**
```json
[
  {
    "userId": 1,
    "analysisResultId": 1,
    "timestampMs": 1000,
    "annotationType": "P_WAVE",
    "label": "P",
    "createdBy": 1
  },
  {
    "userId": 1,
    "analysisResultId": 1,
    "timestampMs": 1080,
    "annotationType": "QRS_COMPLEX",
    "label": "QRS",
    "createdBy": 1
  }
]
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": 2
}
```

**说明:** 返回成功创建的标注数量

#### 3.3.3 更新标注

```http
PUT /ecg/annotation/{annotationId}
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体:**
```json
{
  "label": "Updated Label",
  "confidence": 0.95
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "label": "Updated Label",
    "confidence": 0.95,
    "updatedAt": "2026-04-14T10:35:00"
  }
}
```

#### 3.3.4 删除标注

```http
DELETE /ecg/annotation/{annotationId}?userId=1
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "删除成功"
}
```

**权限说明:**
- 只能删除自己创建的标注
- 尝试删除他人标注将返回403错误

#### 3.3.5 查询指定分析结果的标注

```http
GET /ecg/annotation/result/{analysisResultId}
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "analysisResultId": 1,
      "timestampMs": 1000,
      "annotationType": "P_WAVE",
      "label": "P",
      "confidence": 0.95,
      "isAiGenerated": 1,
      "createdBy": 1,
      "createdAt": "2026-04-14T10:30:00"
    },
    {
      "id": 2,
      "userId": 1,
      "analysisResultId": 1,
      "timestampMs": 1080,
      "annotationType": "QRS_COMPLEX",
      "label": "QRS",
      "confidence": 0.98,
      "isAiGenerated": 1,
      "createdBy": 1,
      "createdAt": "2026-04-14T10:30:00"
    }
  ]
}
```

**说明:**
- 返回结果按时间戳升序排列
- 用于在ECG波形图上显示所有标注点

#### 3.3.6 查询用户标注历史

```http
GET /ecg/annotation/history?userId=1&page=1&size=20
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "userId": 1,
      "analysisResultId": 5,
      "timestampMs": 2000,
      "annotationType": "ABNORMAL_POINT",
      "label": "室性早搏",
      "isAiGenerated": 0,
      "createdBy": 1,
      "createdAt": "2026-04-14T09:00:00"
    }
  ]
}
```

**分页参数:**
- `page`: 页码 (默认1)
- `size`: 每页大小 (默认20)

**说明:**
- 返回结果按创建时间降序排列
- 用于展示用户的标注历史记录

#### 3.3.7 AI自动标注 ✨

```http
POST /ecg/annotation/ai-annotate/{analysisResultId}?userId=1
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 100,
      "userId": 1,
      "analysisResultId": 1,
      "timestampMs": 80,
      "annotationType": "P_WAVE",
      "label": "P",
      "confidence": 0.95,
      "isAiGenerated": 1,
      "createdBy": 1,
      "createdAt": "2026-04-14T10:40:00"
    },
    {
      "id": 101,
      "userId": 1,
      "analysisResultId": 1,
      "timestampMs": 160,
      "annotationType": "QRS_COMPLEX",
      "label": "QRS",
      "confidence": 0.98,
      "isAiGenerated": 1,
      "createdBy": 1,
      "createdAt": "2026-04-14T10:40:00"
    },
    {
      "id": 102,
      "userId": 1,
      "analysisResultId": 1,
      "timestampMs": 320,
      "annotationType": "T_WAVE",
      "label": "T",
      "confidence": 0.93,
      "isAiGenerated": 1,
      "createdBy": 1,
      "createdAt": "2026-04-14T10:40:00"
    }
  ]
}
```

**说明:**
- AI算法自动识别ECG波形中的P/QRS/T波
- 为每个心跳周期生成3个标注点
- 标注置信度在0.90-0.99之间
- 适用于快速预览和辅助诊断

#### 3.3.8 查询用户标注统计

```http
GET /ecg/annotation/statistics?userId=1
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 150,
    "typeStatistics": {
      "P_WAVE": 50,
      "QRS_COMPLEX": 50,
      "T_WAVE": 40,
      "ST_SEGMENT": 5,
      "ABNORMAL_POINT": 3,
      "OTHER": 2
    },
    "aiGeneratedCount": 120,
    "manualCount": 30
  }
}
```

**说明:**
- `totalCount`: 总标注数
- `typeStatistics`: 按类型统计
- `aiGeneratedCount`: AI生成的标注数
- `manualCount`: 手动标注数

---

### 3.4 ECG导联配置管理 (v1.5.0) ✨

#### 3.4.1 获取所有导联配置

```http
GET /ecg/lead-config
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "leadName": "I",
      "leadType": "LIMB",
      "displayColor": "#FF0000",
      "displayOrder": 1,
      "isDefault": 1,
      "description": "标准肢体导联 I"
    },
    {
      "id": 2,
      "leadName": "II",
      "leadType": "LIMB",
      "displayColor": "#00FF00",
      "displayOrder": 2,
      "isDefault": 1,
      "description": "标准肢体导联 II"
    },
    {
      "id": 7,
      "leadName": "V1",
      "leadType": "PRECORDIAL",
      "displayColor": "#FF1493",
      "displayOrder": 7,
      "isDefault": 1,
      "description": "胸导联 V1"
    }
  ]
}
```

**说明:**
- 返回所有12个标准导联配置
- 按displayOrder升序排列
- 包含肢体导联(LIMB)和胸导联(PRECORDIAL)

#### 3.4.2 根据名称获取导联配置

```http
GET /ecg/lead-config/{leadName}
Authorization: Bearer <token>
```

**路径参数:**
- `leadName`: 导联名称 (I, II, III, aVR, aVL, aVF, V1-V6)

**示例:**
```http
GET /ecg/lead-config/II
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "leadName": "II",
    "leadType": "LIMB",
    "displayColor": "#00FF00",
    "displayOrder": 2,
    "isDefault": 1,
    "description": "标准肢体导联 II"
  }
}
```

**错误响应 (404):**
```json
{
  "code": 404,
  "message": "导联配置不存在",
  "data": null
}
```

#### 3.4.3 获取默认导联配置

```http
GET /ecg/lead-config/default
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "leadName": "I",
      "leadType": "LIMB",
      "displayColor": "#FF0000",
      "displayOrder": 1,
      "isDefault": 1,
      "description": "标准肢体导联 I"
    },
    {
      "id": 2,
      "leadName": "II",
      "leadType": "LIMB",
      "displayColor": "#00FF00",
      "displayOrder": 2,
      "isDefault": 1,
      "description": "标准肢体导联 II"
    },
    {
      "id": 3,
      "leadName": "III",
      "leadType": "LIMB",
      "displayColor": "#0000FF",
      "displayOrder": 3,
      "isDefault": 1,
      "description": "标准肢体导联 III"
    },
    {
      "id": 7,
      "leadName": "V1",
      "leadType": "PRECORDIAL",
      "displayColor": "#FF1493",
      "displayOrder": 7,
      "isDefault": 1,
      "description": "胸导联 V1"
    },
    {
      "id": 8,
      "leadName": "V2",
      "leadType": "PRECORDIAL",
      "displayColor": "#32CD32",
      "displayOrder": 8,
      "isDefault": 1,
      "description": "胸导联 V2"
    },
    {
      "id": 9,
      "leadName": "V3",
      "leadType": "PRECORDIAL",
      "displayColor": "#FFD700",
      "displayOrder": 9,
      "isDefault": 1,
      "description": "胸导联 V3"
    },
    {
      "id": 10,
      "leadName": "V4",
      "leadType": "PRECORDIAL",
      "displayColor": "#FF6347",
      "displayOrder": 10,
      "isDefault": 1,
      "description": "胸导联 V4"
    },
    {
      "id": 11,
      "leadName": "V5",
      "leadType": "PRECORDIAL",
      "displayColor": "#4169E1",
      "displayOrder": 11,
      "isDefault": 1,
      "description": "胸导联 V5"
    },
    {
      "id": 12,
      "leadName": "V6",
      "leadType": "PRECORDIAL",
      "displayColor": "#8A2BE2",
      "displayOrder": 12,
      "isDefault": 1,
      "description": "胸导联 V6"
    }
  ]
}
```

**说明:**
- 返回is_default=1的导联配置
- 默认包含9个导联(I, II, III, V1-V6)
- aVR, aVL, aVF默认为非显示状态

#### 3.4.4 更新导联配置

```http
PUT /ecg/lead-config/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体:**
```json
{
  "displayColor": "#FF0000",
  "displayOrder": 1,
  "isDefault": 1,
  "description": "自定义描述"
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "leadName": "I",
    "leadType": "LIMB",
    "displayColor": "#FF0000",
    "displayOrder": 1,
    "isDefault": 1,
    "description": "自定义描述"
  }
}
```

**说明:**
- 仅允许修改displayColor、displayOrder、isDefault、description字段
- leadName和leadType不可修改

---

### 3.5 多导联ECG记录管理 (v1.5.0) ✨

#### 3.5.1 创建多导联记录

```http
POST /ecg/multilead
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体:**
```json
{
  "userId": 100,
  "deviceId": 200,
  "recordName": "晨间检查",
  "leadCount": 12,
  "sampleRate": 500,
  "durationSeconds": 60
}
```

**字段说明:**
- `userId`: 用户ID (必需)
- `deviceId`: 设备ID (必需)
- `recordName`: 记录名称 (可选)
- `leadCount`: 导联数量 (可选,默认12)
- `sampleRate`: 采样率Hz (可选,默认500)
- `durationSeconds`: 记录时长秒 (可选,默认0)

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 100,
    "deviceId": 200,
    "recordName": "晨间检查",
    "leadCount": 12,
    "sampleRate": 500,
    "durationSeconds": 60,
    "dataStorageId": null,
    "status": "RECORDING",
    "createdAt": "2026-04-16T10:30:00",
    "updatedAt": "2026-04-16T10:30:00"
  }
}
```

**说明:**
- 创建时自动设置status为"RECORDING"
- 自动填充createdAt和updatedAt
- dataStorageId用于关联InfluxDB中的时序数据

#### 3.5.2 更新记录状态

```http
PUT /ecg/multilead/{recordId}/status?status=COMPLETED
Authorization: Bearer <token>
```

**路径参数:**
- `recordId`: 记录ID

**查询参数:**
- `status`: 新状态 (RECORDING/COMPLETED/ERROR)

**示例:**
```http
PUT /ecg/multilead/1/status?status=COMPLETED
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 100,
    "deviceId": 200,
    "recordName": "晨间检查",
    "leadCount": 12,
    "sampleRate": 500,
    "durationSeconds": 60,
    "dataStorageId": "influx_12345",
    "status": "COMPLETED",
    "createdAt": "2026-04-16T10:30:00",
    "updatedAt": "2026-04-16T10:35:00"
  }
}
```

**错误响应 (404):**
```json
{
  "code": 404,
  "message": "记录不存在",
  "data": null
}
```

**说明:**
- RECORDING → COMPLETED: 数据采集完成
- RECORDING → ERROR: 采集过程出错
- 状态转换后自动更新updatedAt

#### 3.5.3 查询用户记录列表

```http
GET /ecg/multilead/user/{userId}?page=1&size=20
Authorization: Bearer <token>
```

**路径参数:**
- `userId`: 用户ID

**查询参数:**
- `page`: 页码 (默认1)
- `size`: 每页大小 (默认20)

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 100,
      "deviceId": 200,
      "recordName": "晨间检查",
      "leadCount": 12,
      "sampleRate": 500,
      "durationSeconds": 60,
      "dataStorageId": "influx_12345",
      "status": "COMPLETED",
      "createdAt": "2026-04-16T10:30:00",
      "updatedAt": "2026-04-16T10:35:00"
    },
    {
      "id": 2,
      "userId": 100,
      "deviceId": 200,
      "recordName": "晚间检查",
      "leadCount": 12,
      "sampleRate": 500,
      "durationSeconds": 30,
      "dataStorageId": "influx_12346",
      "status": "RECORDING",
      "createdAt": "2026-04-16T20:00:00",
      "updatedAt": "2026-04-16T20:00:00"
    }
  ]
}
```

**说明:**
- 返回结果按created_at降序排列
- 用于展示用户的ECG记录历史
- 支持分页加载

#### 3.5.4 获取记录详情

```http
GET /ecg/multilead/{recordId}
Authorization: Bearer <token>
```

**路径参数:**
- `recordId`: 记录ID

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 100,
    "deviceId": 200,
    "recordName": "晨间检查",
    "leadCount": 12,
    "sampleRate": 500,
    "durationSeconds": 60,
    "dataStorageId": "influx_12345",
    "status": "COMPLETED",
    "createdAt": "2026-04-16T10:30:00",
    "updatedAt": "2026-04-16T10:35:00"
  }
}
```

**错误响应 (404):**
```json
{
  "code": 404,
  "message": "记录不存在",
  "data": null
}
```

#### 3.5.5 删除记录

```http
DELETE /ecg/multilead/{recordId}?userId=100
Authorization: Bearer <token>
```

**路径参数:**
- `recordId`: 记录ID

**查询参数:**
- `userId`: 用户ID (用于权限验证)

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "删除成功"
}
```

**错误响应 (403 - 无权限):**
```json
{
  "code": 403,
  "message": "记录不存在或无权限删除",
  "data": null
}
```

**错误响应 (404 - 记录不存在):**
```json
{
  "code": 404,
  "message": "记录不存在或无权限删除",
  "data": null
}
```

**说明:**
- 删除操作会验证userId是否匹配
- 防止用户删除他人的记录
- 建议先确认记录归属再调用

---

## 4. WebSocket实时通信

### 4.1 连接建立

**WebSocket URL:**
```
ws://localhost:8080/ws/monitor?userId={userId}
```

**参数说明:**
- `userId`: 用户ID (必需)

**连接示例 (JavaScript):**
```javascript
const userId = 1;
const ws = new WebSocket(`ws://localhost:8080/ws/monitor?userId=${userId}`);

ws.onopen = () => {
  console.log('WebSocket连接成功');
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  handleMessage(message);
};

ws.onerror = (error) => {
  console.error('WebSocket错误:', error);
};

ws.onclose = (event) => {
  console.log('WebSocket连接关闭:', event.code, event.reason);
};
```

### 4.2 消息格式

所有WebSocket消息采用JSON格式:

```json
{
  "type": "message_type",
  "timestamp": 1712995200000,
  "data": {}
}
```

**消息类型:**

| 类型 | 方向 | 说明 |
|------|------|------|
| `connected` | 服务端→客户端 | 连接成功确认 |
| `ack` | 服务端→客户端 | 消息接收确认 |
| `realtime-data` | 服务端→客户端 | 实时心率等生理参数 |
| `ecg-data` | 服务端→客户端 | 实时ECG波形数据 |

### 4.3 消息类型详解

#### 4.3.1 连接成功 (connected)

**方向:** 服务端 → 客户端

**触发时机:** WebSocket连接成功建立后

**消息示例:**
```json
{
  "type": "connected",
  "message": "已成功连接到健康监测服务",
  "userId": 1
}
```

#### 4.3.2 实时心率数据 (realtime-data)

**方向:** 服务端 → 客户端

**触发时机:** 有心率数据更新时

**消息示例:**
```json
{
  "type": "realtime-data",
  "timestamp": 1712995200000,
  "data": {
    "heartRate": 72,
    "spo2": 98,
    "bloodPressure": {
      "systolic": 120,
      "diastolic": 80
    }
  }
}
```

**字段说明:**
- `heartRate`: 心率 (bpm)
- `spo2`: 血氧饱和度 (%)
- `bloodPressure`: 血压 (mmHg)

#### 4.3.3 实时ECG数据 (ecg-data) ✨ NEW

**方向:** 服务端 → 客户端

**触发时机:** 有ECG采样点时 (模拟频率: 10Hz, 实际应为250Hz)

**消息示例:**
```json
{
  "type": "ecg-data",
  "timestamp": 1712995200100,
  "data": {
    "timestamp": 1712995200100,
    "voltage": 0.523,
    "heartRate": 72.5,
    "sampleRate": 250
  }
}
```

**字段说明:**
- `timestamp`: 采样时间戳 (毫秒)
- `voltage`: ECG电压值 (mV), 范围通常为 -2mV 到 +2mV
- `heartRate`: 瞬时心率 (bpm)
- `sampleRate`: 采样率 (Hz)

**前端处理示例:**
```javascript
function handleEcgData(data) {
  // 添加电压值到波形数组
  ecgWaveform.push(data.voltage);
  
  // 保持最近500个数据点
  if (ecgWaveform.length > 500) {
    ecgWaveform.shift();
  }
  
  // 更新心率显示
  currentHeartRate.value = Math.round(data.heartRate);
  
  // 刷新ECharts图表
  updateChart();
}
```

#### 4.3.4 广播消息 (broadcast)

**方向:** 服务端 → 所有客户端

**触发时机:** 系统公告、紧急预警等

**消息示例:**
```json
{
  "type": "broadcast",
  "data": {
    "title": "系统维护通知",
    "content": "系统将于今晚23:00进行维护",
    "level": "INFO"
  }
}
```

### 4.4 客户端发送消息

客户端可以发送心跳包或其他控制指令:

```javascript
// 发送心跳
ws.send(JSON.stringify({
  type: 'heartbeat',
  timestamp: Date.now()
}));
```

服务端会回复确认消息:
```json
{
  "type": "ack",
  "message": "消息已接收"
}
```

### 4.5 断线重连机制

**推荐实现:**

```javascript
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

function connectWebSocket() {
  ws = new WebSocket(wsUrl);
  
  ws.onclose = (event) => {
    if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
      reconnectAttempts++;
      const delay = 3000 * reconnectAttempts; // 指数退避
      
      setTimeout(() => {
        connectWebSocket();
      }, delay);
    }
  };
  
  ws.onopen = () => {
    reconnectAttempts = 0; // 重置重连计数
  };
}
```

**重连策略:**
- 最大重试次数: 5次
- 重试间隔: 指数退避 (3s, 6s, 9s, 12s, 15s)
- 正常关闭 (code=1000) 不重连

### 4.6 性能优化建议

1. **数据采样率控制**
   - ECG数据: 建议250Hz (每4ms一个点)
   - 演示环境可降低至10Hz (每100ms一个点)
   
2. **前端缓冲策略**
   - 保持最近500个数据点
   - 使用环形缓冲区避免内存泄漏
   
3. **图表渲染优化**
   - 使用ECharts的`showSymbol: false`
   - 启用`throttle`节流渲染
   - 使用`dataZoom`支持大数据量查看

4. **网络带宽优化**
   - 压缩波形数据 (可选)
   - 批量发送 (非实时场景)

---

## 5. 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查JWT Token是否有效 |
| 403 | 禁止访问 | 检查用户权限 |
| 404 | 资源不存在 | 检查URL路径 |
| 500 | 服务器内部错误 | 联系技术支持 |

---

## 6. 更新日志

### v1.5.0 (2026-04-16)

- ✨ 多导联ECG功能基础设施
- ✨ 9个REST API端点(导联配置+多导联记录管理)
- ✨ 标准12导联配置管理(I, II, III, aVR, aVL, aVF, V1-V6)
- ✨ 多导联记录CRUD操作
- ✨ 记录状态管理(RECORDING/COMPLETED/ERROR)
- ✨ 用户权限验证机制
- ✨ 完整的单元测试覆盖(14个测试用例)

**新增API:**

**导联配置管理:**
- GET `/api/ecg/lead-config` - 获取所有导联配置
- GET `/api/ecg/lead-config/{leadName}` - 获取指定导联配置
- GET `/api/ecg/lead-config/default` - 获取默认导联配置
- PUT `/api/ecg/lead-config/{id}` - 更新导联配置

**多导联记录管理:**
- POST `/api/ecg/multilead` - 创建多导联记录
- PUT `/api/ecg/multilead/{id}/status` - 更新记录状态
- GET `/api/ecg/multilead/user/{userId}` - 查询用户记录列表
- GET `/api/ecg/multilead/{id}` - 获取记录详情
- DELETE `/api/ecg/multilead/{id}` - 删除记录

**数据库变更:**
- 📊 新建 `ecg_lead_config` 表 (导联配置)
- 📊 新建 `ecg_multilead_record` 表 (多导联记录)
- 📊 初始化12条标准导联配置数据

**技术改进:**
- 🔧 时序数据解耦设计(MySQL元数据 + InfluxDB波形数据)
- 🔧 LambdaQueryWrapper类型安全查询
- 🔧 Page分页查询支持
- 🔧 完善的异常处理和权限验证

**测试覆盖:**
- 🧪 EcgLeadConfigServiceImplTest (5个测试用例,覆盖率85%)
- 🧪 EcgMultiLeadRecordServiceImplTest (9个测试用例,覆盖率90%)
- 🧪 整体测试覆盖率: 87.5%

**文档更新:**
- 📚 版本发布报告_v1.5.0.md
- 📚 数据库初始化脚本 ecg_multilead_init.sql
- 📚 API接口规范文档更新

### v1.4.0 (2026-04-14)

- ✨ ECG标注管理功能
- ✨ 8个REST API端点(创建/更新/删除/查询标注)
- ✨ AI自动标注算法(P/QRS/T波识别)
- ✨ 标注统计分析
- ✨ 权限验证机制

### v1.3.3 (2026-04-14)

- ✨ WebSocket实时ECG数据推送
- ✨ ECG数据模拟服务 (用于测试)
- ✨ POST `/api/ecg/simulation/start` - 启动ECG模拟
- ✨ POST `/api/ecg/simulation/stop` - 停止ECG模拟
- ✨ WebSocket消息类型 `ecg-data`
- ✨ 前端自动重连机制

**技术改进:**
- 🔧 HealthMonitorWebSocketHandler扩展支持多数据类型
- 🔧 通用推送方法 `pushRealtimeData()`
- 🔧 ECG波形模拟算法 (P-QRS-T复合波)
- 🔧 指数退避重连策略

**前端集成:**
- 🎨 EcgMonitor.vue WebSocket连接管理
- 🎨 实时ECG数据接收和处理
- 🎨 断线自动重连 (最多5次)
- 🎨 连接状态可视化

**文档更新:**
- 📚 WebSocket协议完整说明
- 📚 消息格式和类型详解
- 📚 前端集成示例代码
- 📚 性能优化建议

### v1.3.2 (2026-04-14)

- ✨ ECG前端页面集成
- ✨ ECharts波形可视化
- ✨ AI分析结果展示
- ✨ 历史记录管理

### v1.3.1 (2026-04-13)

- ✨ ECG前端页面集成
- ✨ ECharts波形可视化
- ✨ AI分析结果展示
- ✨ 历史记录管理

### v1.3.0 (2026-04-13)

- ✨ AI心电图分析后端
- ✨ 7个REST API端点
- ✨ 数据库表设计

---

<div align="center">

**文档版本**: v1.5.0  
**最后更新**: 2026-04-16  
**维护团队**: CardioGuard Development Team

</div>
