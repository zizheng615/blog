# Personal Blog System

A clean and minimal personal blog for publishing technical articles and life stories. It ships with an admin dashboard, a comment system, visitor analytics, Redis caching, and more.

## Tech Stack

- **Frontend**: Vue 3 + Element Plus + Pinia + Vue Router + Vite
- **Backend**: Spring Boot 2.7 + Spring Security + JWT + MyBatis Plus + Redis
- **Database**: MySQL 8
- **Cache**: Redis

## Features

- Top navigation bar: Technical Articles, Life Stories, Contact Me
- Homepage sidebar: category and tag browsing, with click-through filtering
- Article list: titles, summaries, and tags, with pagination
- Article detail page: a professional style for technical posts and a literary style for life stories
- Comment system: emoji support and nested replies
- Footer: friend links, GitHub and Bilibili links, visitor statistics
- Admin dashboard: authentication, article publishing/editing (rich-text editor), and comment moderation
- Security: JWT authentication, SQL injection filtering, XSS filtering, and CORS configuration
- **Redis caching**: article lists, article details, categories, tags, and comments are all cached to improve query performance. Related caches are invalidated automatically after data changes, keeping Redis consistent with the database.

## Prerequisites

1. JDK 8+
2. Node.js 18+
3. MySQL 8.0+
4. Redis 3.0+
5. Maven 3.8+

## Quick Start

### 1. Database Setup

```bash
# Create the database (the character set must be utf8mb4 to support emoji)
mysql -u root -p -e "CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import the schema and seed data
mysql -u root -p blog < backend/src/main/resources/db/schema.sql
```

Update the database connection settings in `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. Redis Setup

Redis connects to `localhost:6379` by default. To change this, edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

Start Redis:

```bash
# Windows
redis-server.exe

# Linux/macOS
redis-server
```

### 3. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs at http://localhost:8080 by default.

### 4. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at http://localhost:5173 by default.

## URLs

| Page | URL |
|------|-----|
| Blog homepage | http://localhost:5173 |
| Article list | http://localhost:5173/articles |
| Contact me | http://localhost:5173/contact |
| Admin dashboard | http://localhost:5173/admin/login |

## Default Credentials

- Username: `admin`
- Password: `admin123`

## Common Pitfalls

1. **MySQL must use the utf8mb4 character set.** Otherwise, inserting comments with emoji will fail. Always specify `CHARACTER SET utf8mb4` when creating the database.

2. **Redis must be running first.** The backend attempts to connect to Redis on startup; if Redis is not running, startup will fail.

3. **Port conflicts:**
   - The backend uses port 8080 by default. If it is taken, change `server.port` in `application.yml`.
   - The frontend uses port 5173 by default. If it is taken, change `server.port` in `vite.config.js`.
   - Redis uses port 6379 by default.

4. **JDK version.** This project uses Spring Boot 2.7 and supports JDK 8+. Upgrading to Spring Boot 3 requires JDK 17+.

5. **JWT token expiry.** Tokens are valid for 24 hours; you must log in again after they expire.

6. **Slow Maven dependency downloads.** You can configure the Aliyun mirror in `settings.xml` to speed things up:

   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <name>Aliyun Public Repository</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

## Redis Cache Design

### Cache Key Conventions

| Data type | Key format | TTL |
|-----------|------------|-----|
| Article list | `article:list:{page}:{size}:{categoryId}:{tagId}:{type}:{status}` | 30 minutes |
| Article detail | `article:detail:{id}` | 30 minutes |
| Category list | `category:all` | 60 minutes |
| Tag list | `tag:all` | 60 minutes |
| Comment list | `comment:article:{articleId}` | 30 minutes |

### Cache Consistency

- **Reads**: check Redis first and return on a hit; on a miss, query the database and write the result back to Redis.
- **Creating an article**: evict all article list caches.
- **Updating or deleting an article**: evict all article list caches plus the detail cache for that article.
- **Posting a comment**: evict the comment cache for that article.
- **Deleting a comment**: evict the comment cache for that article.

### Inspecting the Cache

```bash
# List all cache keys
redis-cli keys '*'

# View cache statistics
redis-cli info keyspace
```

## Cloud Deployment (Docker Compose)

### 1. Choosing a Cloud Server (Most Economical Options)

| Provider | Specs | Price | Link |
|----------|-------|-------|------|
| Alibaba Cloud Simple Application Server | 2 vCPU / 2 GB RAM / 3 Mbps | ~¥99/year (new users) | [Alibaba Cloud](https://www.aliyun.com/minisite/goods?userCode=yours) |
| Tencent Cloud Lighthouse | 2 vCPU / 2 GB RAM / 4 Mbps | ~¥99/year (new users) | [Tencent Cloud](https://cloud.tencent.com/act/lighthouse) |
| Huawei Cloud Flexus | 2 vCPU / 2 GB RAM / 2 Mbps | ~¥99/year (new users) | [Huawei Cloud](https://activity.huaweicloud.com/) |

**Operating system**: Ubuntu 22.04 LTS or CentOS 7/8.

**About the ICP filing:**
- **Mainland China servers** (Beijing, Shanghai, Guangzhou, etc.): using a domain on ports 80/443 **requires an ICP filing** → [Alibaba Cloud filing](https://beian.aliyun.com/) / [Tencent Cloud filing](https://cloud.tencent.com/product/ba)
- **Filing-free options**: buy a server in Hong Kong, Singapore, or the US, or access it directly by IP on a non-standard port.

> Recommendation: for a personal blog, choose an **Alibaba Cloud or Tencent Cloud Lighthouse instance in Hong Kong** (around ¥200–300/year). No filing is required, and it is ready to use out of the box.

### 2. Server Setup (Installing Docker)

Connect to the server via SSH, then run:

```bash
# Update the system
sudo apt update && sudo apt upgrade -y    # Ubuntu/Debian
# or
sudo yum update -y                         # CentOS

# Install Docker (official script)
curl -fsSL https://get.docker.com | sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Start Docker and enable it on boot
sudo systemctl start docker
sudo systemctl enable docker

# Verify the installation
docker --version
docker-compose --version
```

### 3. First Deployment

```bash
# 1. Clone the project onto the server
git clone https://github.com/yourname/blog.git /opt/blog
cd /opt/blog

# 2. Configure environment variables
cp .env.example .env
nano .env        # Change the passwords and JWT secret (always do this in production)

# 3. Start all services
docker-compose up -d --build
```

The first startup automatically:
- Pulls the MySQL 8.0 and Redis 7 images
- Builds the frontend and backend Docker images
- Initializes the database (runs `schema.sql`)
- Starts all services and sets up the container network

**Verification**:
- Open `http://SERVER_IP` in a browser → blog homepage
- `http://SERVER_IP:8080/api/v1/articles?page=1&size=1` → API smoke test

### 4. Routine Hot Updates

> Hot update = change code locally → push to the repository → pull on the server → rebuild images → restart containers.

**Full hot-update workflow:**

```bash
# ===== Local development machine =====
# Commit your changes
git add .
git commit -m "Update articles / fix bugs"
git push origin main

# ===== SSH into the cloud server =====
cd /opt/blog

# Pull the latest code
git pull

# Run the hot update
chmod +x scripts/docker-redeploy.sh
./scripts/docker-redeploy.sh
```

**Script options:**

```bash
./scripts/docker-redeploy.sh           # Incremental update (recommended for daily use)
./scripts/docker-redeploy.sh --full    # Full rebuild (use when dependencies change)
./scripts/docker-redeploy.sh --backend # Update the backend only
./scripts/docker-redeploy.sh --frontend# Update the frontend only
./scripts/docker-redeploy.sh --logs    # Show logs after updating
```

**Manual hot update (without the script):**

```bash
# Incremental update (rebuilds only the services whose code changed; faster)
docker-compose up -d --build

# Full rebuild (clear the cache and rebuild from scratch)
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Force-restart all containers
docker-compose restart
```

### 5. Binding a Domain + HTTPS

#### Buying a Domain (Recommended)

| Platform | .com price | .cn price | Cheapest option |
|----------|------------|-----------|-----------------|
| Alibaba Cloud | ~¥69/year | ~¥35/year | .top ~¥12/year |
| Tencent Cloud | ~¥69/year | ~¥35/year | .xyz ~¥12/year |
| Namecheap | ~$10/year | — | — |

> The cheapest `.top` or `.xyz` domains cost about ¥10–20/year, which is more than enough for a personal blog.

**Steps:**
1. Buy a domain from Alibaba Cloud or Tencent Cloud.
2. Open the **DNS** console.
3. Add two A records:
   - `Host: @` → `Value: your server IP`
   - `Host: www` → `Value: your server IP`
4. Wait for DNS to propagate (usually a few minutes to a few hours).

#### Configuring HTTPS (Free Let's Encrypt Certificate)

**One-command setup (recommended):**

```bash
# Run from the project directory on the server
chmod +x scripts/setup-ssl.sh
sudo ./scripts/setup-ssl.sh yourdomain.com
```

This script automatically:
- Installs Nginx
- Installs Certbot
- Copies the configuration files
- Requests and installs the SSL certificate
- Configures automatic certificate renewal

**Manual setup:**

```bash
# 1. Install Nginx + Certbot
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx

# 2. Copy the configuration file
sudo cp nginx/blog.conf /etc/nginx/conf.d/blog.conf
sudo sed -i 's/yourdomain.com/yourdomain.com/g' /etc/nginx/conf.d/blog.conf

# 3. Test the configuration
sudo nginx -t

# 4. Request the certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 5. Start Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

**Automatic certificate renewal** (configured by default):

```bash
# Check renewal status
sudo certbot certificates

# Test renewal manually
sudo certbot renew --dry-run

# Check the scheduled job
sudo systemctl status certbot.timer
```

#### Verification

Once configured:
- `http://yourdomain.com` → automatically redirects to HTTPS
- `https://yourdomain.com` → blog homepage
- `https://yourdomain.com/api/...` → backend API

#### Without a Domain (IP + Self-Signed Certificate Only)

If you do not want to buy a domain yet, you can:
- Access the site directly via `http://SERVER_IP` (HTTP, unencrypted), or
- Configure a self-signed certificate (browsers will show a security warning; not recommended).

### 6. Common Maintenance Commands

```bash
# Show the status of all containers
docker-compose ps

# Follow logs in real time
docker-compose logs -f              # All logs
docker-compose logs -f backend      # Backend logs
docker-compose logs -f mysql        # Database logs

# Restart a single service
docker-compose restart backend
docker-compose restart frontend

# Open a shell inside a container
docker-compose exec mysql mysql -uroot -proot blog
docker-compose exec redis redis-cli

# Back up the database
docker-compose exec mysql mysqldump -uroot -proot blog > backup-$(date +%Y%m%d).sql

# Restore the database
cat backup.sql | docker-compose exec -T mysql mysql -uroot -proot blog

# Remove unused images (free up disk space)
docker image prune -f

# Show container resource usage
docker stats
```

### 7. Firewall Configuration

Open the following ports in your cloud server's security group or firewall:

| Port | Purpose | Required |
|------|---------|----------|
| 22 | SSH | Yes |
| 80 | HTTP | Yes |
| 443 | HTTPS | When using a domain |

On Alibaba Cloud or Tencent Cloud, add these under Console → Security Group → Inbound Rules.

## Project Structure

```
D:\Blog
├── backend/           # Spring Boot backend
│   ├── src/main/java/com/blog/
│   │   ├── controller/    # REST API controllers
│   │   ├── service/       # Business logic layer
│   │   ├── mapper/        # MyBatis Plus data access layer
│   │   ├── entity/        # Entity classes
│   │   ├── security/      # JWT authentication
│   │   ├── interceptor/   # SQL injection / XSS filters
│   │   ├── config/        # Configuration classes (RedisConfig, etc.)
│   │   └── utils/         # Utility classes (RedisCache, etc.)
│   ├── src/main/resources/
│   │   ├── db/schema.sql  # Database script
│   │   ├── mapper/xml/    # MyBatis XML mappings
│   │   └── application*.yml
│   ├── Dockerfile         # Backend container image
│   └── pom.xml
├── frontend/          # Vue 3 frontend
│   ├── src/
│   │   ├── views/         # Page components
│   │   ├── components/    # Shared components
│   │   ├── api/           # API request wrappers
│   │   ├── stores/        # Pinia state management
│   │   └── router/        # Route configuration
│   ├── Dockerfile         # Frontend container image
│   ├── nginx.conf         # Nginx production configuration
│   ├── .env.production    # Production environment variables
│   └── package.json
├── nginx/             # Nginx configuration files
│   └── blog.conf          # HTTPS reverse-proxy configuration
├── scripts/           # Deployment scripts
│   ├── docker-redeploy.sh # Docker hot-update script
│   └── setup-ssl.sh       # One-command HTTPS setup script
├── docker-compose.yml # One-command Docker deployment
├── .env.example       # Docker environment variable template
└── README.md
```

## Database Tables

| Table | Description |
|-------|-------------|
| user | Administrator accounts |
| category | Article categories (technical / life) |
| tag | Article tags |
| article | Articles |
| article_tag | Article–tag associations |
| comment | Comments |
| visitor | Visitor records |
| daily_visit | Daily visit statistics |
| friend_link | Friend links |

## License

MIT
