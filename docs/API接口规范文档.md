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
- **API版本**: v1.3.2

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

### v1.3.2 (2026-04-14)

**新增功能:**
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

**文档版本**: v1.3.2  
**最后更新**: 2026-04-14  
**维护团队**: CardioGuard Development Team

</div>
