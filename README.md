# 🎬 智能视频内容管理平台

一个基于 Spring Boot + Vue 3 的全栈视频内容管理系统，集成 B站视频爬取、AI 智能审核、大数据分析等功能。

## 📋 项目简介

本项目是一个功能完整的视频内容管理平台，支持视频的爬取、存储、智能审核、用户交互和数据分析。系统采用前后端分离架构，集成了机器学习模型进行内容审核，并使用大数据技术栈处理海量数据。

### 核心特性

- 🎥 **B站视频爬取** - 支持 B站视频信息自动爬取与存储
- 🤖 **AI 智能审核** - 基于 Spark ML 的文本分类模型，自动识别违规内容
- 📊 **大数据分析** - 集成 Hadoop HDFS、Kafka 进行大数据处理
- 🔐 **权限管理** - 基于 RBAC 的用户角色权限控制系统
- 💬 **社交互动** - 评论、点赞、收藏、关注、私信等完整社交功能
- 📈 **数据可视化** - ECharts 图表展示数据分析结果
- 🤝 **AI 助手** - 集成智谱 AI SDK 提供智能助手功能

---

## 系统功能

### 首页

* 鼠标滚轮下滑刷新
* AI智能小助手
<img width="1566" height="948" alt="image" src="https://github.com/user-attachments/assets/2fd7a0d3-bc41-440a-9921-0aeea243317a" />

* 根据收藏推荐十条视频
* 按分类筛选
<img width="1207" height="943" alt="image" src="https://github.com/user-attachments/assets/14bdef52-7b92-460a-b4f9-86df0cb41f9c" />

视频点赞收藏评论
<img width="1180" height="868" alt="image" src="https://github.com/user-attachments/assets/9786eb7b-1d5d-49f1-b138-70858389aa7c" />
<img width="1007" height="694" alt="image" src="https://github.com/user-attachments/assets/0ebe0774-b1bd-4e3e-9acd-9a6b8c40c84c" />

消息中心，私信，系统通知，点赞收藏关注通知
<img width="1255" height="497" alt="image" src="https://github.com/user-attachments/assets/7106efc2-4d8f-4984-8827-1571ed3812da" />
<img width="1262" height="938" alt="image" src="https://github.com/user-attachments/assets/fea4e821-2ac6-4a2e-89b3-32bf6fe524ff" />
<img width="1292" height="576" alt="image" src="https://github.com/user-attachments/assets/51110d93-431e-4ecf-b6ec-819703f37cb4" />

创作者中心
<img width="1234" height="811" alt="image" src="https://github.com/user-attachments/assets/968c135a-9241-48ac-a978-964487b60992" />
<img width="1233" height="938" alt="image" src="https://github.com/user-attachments/assets/3c0c46cc-02a4-4b07-91eb-0a44c84026b3" />

用户中心
<img width="1205" height="896" alt="image" src="https://github.com/user-attachments/assets/7ec59070-55ff-425e-9192-cdb5beda2ce9" />

后台管理
<img width="1818" height="934" alt="image" src="https://github.com/user-attachments/assets/64a7ea81-5655-4a53-a7af-7468e38518ac" />
<img width="1817" height="937" alt="image" src="https://github.com/user-attachments/assets/2e8bf3b2-ce40-4222-bb60-20686a596c8a" />
<img width="1620" height="926" alt="image" src="https://github.com/user-attachments/assets/d41c4ac3-50db-4e7c-a42f-cad465ba3717" />
<img width="1634" height="939" alt="image" src="https://github.com/user-attachments/assets/fc5161f4-07f6-4834-a233-b02db158045d" />
<img width="1904" height="938" alt="image" src="https://github.com/user-attachments/assets/c388d4eb-4688-49e6-a7b7-3d3be63cab65" />

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Admin-Web     │    │   ML Service    │
│   (Vue 3)       │    │   (Vue 3)       │    │   (Flask)       │
└────────┬────────┘    └────────┬────────┘    └────────┬────────┘
         │                      │                      │
         └──────────────────────┼──────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │      Backend          │
                    │   (Spring Boot)       │
                    └───────────┬───────────┘
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
┌────────▼────────┐    ┌────────▼────────┐    ┌───────▼────────┐
│     MySQL       │    │     Kafka       │    │  Hadoop HDFS   │
│   (数据存储)     │    │  (消息队列)      │    │  (分布式存储)   │
└─────────────────┘    └─────────────────┘    └────────────────┘
```

---

## 🛠️ 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| Spring Security | - | 安全框架 |
| JWT | 0.9.1 | 身份认证 |
| Spring Kafka | - | 消息队列 |
| Hadoop HDFS | 2.7.3 | 分布式存储 |
| 智谱 AI SDK | 0.3.3 | AI 能力集成 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 前端框架 |
| Vue Router | 5.x | 路由管理 |
| Element Plus | 2.13.x | UI 组件库 |
| ECharts | 6.0.x | 图表可视化 |
| Axios | 1.13.x | HTTP 客户端 |
| Vite | 7.x | 构建工具 |

### 机器学习
| 技术 | 说明 |
|------|------|
| PySpark | 大数据处理与模型训练 |
| jieba | 中文分词 |
| Logistic Regression | 文本分类模型 |
| TF-IDF | 特征提取 |

---

## 📁 项目结构

```
p2_0/
├── backend/                    # 后端服务 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/example/backend/
│   │       ├── controller/     # 控制器层 (24个控制器)
│   │       ├── service/        # 服务层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── config/         # 配置类
│   │       └── utils/          # 工具类
│   └── pom.xml
│
├── frontend/                   # 用户前端 (Vue 3)
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   └── assets/             # 静态资源
│   └── package.json
│
├── admin-web/                  # 管理后台 (Vue 3)
│   ├── src/
│   │   ├── views/              # Dashboard、用户管理、数据分析等
│   │   └── api/                # API 接口
│   └── package.json
│
├── ml_service/                 # 机器学习服务 (Flask)
│   └── app.py                  # 文本分类、视频爬取等接口
│
├── bilibili_crawler.py         # B站视频爬虫脚本
├── train_lr_optimized.py       # 模型训练脚本
│
├── lr_text_classification_model_optimized/  # 训练好的模型
│
├── uploads/                    # 上传文件存储
└── *.json                      # 训练数据集
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 1.8+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Kafka**: 2.x+
- **Hadoop**: 2.7.x (可选)
- **Python**: 3.8+ (ML 服务)
- **Spark**: 3.x (模型训练)

### 1. 数据库配置

创建数据库并导入表结构：

```sql
CREATE DATABASE smart_campus_course DEFAULT CHARACTER SET utf8mb4;
```

修改 `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_campus_course
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 启动前端服务

```bash
# 用户端
cd frontend
npm install
npm run dev

# 管理后台
cd admin-web
npm install
npm run dev
```

### 4. 启动机器学习服务 (可选)

```bash
cd ml_service
pip install -r requirements.txt
python app.py
```

ML 服务将在 `http://127.0.0.1:5001` 启动

---

## 📦 核心功能模块

### 1. 用户认证与授权
- 用户注册/登录
- JWT Token 认证
- RBAC 权限控制
- 角色管理

### 2. 视频管理
- B站视频自动爬取
- 视频信息存储与展示
- 视频分类与标签
- 视频代理播放

### 3. 智能审核
- 文本内容违规检测
- 6 类违规类型识别：
  - 博彩
  - 低俗色情
  - 黑产广告
  - 谩骂引战
  - 欺诈
  - 不违规

### 4. 社交功能
- 视频点赞/收藏
- 评论系统
- 用户关注
- 私信功能
- 通知系统 (Kafka)

### 5. 数据分析
- 视频数据统计
- 用户行为分析
- ECharts 可视化展示
- 词云生成

### 6. AI 助手
- 智谱 AI 集成
- 智能对话
- 内容推荐

---

## 📡 API 接口

### 主要接口列表

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/login | 用户登录 |
| 认证 | POST /api/auth/register | 用户注册 |
| 视频 | GET /api/videos | 获取视频列表 |
| 视频 | POST /api/crawler/crawl | 爬取 B站视频 |
| 审核 | POST /api/ml/predict | 内容审核预测 |
| 用户 | GET /api/users/profile | 获取用户信息 |
| 评论 | GET /api/comments/{videoId} | 获取视频评论 |
| 通知 | GET /api/notifications | 获取通知列表 |

---

## 🤖 机器学习模型

### 模型训练

```bash
python train_lr_optimized.py
```

训练数据包括：
- `不违规.json`
- `博彩.json`
- `低俗色情.json`
- `黑产广告.json`
- `谩骂引战.json`
- `欺诈.json`

### 模型特点
- 基于 PySpark 的分布式训练
- TF-IDF 特征提取
- 逻辑回归分类
- 支持大规模数据处理

---

## 📊 数据库设计

### 主要数据表

| 表名 | 说明 |
|------|------|
| user | 用户信息 |
| video | 视频信息 |
| bilibili_video | B站视频数据 |
| comment | 评论 |
| category | 视频分类 |
| tag | 标签 |
| role | 角色 |
| permission | 权限 |
| notification | 通知 |

---

## 🔧 配置说明

### Kafka 配置
```properties
spring.kafka.bootstrap-servers=localhost:9092
```

### HDFS 配置
```properties
HDFS_URL=http://localhost:50070
HDFS_NAMENODE=hdfs://localhost:9000
```

### 文件上传配置
```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

---

## 📸 系统截图

### 用户端
- 首页视频浏览
- 视频详情页
- 用户中心
- 创作者中心

### 管理后台
- 数据仪表盘
- 用户管理
- 数据分析
- 内容审核

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目仅供学习交流使用。

---

## 👥 作者

毕业设计项目

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [ECharts](https://echarts.apache.org/)
- [PySpark](https://spark.apache.org/docs/latest/api/python/)
<<<<<<< HEAD
- [智谱 AI](https://www.zhipuai.cn/)
=======
- [智谱 AI](https://www.zhipuai.cn/)
>>>>>>> dev
