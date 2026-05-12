# 个人博客系统

简洁清新的个人博客，支持技术文章和生活分享，具备管理员后台、评论、访客统计、Redis缓存等功能。

## 技术栈

- **前端**: Vue 3 + Element Plus + Pinia + Vue Router + Vite
- **后端**: Spring Boot 2.7 + Spring Security + JWT + MyBatis Plus + Redis
- **数据库**: MySQL 8
- **缓存**: Redis

## 功能特性

- 顶部导航栏：技术文章、生活分享、联系我
- 首页侧边栏：分类和标签展示，支持点击查看文章
- 文章列表：展示标题、摘要、标签，支持分页
- 文章详情：技术文章专业风格，生活分享文艺风格
- 评论系统：支持表情符号，嵌套回复
- 页脚：友链、GitHub、B站链接、访客统计
- 管理后台：登录认证、文章发布/编辑（富文本编辑器）、评论管理
- 安全防护：JWT认证、SQL注入过滤、XSS过滤、CORS配置
- **Redis缓存**：文章列表、文章详情、分类、标签、评论均加入Redis缓存，提升查询性能；数据修改后自动清除相关缓存，保证缓存与数据库一致性

## 环境准备

1. JDK 8+
2. Node.js 18+
3. MySQL 8.0+
4. Redis 3.0+
5. Maven 3.8+

## 快速开始

### 1. 数据库配置

```bash
# 创建数据库（字符集必须为 utf8mb4 以支持 emoji）
mysql -u root -p -e "CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构和种子数据
mysql -u root -p blog < backend/src/main/resources/db/schema.sql
```

修改 `backend/src/main/resources/application-dev.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. Redis 配置

Redis 默认连接 `localhost:6379`，如需修改请编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

启动 Redis：
```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 http://localhost:8080

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173

## 访问地址

| 页面 | 地址 |
|------|------|
| 博客首页 | http://localhost:5173 |
| 文章列表 | http://localhost:5173/articles |
| 联系我 | http://localhost:5173/contact |
| 管理后台 | http://localhost:5173/admin/login |

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 常见坑点

1. **MySQL 字符集必须是 utf8mb4**：否则插入 emoji 评论会报错。创建数据库时务必指定 `CHARACTER SET utf8mb4`。

2. **Redis 必须先启动**：后端启动时会尝试连接 Redis，如果 Redis 未启动会导致启动失败。

3. **端口冲突**：
   - 后端默认端口 8080，如果被占用，修改 `application.yml` 中的 `server.port`
   - 前端默认端口 5173，如果被占用，修改 `vite.config.js` 中的 `server.port`
   - Redis 默认端口 6379

4. **JDK 版本**：本项目使用 Spring Boot 2.7，支持 JDK 8+。如需使用 Spring Boot 3，需要升级到 JDK 17+。

5. **JWT Token 过期**：Token 有效期为 24 小时，过期后需要重新登录。

6. **Maven 依赖下载慢**：可在 `settings.xml` 中配置阿里云镜像加速：
   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <name>阿里云公共仓库</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

## Redis 缓存设计

### 缓存Key规范

| 数据类型 | Key格式 | 过期时间 |
|----------|---------|----------|
| 文章列表 | `article:list:{page}:{size}:{categoryId}:{tagId}:{type}:{status}` | 30分钟 |
| 文章详情 | `article:detail:{id}` | 30分钟 |
| 分类列表 | `category:all` | 60分钟 |
| 标签列表 | `tag:all` | 60分钟 |
| 评论列表 | `comment:article:{articleId}` | 30分钟 |

### 缓存一致性保证

- **查询**：先查Redis，命中则直接返回；未命中则查数据库并写入Redis
- **新增文章**：清除所有文章列表缓存
- **更新/删除文章**：清除所有文章列表缓存 + 对应文章详情缓存
- **发表评论**：清除对应文章的评论缓存
- **删除评论**：清除对应文章的评论缓存

### 查看缓存状态

```bash
# 查看所有缓存key
redis-cli keys '*'

# 查看缓存统计
redis-cli info keyspace
```

## 云服务器部署（Docker Compose）

### 1. 选购云服务器（最经济方案）

| 服务商 | 配置 | 价格 | 链接 |
|--------|------|------|------|
| 阿里云轻量应用服务器 | 2核2G 3M带宽 | ~99元/年（新用户） | [阿里云](https://www.aliyun.com/minisite/goods?userCode=yours) |
| 腾讯云轻量应用服务器 | 2核2G 4M带宽 | ~99元/年（新用户） | [腾讯云](https://cloud.tencent.com/act/lighthouse) |
| 华为云Flexus云服务器 | 2核2G 2M带宽 | ~99元/年（新用户） | [华为云](https://activity.huaweicloud.com/) |

**系统选择**：Ubuntu 22.04 LTS 或 CentOS 7/8

**关于备案**：
- **国内服务器**（北京/上海/广州等）：使用域名 + 80/443 端口 **必须备案** → [阿里云备案](https://beian.aliyun.com/) / [腾讯云备案](https://cloud.tencent.com/product/ba)
- **免备案方案**：购买香港/新加坡/美国区域服务器，或使用 IP 直接访问 + 非标准端口

> 建议：个人博客买 **阿里云/腾讯云轻量香港节点**，约 200-300元/年，免备案，即开即用。

### 2. 服务器初始化（安装 Docker）

SSH 连接服务器后执行：

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y    # Ubuntu/Debian
# 或
sudo yum update -y                         # CentOS

# 安装 Docker（官方脚本）
curl -fsSL https://get.docker.com | sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 启动 Docker 并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
docker --version
docker-compose --version
```

### 3. 首次部署

```bash
# 1. 克隆项目到服务器
git clone https://github.com/yourname/blog.git /opt/blog
cd /opt/blog

# 2. 配置环境变量
cp .env.example .env
nano .env        # 修改密码和 JWT 密钥（生产环境务必修改）

# 3. 启动所有服务
docker-compose up -d --build
```

首次启动会自动完成：
- 拉取 MySQL 8.0 和 Redis 7 镜像
- 构建前后端 Docker 镜像
- 初始化数据库（执行 schema.sql）
- 启动所有服务并建立容器网络

**访问验证**：
- 浏览器访问 `http://服务器IP` → 博客首页
- `http://服务器IP:8080/api/v1/articles?page=1&size=1` → API 测试

### 4. 日常热更新

> 热更新 = 本地修改代码 → push 到仓库 → 服务器拉取 → 重新构建镜像 → 重启容器

**完整热更新流程：**

```bash
# ===== 本地开发机 =====
# 修改代码后提交
git add .
git commit -m "更新文章/修复bug"
git push origin main

# ===== SSH 登录云服务器 =====
cd /opt/blog

# 拉取最新代码
git pull

# 执行热更新
chmod +x scripts/docker-redeploy.sh
./scripts/docker-redeploy.sh
```

**脚本参数说明：**

```bash
./scripts/docker-redeploy.sh           # 增量更新（推荐日常用）
./scripts/docker-redeploy.sh --full    # 完整重建（依赖变更时用）
./scripts/docker-redeploy.sh --backend # 只更新后端
./scripts/docker-redeploy.sh --frontend# 只更新前端
./scripts/docker-redeploy.sh --logs    # 更新后查看日志
```

**手动热更新（不依赖脚本）：**

```bash
# 增量更新（只重建代码变化的服务，速度快）
docker-compose up -d --build

# 完整重建（清理缓存，彻底重新构建）
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# 强制重启所有容器
docker-compose restart
```

### 5. 绑定域名 + HTTPS

#### 购买域名（推荐）

| 平台 | .com 价格 | .cn 价格 |  cheapest |
|------|-----------|----------|-----------|
| 阿里云 | ~69元/年 | ~35元/年 | .top ~12元/年 |
| 腾讯云 | ~69元/年 | ~35元/年 | .xyz ~12元/年 |
| Namecheap | ~$10/年 | - | - |

> 最便宜的 `.top` 或 `.xyz` 域名约 10-20元/年，足够个人博客使用。

**操作步骤**：
1. 在阿里云/腾讯云购买域名
2. 进入 **域名解析** 控制台
3. 添加两条 A 记录：
   - `主机记录: @` → `记录值: 你的服务器IP`
   - `主机记录: www` → `记录值: 你的服务器IP`
4. 等待 DNS 生效（通常几分钟到几小时）

#### 配置 HTTPS（Let's Encrypt 免费证书）

**一键配置（推荐）：**

```bash
# 在服务器项目目录执行
chmod +x scripts/setup-ssl.sh
sudo ./scripts/setup-ssl.sh yourdomain.com
```

这个脚本会自动完成：
- 安装 Nginx
- 安装 Certbot
- 复制配置文件
- 申请并安装 SSL 证书
- 设置证书自动续期

**手动配置：**

```bash
# 1. 安装 Nginx + Certbot
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx

# 2. 复制配置文件
sudo cp nginx/blog.conf /etc/nginx/conf.d/blog.conf
sudo sed -i 's/yourdomain.com/yourdomain.com/g' /etc/nginx/conf.d/blog.conf

# 3. 测试配置
sudo nginx -t

# 4. 申请证书
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 5. 启动 Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

**证书自动续期**（已默认配置）：
```bash
# 查看续期状态
sudo certbot certificates

# 手动测试续期
sudo certbot renew --dry-run

# 查看定时任务
sudo systemctl status certbot.timer
```

#### 访问验证

配置完成后：
- `http://yourdomain.com` → 自动跳转到 HTTPS
- `https://yourdomain.com` → 博客首页
- `https://yourdomain.com/api/...` → 后端 API

#### 不使用域名（仅 IP + 自签名证书）

如果暂时不买域名，可以：
- 直接用 `http://服务器IP` 访问（HTTP，无加密）
- 或配置自签名证书（浏览器会提示不安全，不推荐）

### 6. 常用维护命令

```bash
# 查看所有容器状态
docker-compose ps

# 查看实时日志
docker-compose logs -f              # 全部日志
docker-compose logs -f backend      # 后端日志
docker-compose logs -f mysql        # 数据库日志

# 重启单个服务
docker-compose restart backend
docker-compose restart frontend

# 进入容器内部
docker-compose exec mysql mysql -uroot -proot blog
docker-compose exec redis redis-cli

# 数据库备份
docker-compose exec mysql mysqldump -uroot -proot blog > backup-$(date +%Y%m%d).sql

# 数据库恢复
cat backup.sql | docker-compose exec -T mysql mysql -uroot -proot blog

# 清理无用镜像（释放磁盘空间）
docker image prune -f

# 查看容器资源占用
docker stats
```

### 7. 防火墙配置

云服务器安全组/防火墙需开放以下端口：

| 端口 | 用途 | 必须 |
|------|------|------|
| 22 | SSH | 是 |
| 80 | HTTP | 是 |
| 443 | HTTPS | 有域名时 |

阿里云/腾讯云在控制台 → 安全组 → 入站规则中添加。

## 项目结构

```
D:\Blog
├── backend/           # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── controller/    # REST API 控制器
│   │   ├── service/       # 业务逻辑层
│   │   ├── mapper/        # MyBatis Plus 数据访问层
│   │   ├── entity/        # 实体类
│   │   ├── security/      # JWT 认证
│   │   ├── interceptor/   # SQL注入/XSS过滤器
│   │   ├── config/        # 配置类（RedisConfig等）
│   │   └── utils/         # 工具类（RedisCache等）
│   ├── src/main/resources/
│   │   ├── db/schema.sql  # 数据库脚本
│   │   ├── mapper/xml/    # MyBatis XML 映射
│   │   └── application*.yml
│   ├── Dockerfile         # 后端容器镜像
│   └── pom.xml
├── frontend/          # Vue 3 前端
│   ├── src/
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 公共组件
│   │   ├── api/           # API 请求封装
│   │   ├── stores/        # Pinia 状态管理
│   │   └── router/        # 路由配置
│   ├── Dockerfile         # 前端容器镜像
│   ├── nginx.conf         # Nginx 生产配置
│   ├── .env.production    # 生产环境变量
│   └── package.json
├── nginx/             # Nginx 配置文件
│   └── blog.conf          # HTTPS 反向代理配置
├── scripts/           # 部署脚本
│   ├── docker-redeploy.sh # Docker 热更新脚本
│   └── setup-ssl.sh       # 一键 HTTPS 配置脚本
├── docker-compose.yml # Docker 一键部署
├── .env.example       # Docker 环境变量模板
└── README.md
```

## 数据库表说明

| 表名 | 说明 |
|------|------|
| user | 管理员用户 |
| category | 文章分类（技术/生活）|
| tag | 文章标签 |
| article | 文章 |
| article_tag | 文章-标签关联 |
| comment | 评论 |
| visitor | 访客记录 |
| daily_visit | 每日访问统计 |
| friend_link | 友情链接 |

## 许可证

MIT
