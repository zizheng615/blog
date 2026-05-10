#!/bin/bash
# ============================================================
# Blog 服务器首次初始化脚本
# 在全新的 Linux 云服务器上运行，自动安装依赖并部署项目
# 用法: sudo ./setup.sh
# 支持: Ubuntu 20.04/22.04, CentOS 7/8, Debian 10/11
# ============================================================

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${GREEN}[SETUP]${NC} $1"; }
warn() { echo -e "${YELLOW}[SETUP]${NC} $1"; }
error() { echo -e "${RED}[SETUP]${NC} $1"; exit 1; }

# 配置
PROJECT_DIR="/opt/blog"
GIT_REPO="${1:-}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root}"
JWT_SECRET="${JWT_SECRET:-mySecretKeyForBlogApplication2026}"

detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VER=$VERSION_ID
    else
        error "无法识别操作系统"
    fi
    log "检测到系统: $OS $VER"
}

install_package() {
    case $OS in
        ubuntu|debian)
            apt-get install -y -qq "$@" > /dev/null 2>&1
            ;;
        centos|rhel|fedora|rocky|almalinux)
            yum install -y "$@" > /dev/null 2>&1
            ;;
        *)
            error "不支持的操作系统: $OS"
            ;;
    esac
}

update_packages() {
    log "更新软件包列表..."
    case $OS in
        ubuntu|debian)
            apt-get update -qq
            ;;
        centos|rhel|fedora|rocky|almalinux)
            yum update -y > /dev/null 2>&1
            ;;
    esac
}

install_dependencies() {
    log "安装基础依赖..."
    update_packages
    install_package curl wget git vim net-tools
}

install_jdk8() {
    log "安装 JDK 8..."
    case $OS in
        ubuntu|debian)
            install_package openjdk-8-jdk
            ;;
        centos|rhel|rocky|almalinux)
            install_package java-1.8.0-openjdk java-1.8.0-openjdk-devel
            ;;
    esac
    java -version || error "JDK 8 安装失败"
    log "JDK 8 安装完成"
}

install_maven() {
    log "安装 Maven..."
    install_package maven
    mvn -version || error "Maven 安装失败"
    log "Maven 安装完成"
}

install_node() {
    log "安装 Node.js 18..."
    if ! command -v node > /dev/null 2>&1; then
        curl -fsSL https://deb.nodesource.com/setup_18.x | bash - > /dev/null 2>&1 || \
            curl -fsSL https://rpm.nodesource.com/setup_18.x | bash - > /dev/null 2>&1
        install_package nodejs
    fi
    node -v || error "Node.js 安装失败"
    log "Node.js 安装完成: $(node -v)"
}

install_mysql() {
    log "安装 MySQL 8..."
    case $OS in
        ubuntu|debian)
            install_package mysql-server
            systemctl start mysql
            systemctl enable mysql
            ;;
        centos|rhel|rocky|almalinux)
            if command -v dnf > /dev/null 2>&1; then
                dnf install -y @mysql > /dev/null 2>&1
            else
                yum install -y mysql-server > /dev/null 2>&1
            fi
            systemctl start mysqld
            systemctl enable mysqld
            ;;
    esac

    # 设置 root 密码
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}'; FLUSH PRIVILEGES;" > /dev/null 2>&1 || \
        mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT 1" > /dev/null 2>&1 || \
        warn "MySQL root 密码设置可能需要手动处理"

    log "MySQL 安装完成"
}

install_redis() {
    log "安装 Redis..."
    case $OS in
        ubuntu|debian)
            install_package redis-server
            systemctl start redis-server
            systemctl enable redis-server
            ;;
        centos|rhel|rocky|almalinux)
            install_package epel-release > /dev/null 2>&1 || true
            install_package redis
            systemctl start redis
            systemctl enable redis
            ;;
    esac
    log "Redis 安装完成"
}

install_nginx() {
    log "安装 Nginx..."
    install_package nginx

    # 配置 Nginx
    cat > /etc/nginx/conf.d/blog.conf << 'EOF'
server {
    listen 80;
    server_name _;

    location / {
        root /var/www/blog;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

    mkdir -p /var/www/blog
    nginx -t && systemctl restart nginx
    systemctl enable nginx
    log "Nginx 安装完成"
}

clone_project() {
    log "克隆项目..."
    mkdir -p "$PROJECT_DIR"
    cd "$PROJECT_DIR"

    if [ -z "$GIT_REPO" ]; then
        warn "未提供 Git 仓库地址，请手动将代码放到 $PROJECT_DIR"
        warn "或者重新运行: sudo ./setup.sh https://github.com/yourname/blog.git"
    else
        git clone "$GIT_REPO" . > /dev/null 2>&1 || {
            warn "目录已存在，尝试更新..."
            git pull > /dev/null 2>&1 || true
        }
        log "项目克隆完成"
    fi
}

init_database() {
    log "初始化数据库..."
    if [ -f "$PROJECT_DIR/backend/src/main/resources/db/schema.sql" ]; then
        mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" --default-character-set=utf8mb4 > /dev/null 2>&1 << EOF
DROP DATABASE IF EXISTS blog;
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
        mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" --default-character-set=utf8mb4 blog < "$PROJECT_DIR/backend/src/main/resources/db/schema.sql"
        log "数据库初始化完成"
    else
        warn "未找到 schema.sql，请手动初始化数据库"
    fi
}

create_systemd_service() {
    log "创建系统服务..."
    cat > /etc/systemd/system/blog-backend.service << EOF
[Unit]
Description=Blog Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$PROJECT_DIR
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar $PROJECT_DIR/backend/target/blog-backend-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=5
Environment="SPRING_DATASOURCE_PASSWORD=${MYSQL_ROOT_PASSWORD}"

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    systemctl enable blog-backend
    log "系统服务创建完成"
}

print_summary() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}     博客系统初始化完成！${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "项目目录: ${YELLOW}$PROJECT_DIR${NC}"
    echo -e "Nginx 根目录: ${YELLOW}/var/www/blog${NC}"
    echo -e "MySQL root 密码: ${YELLOW}${MYSQL_ROOT_PASSWORD}${NC}"
    echo ""
    echo -e "${GREEN}后续操作:${NC}"
    echo "  1. 将代码放入 $PROJECT_DIR"
    echo "  2. 执行 $PROJECT_DIR/scripts/deploy.sh 进行首次部署"
    echo "  3. 访问 http://服务器IP 查看博客"
    echo ""
    echo -e "${GREEN}常用命令:${NC}"
    echo "  systemctl start|stop|restart blog-backend"
    echo "  systemctl start|stop|restart nginx"
    echo "  systemctl start|stop|restart mysql"
    echo "  systemctl start|stop|restart redis"
    echo ""
    echo -e "${BLUE}========================================${NC}"
}

# ==================== 主流程 ====================

log "开始初始化博客服务器..."
detect_os

install_dependencies
install_jdk8
install_maven
install_node
install_mysql
install_redis
install_nginx
clone_project
init_database
create_systemd_service

print_summary
