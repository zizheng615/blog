#!/bin/bash
# ============================================================
# Blog 一键部署脚本
# 在服务器上执行，拉取最新代码并重启服务
# 用法: ./deploy.sh
# ============================================================

set -e

# 配置项（根据实际环境修改）
PROJECT_DIR="/opt/blog"
BACKEND_JAR="backend/target/blog-backend-1.0.0.jar"
NGINX_ROOT="/var/www/blog"
LOG_FILE="/opt/blog/logs/deploy.log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
    exit 1
}

# 确保日志目录存在
mkdir -p "$(dirname "$LOG_FILE")"

# 进入项目目录
cd "$PROJECT_DIR" || error "项目目录不存在: $PROJECT_DIR"
log "进入项目目录: $PROJECT_DIR"

# 1. 拉取最新代码
log "拉取最新代码..."
git fetch origin
git reset --hard origin/main || git reset --hard origin/master
log "代码更新完成"

# 2. 构建后端
log "构建后端..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q || error "后端构建失败"
log "后端构建完成: $BACKEND_JAR"

# 3. 构建前端
log "构建前端..."
cd "$PROJECT_DIR/frontend"
npm install > /dev/null 2>&1 || warn "npm install 出现警告"
npm run build || error "前端构建失败"
log "前端构建完成"

# 4. 优雅停止旧后端
log "停止旧服务..."
PID=$(pgrep -f "blog-backend-1.0.0.jar" || true)
if [ -n "$PID" ]; then
    log "发送终止信号到旧进程 (PID: $PID)"
    kill "$PID" 2>/dev/null || true
    for i in {1..10}; do
        if ! pgrep -f "blog-backend-1.0.0.jar" > /dev/null; then
            log "旧进程已停止"
            break
        fi
        sleep 1
    done
    # 强制终止
    PID=$(pgrep -f "blog-backend-1.0.0.jar" || true)
    if [ -n "$PID" ]; then
        warn "强制终止旧进程 (PID: $PID)"
        kill -9 "$PID" 2>/dev/null || true
    fi
else
    log "没有运行中的后端进程"
fi

# 5. 部署前端到 Nginx
log "部署前端到 Nginx..."
if [ ! -d "$NGINX_ROOT" ]; then
    sudo mkdir -p "$NGINX_ROOT"
fi
sudo cp -r "$PROJECT_DIR/frontend/dist/"* "$NGINX_ROOT/"
sudo nginx -t && sudo systemctl reload nginx
log "Nginx 重载完成"

# 6. 启动新后端
log "启动新后端..."
cd "$PROJECT_DIR"
nohup java -Xms256m -Xmx512m \
    -jar "$BACKEND_JAR" \
    --spring.profiles.active=prod \
    > "$LOG_FILE" 2>&1 &
cd - > /dev/null

# 7. 健康检查
log "等待服务启动..."
sleep 10

HEALTH_URL="http://localhost:8080/api/v1/articles?page=1&size=1"
for i in {1..12}; do
    if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
        log "========================================"
        log "部署成功！"
        log "后端: http://localhost:8080"
        log "前端: http://localhost (Nginx)"
        log "日志: $LOG_FILE"
        log "========================================"
        exit 0
    fi
    echo "健康检查 $i/12 ..."
    sleep 3
done

error "部署失败 - 后端服务未能正常启动，请检查日志: $LOG_FILE"
