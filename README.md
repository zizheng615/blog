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

## 开发学习笔记

> 这一节按日期记录每次新增/重构功能时遇到的「重难点」，方便日后回顾。

### 2026-05-11：联系方式 & 友链后台管理

本次新增了「后台可编辑联系方式」和「后台可增删改友链」两个功能。以下是关键技术点。

#### 1. 数据建模：通用 key/value 配置表 vs 专用表

> 联系方式只有 3 个字段（邮箱、GitHub、B 站），把它们都建一个固定列的 `contact` 表也行，但日后想加「微信公众号、微博、知乎」就得改表结构。

更通用的做法是建一个 **`site_config` 配置表**，所有杂项都以 `key/value` 形式存放：

```sql
CREATE TABLE `site_config` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key`   VARCHAR(64)  NOT NULL UNIQUE,
    `config_value` VARCHAR(512),
    `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

- `config_key` 加 `UNIQUE` 约束，方便做 upsert
- `config_value` 用 `VARCHAR(512)`，足够放 URL、邮箱、短文本

**何时选 key/value：** 字段会持续增加、字段之间没有强关系（不会 JOIN）、不需要 SQL 层做校验。本项目的「站点设置」就是这种典型场景。

#### 2. 后端模块：一个 CRUD 只需 5 个文件

完整的「新增 site_config 模块」涉及：

```
entity/SiteConfig.java            ← @Data + @TableName("site_config")
mapper/SiteConfigMapper.java      ← extends BaseMapper<SiteConfig>，零代码
service/SiteConfigService.java    ← 业务接口
service/impl/SiteConfigServiceImpl.java
controller/SiteConfigController.java
```

照着 `FriendLink` 那一套复制改名即可。MyBatis Plus 的 `BaseMapper` 已经提供 `selectList`、`insert`、`updateById`、`deleteById`，常规 CRUD 不用写 SQL。

#### 3. Upsert（不存在则插入，存在则更新）

后端保存联系方式时，前端传过来的是一个 `Map<String, String>`，每个 key 都可能是新增也可能是更新。MyBatis Plus 没有原生 upsert，最简单的写法是先查再分支：

```java
@Override
@Transactional
public Map<String, String> updateAll(Map<String, String> updates) {
    for (Map.Entry<String, String> entry : updates.entrySet()) {
        String key = entry.getKey();
        SiteConfig existing = siteConfigMapper.selectOne(
            new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, key));
        if (existing == null) {
            SiteConfig sc = new SiteConfig();
            sc.setConfigKey(key);
            sc.setConfigValue(entry.getValue());
            siteConfigMapper.insert(sc);
        } else {
            existing.setConfigValue(entry.getValue());
            siteConfigMapper.updateById(existing);
        }
    }
    return getAll();
}
```

要点：
- **加 `@Transactional`**：多次 DB 操作要在一个事务里，要么全部成功，要么一起回滚
- 使用 `LambdaQueryWrapper` 而不是手写 `"config_key = ?"`，避免列名拼写错误

#### 4. 同一个 Controller 里公开接口 + 管理员接口共存

前台需要读联系方式（无需登录），后台需要读取并能修改（需 ADMIN 角色）：

```java
@RestController
@RequestMapping("/api/v1")
public class SiteConfigController {

    @GetMapping("/site-config")           // 公开
    public Result<Map<String, String>> getPublic() { ... }

    @GetMapping("/admin/site-config")     // 管理员
    public Result<Map<String, String>> getAdmin() { ... }

    @PutMapping("/admin/site-config")     // 管理员
    public Result<Map<String, String>> update(@RequestBody Map<String, String> body) { ... }
}
```

Spring Security 在 `SecurityConfig` 里已配好：

```java
.antMatchers(HttpMethod.GET, "/api/v1/**").permitAll()   // GET 全公开
.antMatchers("/api/v1/admin/**").hasRole("ADMIN")        // 但 admin 下的修改类需要登录
```

#### 5. 前端：Element Plus 表格 + 弹窗 CRUD 的固定套路

`FriendLinkManageView.vue` 完整体现了「列表 + 新增/编辑弹窗 + 删除确认」的标准做法：

```vue
<el-table :data="links" v-loading="loading" stripe>
  <el-table-column prop="name" label="名称" />
  ...
  <el-table-column label="操作">
    <template #default="{ row }">
      <el-button @click="openEdit(row)">编辑</el-button>
      <el-button type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>

<el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '新增'">
  <el-form ref="formRef" :model="form" :rules="rules" />
  <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
  </template>
</el-dialog>
```

关键点：
- **`form.id` 既是「是否编辑模式」的判定，又是 URL 里的 ID**——新增时为 `null`，编辑时填进去
- **`v-loading="loading"`** 让用户在请求过程中看到加载态
- **`ElMessageBox.confirm` 用 try/catch 包裹**，取消时它会 `reject('cancel')`：

```js
try {
  await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' })
  await deleteFriendLink(row.id)
} catch (e) {
  if (e !== 'cancel') { /* 真出错了 */ }
}
```

#### 6. el-form 校验是 Promise，要 try/catch

```js
const handleSubmit = async () => {
  try {
    await formRef.value.validate()   // 校验失败会 throw
  } catch (e) {
    return                            // 直接 return，不要往下走
  }
  // 走到这里说明校验通过
  await updateFriendLink(form.id, payload)
}
```

#### 7. 表单复用：一个 form 对象支持「新增 + 编辑」

最佳实践是写一个 `emptyForm()` 工厂函数，每次打开弹窗时 `Object.assign(form, emptyForm(), 现有数据)`：

```js
const emptyForm = () => ({
  id: null, name: '', url: '', sortOrder: 0, isActive: true,
})
const form = reactive(emptyForm())

const openCreate = () => { Object.assign(form, emptyForm()) }
const openEdit = (row) => { Object.assign(form, emptyForm(), row) }
```

这样能保证关闭弹窗后再次打开时不会残留上一次的数据。

#### 8. 默认值兜底：API 挂了页面也别裂

前台 `ContactView` 读取 `/api/v1/site-config`，但如果后端还没部署、或者 DB 还没建好表，要让页面照样能展示。做法是：在 setup 阶段先填默认值，API 成功后再合并覆盖：

```js
const DEFAULTS = {
  contact_email: '2788906816@qq.com',
  contact_github: 'https://github.com/zizheng615',
  contact_bilibili: 'https://space.bilibili.com/291245814',
}
const config = ref({ ...DEFAULTS })

onMounted(async () => {
  try {
    const data = await getSiteConfig()
    if (data && typeof data === 'object') {
      config.value = { ...DEFAULTS, ...data }   // 关键：先展开默认，再被 API 覆盖
    }
  } catch (e) { console.error(e) }
})
```

这种「先有默认值，再被远程数据替换」的模式可以推广到任何依赖远程配置的页面。

#### 9. 可重复执行的迁移脚本（idempotent migration）

写 `migrations/2026-05-13_add_site_config.sql` 时，**插入语句一定要加 `ON DUPLICATE KEY UPDATE`**，这样即使误执行第二次也不会报错：

```sql
CREATE TABLE IF NOT EXISTS `site_config` ( ... );

INSERT INTO `site_config` (`config_key`, `config_value`) VALUES
    ('contact_email',    '2788906816@qq.com'),
    ('contact_github',   'https://github.com/zizheng615'),
    ('contact_bilibili', 'https://space.bilibili.com/291245814')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
```

- `CREATE TABLE IF NOT EXISTS` 保证表已存在时不报错
- `ON DUPLICATE KEY UPDATE` 让 INSERT 在唯一键冲突时变成 UPDATE
- **同步更新 `schema.sql`**，新部署的环境直接拥有最新表结构和种子数据

#### 10. 同步新增「路由 + 侧边栏菜单」

新页面要让用户能进入，必须**同时**改两个文件，路径要一致：

```js
// router/index.js
{ path: 'friend-links', component: () => import('@/views/admin/FriendLinkManageView.vue') },
{ path: 'contact',      component: () => import('@/views/admin/ContactManageView.vue') },
```

```vue
<!-- AdminLayout.vue -->
<el-menu-item index="/admin/friend-links">...友链管理</el-menu-item>
<el-menu-item index="/admin/contact">...联系我编辑</el-menu-item>
```

`el-menu-item` 的 `index` 必须是「带 `/admin/` 前缀的绝对路径」，因为 `el-menu` 上面写了 `router` 属性会直接当作 `to` 用。

#### 11. 修改一处，前台多处生效

为了让管理员在后台改完联系方式后，「联系我」页面和「页脚关注我」板块**都自动更新**，我们把页脚里原本写死的 `<a href="https://github.com/..." />` 也改成了从 `site-config` 接口读。

教训：**不要在多个地方重复硬编码同一个常量**，否则改起来漏一个就尴尬。能从后端读就从后端读，或者抽到统一的常量文件。

#### 12. 把已经提交的文件从 GitHub 里「下架」

发现 `.claude/settings.local.json`（Claude Code 的本地权限配置）不小心被推到 GitHub 上了。处理思路：

**第一步：以后别再追踪它**

把规则写进 `.gitignore`：

```gitignore
# Claude Code 本地配置
.claude/
```

但仅加 `.gitignore` 是不够的——git 对**已被追踪的文件**会忽略 `.gitignore`。

**第二步：从索引中移除（保留本地）**

```bash
git rm --cached .claude/settings.local.json
git add .gitignore
git commit -m "chore: 停止追踪 .claude 本地配置"
git push
```

- `--cached` 是关键：只从 git 索引里删，**磁盘上的文件不动**，本机继续能用
- 推送之后，GitHub 上「最新版本」就看不到这个文件了

**第三步（可选，慎用）：从历史里彻底抹除**

注意：上面只是「以后不追踪」。在旧 commit 里这个文件**还在**，任何人 clone 后 `git log -- 路径` 都能翻出来。要彻底清除得：

```bash
git filter-repo --path .claude/settings.local.json --invert-paths
git push --force
```

这是**破坏性操作**：
- 重写了所有 commit 的 hash
- 所有协作者必须重新 clone
- `--force` 覆盖远程，可能把别人没合并的提交吹掉

**判断准则：**

| 情况 | 处理方式 |
|---|---|
| 配置文件、本地路径、习惯日志（无密钥） | `git rm --cached` + `.gitignore` 就够了 |
| 误传了密码、API key、私钥、token | 必须 `filter-repo` + 强推 + **立刻吊销/更换密钥** |

记忆要点：**密钥一旦推上 GitHub，就当它已经泄露**——光删 commit 不够，必须 revoke + rotate。

### 2026-05-11：IP → 域名 + HTTPS（阿里云轻量·香港节点）

把访问入口从 `http://公网IP` 换成 `https://yourdomain.com`。香港节点免备案，是个人博客的最舒服组合。

#### 架构

```
浏览器 → DNS 解析 → 服务器 80/443
                       ↓
                    宿主机 Nginx（带 SSL）
                       ↓ proxy_pass
              Docker 容器  frontend(:8081) + backend(:8080)
```

**为什么要在宿主机加一层 Nginx？** 让 Certbot 在宿主机申请/续期证书，不必把证书塞进 Docker 容器里。容器只跑应用、解耦干净。

#### 完整步骤

1. **买域名 + 实名认证**（必经）：阿里云域名控制台 → 信息模板里上传身份证，1-3 天审核。
2. **DNS 解析**：在域名解析面板加两条 A 记录：`@` 和 `www` 都指到服务器公网 IP。
3. **开放端口**：阿里云轻量后台 → 防火墙 → 放行 TCP 80、443。
4. **腾出宿主机 80 端口（关键）**：项目里 `docker-compose.yml` 让 frontend 容器直接映射宿主机 :80，会和宿主机 Nginx 冲突。改成 `"8081:80"` 让出 80。
   ```yaml
   frontend:
     ports:
       - "8081:80"      # 原来是 "80:80"
   ```
5. **装 Nginx + Certbot**：
   ```bash
   sudo apt install -y nginx certbot python3-certbot-nginx
   ```
6. **写宿主机 Nginx 配置**：项目里有模板 `nginx/blog.conf`，要改两处：
   ```bash
   sudo cp ~/blog/nginx/blog.conf /etc/nginx/conf.d/blog.conf
   sudo sed -i 's/yourdomain.com/真域名.com/g' /etc/nginx/conf.d/blog.conf
   sudo sed -i 's|proxy_pass http://localhost:80;|proxy_pass http://localhost:8081;|' \
     /etc/nginx/conf.d/blog.conf
   sudo nginx -t
   ```
7. **申请证书**：
   ```bash
   sudo certbot --nginx -d 真域名.com -d www.真域名.com
   ```
   Certbot 自动改 Nginx 配置加 SSL 块、装好 systemd timer 每 60 天续期。
8. **启动 Nginx + 验证**：
   ```bash
   sudo systemctl enable nginx && sudo systemctl restart nginx
   ```
   浏览器访问 `https://真域名.com/blog/`（注意路径是 `/blog/`，因为 vite 的 `base` 配的就是这个）。

#### 关键陷阱

| 现象 | 根因 |
|---|---|
| Certbot 报 `unable to bind 0.0.0.0:80` | 宿主机 80 还被 frontend 容器占着 → 必须先做步骤 4 |
| 访问域名转圈打不开 | 阿里云防火墙没放 80/443 → 步骤 3 漏了 |
| HTTPS 首页通但 `/api` 502 | Nginx `proxy_pass` 端口指错 → backend 在 `localhost:8080`，frontend 在 `localhost:8081` |
| `ping yourdomain.com` 不通 | DNS 未生效（等 5-30 分钟）或**域名实名认证没通过** |
| 浏览器提示「不安全」 | 用了 `http://` 没跳 HTTPS；或证书申请失败被回退 |

#### 一个需要权衡的细节

vite 配置里 `base: '/blog/'`，所以访问入口默认是 `https://你的域名/blog/` 而不是根路径。两个选择：

- **保持现状**（推荐）：维持 `/blog/` 前缀，在 Nginx 加 `location = / { return 301 /blog/; }` 让根路径跳进去
- **改 vite base 为 `/`**：要顺带改 `vue-router` 的 `createWebHistory(import.meta.env.BASE_URL)`、`.env.production` 等，工程稍大

#### 备案 vs 免备案

| 服务器地域 | 80/443 直接用 | 需要备案 |
|---|---|---|
| 中国大陆（北京/上海/杭州...） | ❌ 必须备案，否则封端口 | 是，7-20 天 |
| 中国香港 | ✅ 直接可用 | 否 |
| 新加坡/东京/美西 | ✅ 直接可用 | 否 |

个人博客图省事，**首选香港节点**，约 200-300 元/年。

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
