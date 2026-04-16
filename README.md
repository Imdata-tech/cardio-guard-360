# CardioGuard 360 - 实时心血管健康监护平台

<div align="center">

![Version](https://img.shields.io/badge/version-1.5.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-cyan.svg)
![License](https://img.shields.io/badge/license-MIT-yellow.svg)

**基于 Spring Boot + Vue 3 的实时心血管健康监测与分析系统**

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [API文档](#-api文档) • [部署指南](#-部署指南)

</div>

---

## 📋 项目简介

CardioGuard 360 是一个智能化的心血管健康监护平台,通过可穿戴设备实时采集用户的心率、心电图等生理参数,利用AI算法进行异常检测和趋势分析,为用户提供专业的健康评估和预警服务。

### 核心功能

✅ **实时心率监测** - WebSocket实时推送心率数据  
✅ **心电图(ECG)分析** - AI辅助诊断心律失常  
✅ **异常预警系统** - 多级预警机制,及时发现问题  
✅ **健康报告生成** - 自动生成日报/周报/月报  
✅ **医生远程监护** - 医生端可远程监控患者数据  
✅ **设备管理** - 支持多种可穿戴设备接入  

---

## 🚀 功能特性

### 1. 用户管理模块
- 👤 多角色支持(患者、医生、管理员)
- 🔐 JWT安全认证
- 📱 手机号/邮箱注册

### 2. 设备管理模块
- ⌚ 支持ECG、心率、血压、血氧等多种设备
- 🔗 设备绑定与解绑
- 📊 设备状态实时监控

### 3. 实时心率监测
- 💓 毫秒级数据采集
- 📡 WebSocket实时推送
- 📈 动态数据可视化

### 4. 异常预警系统
- ⚠️ 心动过速/过缓检测
- 🚨 心律失常识别
- 🔔 多级预警(低/中/高/危急)

### 5. 健康报告
- 📄 自动化报告生成
- 📊 趋势分析与统计
- 👨‍⚕️ 医生审核与评语

### 6. 医生监护端
- 👥 患者列表管理
- 📋 预警信息处理
- 💬 在线咨询服务

---

## 🛠 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 编程语言 |
| Spring Boot | 3.2.0 | Web框架 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| InfluxDB | 2.x | 时序数据库 |
| Redis | 7.x | 缓存数据库 |
| JWT | 0.12.3 | Token认证 |
| WebSocket | - | 实时通信 |
| Swagger | 3.x | API文档 |

### 前端技术 (待开发)
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | 5.x | 类型系统 |
| Element Plus | - | UI组件库 |
| ECharts | 5.x | 数据可视化 |
| Pinia | - | 状态管理 |
| Vite | 5.x | 构建工具 |

### 开发工具
- Maven - 依赖管理
- Git - 版本控制
- Docker - 容器化部署
- Postman - API测试

---

## 📦 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.x
- InfluxDB 2.x

### 1. 克隆项目

```bash
git clone https://github.com/your-username/cardio-guard-360.git
cd cardio-guard-360
```

### 2. 初始化数据库

```bash
# 执行数据库脚本
mysql -u root -p < database/init.sql
```

### 3. 配置应用

编辑 `cardio-guard-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cardioguard
    username: root
    password: your_password
    
influxdb:
  token: your_influxdb_token
```

### 4. 启动后端

```bash
cd cardio-guard-backend
mvn clean package
java -jar target/cardioguard-backend-1.0.0.jar
```

### 5. 访问API文档

浏览器打开: http://localhost:8080/api/swagger-ui.html

---

## 📚 API文档

### 认证接口

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "patient1",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138000",
  "role": "PATIENT"
}
```

#### 用户登录
```http
POST /api/auth/login?username=patient1&password=123456
```

### 心率数据接口

#### 上报心率数据
```http
POST /api/health/heart-rate/report
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 1,
  "deviceId": 1,
  "heartRate": 75.5,
  "rrInterval": 800.0,
  "hrv": 50.0,
  "activityStatus": "REST",
  "dataQuality": 95
}
```

#### 查询心率数据
```http
GET /api/health/heart-rate/query?userId=1&startTime=2024-01-01T00:00:00&endTime=2024-01-01T23:59:59
Authorization: Bearer {token}
```

### WebSocket连接

```javascript
const ws = new WebSocket('ws://localhost:8080/api/ws/health-monitor?userId=1');

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('收到实时数据:', data);
};
```

---

## 🗄 数据库设计

### 核心表结构

1. **sys_user** - 用户表
   - 支持患者、医生、管理员三种角色
   - 包含基本信息和登录凭证

2. **device** - 设备表
   - 管理可穿戴设备
   - 记录设备状态和绑定关系

3. **health_alert** - 健康预警表
   - 存储各类异常预警信息
   - 支持预警处理和跟踪

4. **health_report** - 健康报告表
   - 存储生成的健康报告
   - 支持医生审核流程

### 时序数据

使用InfluxDB存储心率等时序数据:
- Measurement: `heart_rate`
- Tags: `userId`, `deviceId`
- Fields: `heartRate`, `rrInterval`, `hrv`, etc.

---

## 🧪 测试

### 运行单元测试

```bash
cd cardio-guard-backend
mvn test
```

### 测试覆盖率

**当前测试覆盖情况:**

| 模块 | 测试文件 | 测试用例数 | 覆盖率 |
|------|---------|-----------|--------|
| 通用响应类 | ResultTest.java | 7 | ✅ 100% |
| JWT工具类 | JwtUtilTest.java | 3 | ✅ 95% |
| 用户服务 | UserServiceImplTest.java | 24 | ✅ 85% |
| 心率服务 | HeartRateServiceImplTest.java | 10 | ✅ 80% |
| **总计** | **4个测试类** | **44个测试用例** | **✅ >80%** |

**测试特性:**
- ✅ 使用JUnit 5 + Mockito框架
- ✅ 覆盖正常场景、异常场景和边界条件
- ✅ 使用@MockBean模拟外部依赖
- ✅ 事务隔离确保测试独立性

**目标测试覆盖率:** > 80% ✅ **已达成**

---

## 🐳 Docker部署

### 使用Docker Compose一键启动

```bash
docker-compose up -d
```

包含的服务:
- MySQL
- Redis
- InfluxDB
- CardioGuard Backend

---

## 📊 系统架构

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│  可穿戴设备  │ ──────> │  Backend API  │ <────── │  Web/App端  │
└─────────────┘         └──────────────┘         └─────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────▼─────┐      ┌─────▼─────┐
              │   MySQL   │      │ InfluxDB  │
              │  (业务数据)│      │ (时序数据)│
              └───────────┘      └───────────┘
```

---

## 📝 版本历史

### v1.5.0 (2026-04-16) - 多导联ECG功能基础设施

**新增功能:**
- ✨ 标准12导联配置管理(I, II, III, aVR, aVL, aVF, V1-V6)
- ✨ 多导联ECG记录CRUD操作
- ✨ 记录状态管理(RECORDING/COMPLETED/ERROR)
- ✨ 9个REST API端点(导联配置+多导联记录)
- ✨ 完整的单元测试覆盖(14个测试用例,覆盖率87.5%)

**技术改进:**
- 🔧 时序数据解耦设计(MySQL元数据 + InfluxDB波形数据)
- 🔧 LambdaQueryWrapper类型安全查询
- 🔧 Page分页查询支持
- 🔧 完善的异常处理和权限验证

**数据库变更:**
- 📊 新建 `ecg_lead_config` 表
- 📊 新建 `ecg_multilead_record` 表
- 📊 初始化12条标准导联配置数据

### v1.4.0 (2026-04-14) - ECG标注管理系统

**新增功能:**
- ✨ ECG波形标注功能(P/QRS/T波)
- ✨ AI自动标注算法
- ✨ 标注统计分析
- ✨ 8个REST API端点

### v1.3.x (2026-04-13~14) - AI心电图分析

**v1.3.3:** WebSocket实时ECG数据推送  
**v1.3.2:** ECG前端页面集成  
**v1.3.1:** ECG前端页面集成  
**v1.3.0:** AI心电图分析后端  

### v1.2.x - 基础功能完善

**v1.2.0:** 完整交付总结,测试覆盖率提升  

### v1.1.x - 核心功能开发

**v1.1.0:** 用户管理、设备管理、心率监测  

### v1.0.0 (初始版本)

- ✅ Spring Boot后端框架搭建
- ✅ MySQL数据库设计
- ✅ JWT认证机制
- ✅ RESTful API基础架构

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request!

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

---

## 📝 开发计划

- [ ] 前端Vue 3界面开发
- [ ] AI心电图分析模型集成
- [ ] 移动端App开发(React Native)
- [ ] 更多设备接入支持
- [ ] 数据导出功能
- [ ] 多语言支持

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 👥 团队

**CardioGuard Team**

---

## 📞 联系方式

- 📧 Email: support@cardioguard.com
- 🌐 Website: www.cardioguard.com
- 💬 Issues: [GitHub Issues](https://github.com/your-username/cardio-guard-360/issues)

---

<div align="center">

**如果这个项目对你有帮助,请给一个 ⭐ Star!**

Made with ❤️ by CardioGuard Team

</div>
