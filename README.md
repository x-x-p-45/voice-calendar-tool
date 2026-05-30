# 🎙️ 语音版日历工具

> 一款基于 Java SpringBoot 开发的语音智能日程管理工具，通过语音输入自动解析日程内容与时间，快速创建、管理个人日程，无需手动繁琐录入。

**本作品为七牛云 X Engineer 暑期实训营第二批次参赛作品。**

---

## 📋 项目介绍

本项目利用语音识别（ASR）技术将用户的语音输入转化为文本，再通过智能正则解析引擎自动提取日程中的**时间信息**与**事件内容**，实现"一句话创建日程"的便捷体验。

### 核心流程

```
用户语音 → ASR语音转文本 → 后端智能解析（时间+事件） → MySQL持久化 → REST API查询
```

---

## 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端语言 | Java | 19 |
| 开发框架 | SpringBoot | 2.7.15 |
| 数据库 | MySQL | 5.7+ / 8.0+ |
| 持久层 | MyBatis | 2.2.2 |
| 接口文档 | SpringDoc OpenAPI | 1.7.0 |
| 语音能力 | 第三方 ASR 接口（HTTP 调用） | — |
| 构建工具 | Maven | 3.8+ |
| 版本管理 | Git + GitHub | — |

### 第三方依赖清单

| 依赖 | GroupId | ArtifactId | 用途 | 
|------|---------|------------|------|
| SpringBoot Web | org.springframework.boot | spring-boot-starter-web | Web 框架 |
| MyBatis Starter | org.mybatis.spring.boot | mybatis-spring-boot-starter | ORM 持久层 |
| MySQL Connector | com.mysql | mysql-connector-j | 数据库驱动 |
| SpringDoc OpenAPI | org.springdoc | springdoc-openapi-ui | API 文档生成 |
| Apache HttpClient | org.apache.httpcomponents | httpclient | HTTP 请求（调用 ASR） |
| Lombok | org.projectlombok | lombok | 简化实体类代码 |

---

## ✨ 核心功能

1. 🎤 **语音输入智能解析** — 自动提取日程时间与事件标题
2. 📅 **日程 CRUD 完整管理** — 新增、查询、删除、提醒状态更新
3. 🔍 **按日期范围查询** — 灵活筛选特定时间段内的日程
4. 🔔 **日程提醒管理** — 标记/切换日程提醒状态
5. 🌐 **标准 RESTful API** — 支持前后端分离对接

---

## 💡 项目亮点

| 亮点 | 说明 |
|------|------|
| 智能时间解析 | 支持"明天下午3点""后天上午10点""2025-12-25 14:00"等多种中文时间表达 |
| MVC 分层架构 | Controller → Service → Mapper → Entity 清晰分层，高可维护性 |
| 统一返回格式 | 所有接口返回 `R<T>` 统一封装（code + msg + data），前端对接友好 |
| 全局异常处理 | `@RestControllerAdvice` 统一捕获异常，接口健壮可靠 |
| 轻量化部署 | 无复杂依赖，克隆代码 → 执行 SQL → 修改配置 → 一键启动 |
| 代码规范 | 完整的 Javadoc 注释，符合阿里巴巴 Java 开发手册规范 |

---

## 🚀 本地部署步骤

### 1. 克隆项目

```bash
git clone https://github.com/x-x-p-45/voice-calendar-tool.git
cd voice-calendar-tool
```

### 2. 初始化数据库

执行项目根目录下的 `init.sql` 脚本：

```bash
mysql -u root -p < init.sql
```

或在 MySQL 客户端中运行该脚本。

### 3. 修改数据库配置

编辑 `src/main/resources/application.yml`，将 `password` 改为你的 MySQL 密码：

```yaml
spring:
  datasource:
    username: root
    password: 你的数据库密码
```

### 4. 启动项目

```bash
mvn clean compile
mvn spring-boot:run
```

或直接在 IDE 中运行 `VoiceCalendarApplication.java`。

### 5. 验证接口

```bash
# 查询所有日程
curl http://localhost:8080/schedule/list

# 语音创建日程
curl -X POST "http://localhost:8080/schedule/voiceAdd?voiceText=明天下午3点项目周会"

# 按范围查询
curl "http://localhost:8080/schedule/listByRange?start=2025-12-01&end=2025-12-31"

# 删除日程
curl -X DELETE "http://localhost:8080/schedule/delete/1"

# 更新提醒状态
curl -X PUT "http://localhost:8080/schedule/remind/1?isRemind=0"
```

---

## 📡 API 接口文档

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| `POST` | `/schedule/voiceAdd` | 语音文本解析并创建日程 | `voiceText` (String, 必填) |
| `GET` | `/schedule/list` | 查询所有日程 | — |
| `GET` | `/schedule/listByRange` | 按日期范围查询 | `start`, `end` (yyyy-MM-dd) |
| `DELETE` | `/schedule/delete/{id}` | 删除指定日程 | `id` (路径参数) |
| `PUT` | `/schedule/remind/{id}` | 更新提醒状态 | `id` (路径参数), `isRemind` (0/1) |

### 统一返回格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

---

## 📁 项目结构

```
voice-calendar-tool/
├── pom.xml                              # Maven 依赖配置
├── init.sql                             # 数据库初始化脚本
├── README.md                            # 项目说明文档
└── src/main/
    ├── java/com/calendar/
    │   ├── VoiceCalendarApplication.java    # 项目启动入口
    │   ├── common/
    │   │   ├── R.java                       # 统一返回结果类
    │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
    │   ├── entity/
    │   │   └── Schedule.java                # 日程实体类
    │   ├── mapper/
    │   │   └── ScheduleMapper.java          # 数据访问层接口
    │   ├── service/
    │   │   ├── ScheduleService.java         # 业务接口
    │   │   └── impl/
    │   │       └── ScheduleServiceImpl.java # 业务实现（含时间解析）
    │   └── controller/
    │       └── ScheduleController.java      # REST 控制器
    └── resources/
        └── application.yml                  # 项目配置文件
```

---

## 🔮 后续可优化方向

- [ ] 完善前端 UI 界面（Vue/React）
- [ ] 增强语音语义解析（引入 NLP 模型）
- [ ] 添加定时提醒推送（WebSocket / 邮件通知）
- [ ] 用户认证与多用户支持
- [ ] 日程分类与标签管理

---

## 📝 开发总结

通过本次项目开发，巩固了 Java 后端开发、SpringBoot 框架使用、数据库设计与接口开发能力，实现了从需求设计到代码落地的全流程开发。项目中实践了 MVC 分层架构、正则表达式文本解析、RESTful API 设计等核心技能。

---

## 👤 作者

- **GitHub**: [x-x-p-45](https://github.com/x-x-p-45)
- **项目仓库**: [voice-calendar-tool](https://github.com/x-x-p-45/voice-calendar-tool)

---

*📅 2025 年 12 月*
