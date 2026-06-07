# craft-trace-vue

基于 Spring Boot + Vue 3 的传统手工艺工坊研学与作品溯源平台毕业设计项目。

## 项目简介

本项目围绕传统手工艺工坊的课程管理、学员参与、作品展示与工艺溯源展开，适合作为毕业设计或课程项目展示。当前仓库同时包含前端与后端代码。

主要功能：

- 用户注册、登录与角色管理
- 教师、学生、匠人、工坊、材料管理
- 课程发布、报名、支付、签到、评价
- 学生作品展示与工艺品管理
- 溯源步骤维护与公开溯源查询
- 图像相似检索辅助查询
- 数据统计展示

## 目录结构

```text
craft-trace-vue/
├─ src/                              Vue 前端源码
├─ public/                           前端静态资源
├─ IdeaProjects（后端）/craft-trace/  Spring Boot 后端项目
├─ demo_refresh_utf8.sql             示例数据修正脚本
├─ package.json                      前端依赖与脚本
├─ vite.config.js                    Vite 配置
└─ README.md
```

## 技术栈

前端：

- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts

后端：

- Spring Boot 2.7
- Spring Data JPA
- MySQL 8
- Maven
- Java 17

## 环境要求

- Node.js 18+
- npm
- Java 17
- Maven 3.9+
- MySQL 8

## 快速启动

### 1. 准备数据库

先在 MySQL 中创建数据库：

```sql
CREATE DATABASE craft_trace DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置后端数据库连接

后端配置文件位于：

`IdeaProjects（后端）/craft-trace/src/main/resources/application.properties`

当前已支持通过环境变量覆盖数据库配置：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

示例：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/craft_trace?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
```

如果你更习惯直接修改 `application.properties`，也可以本地自行调整，但不建议把个人配置再次提交到仓库。

### 3. 启动后端

进入后端目录后执行：

```bash
mvn spring-boot:run
```

后端启动类：

`IdeaProjects（后端）/craft-trace/src/main/java/com/graduation/crafttrace/CraftTraceApplication.java`

说明：

- 项目启用了 `spring.jpa.hibernate.ddl-auto=update`
- 首次启动会自动创建或更新表结构
- 后端包含部分演示数据初始化逻辑

### 4. 安装前端依赖

在项目根目录执行：

```bash
npm install
```

### 5. 启动前端

在项目根目录执行：

```bash
npm run dev
```

默认开发地址：

- `http://127.0.0.1:5173`

## 前端环境变量

开发环境配置文件：

- `.env.development`
- `.env.example`

主要变量说明：

- `VITE_DEV_HOST`：前端开发服务器监听地址
- `VITE_DEV_PORT`：前端开发服务器端口
- `VITE_API_TARGET`：Vite 代理到的后端地址
- `VITE_FILE_BASE_URL`：上传文件的绝对访问前缀，可留空
- `VITE_PUBLIC_ORIGIN`：公开分享链接使用的前端地址，可留空

## 常用命令

前端：

```bash
npm run dev
npm run build
npm run preview
npm run lint
```

后端：

```bash
mvn spring-boot:run
```

## 开源整理说明

为方便发布到 GitHub，仓库已做过一轮整理：

- 删除了未使用的 Vite 脚手架示例文件
- 删除了后端手工调试用的静态 HTML 页面
- 删除了后端运行时上传文件
- 补充了前后端常见生成物和运行时目录的忽略规则
- 去掉了 README 中不适合公开仓库的本机绝对路径说明

如果后续需要重新生成上传文件，应用运行时会自动创建对应目录。

## 说明

- `public/images/` 中保留的是项目演示资源，不属于缓存文件
- `demo_refresh_utf8.sql` 用于补充或修正部分演示数据
- 如果你准备正式开源，建议再补充系统截图、演示视频链接和许可证文件
