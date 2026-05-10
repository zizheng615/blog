#!/bin/bash
# Blog Docker 一键热更新脚本
# 用法: ./docker-redeploy.sh [选项]
#   无选项     : 增量更新（只重建有变化的镜像）
#   --full     : 完整重建（清理缓存，重新构建所有镜像）
#   --backend  : 只更新后端
#   --frontend : 只更新前端
#   --logs     : 更新后查看日志

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

show_help() {
    echo "Blog Docker 热更新脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  (无)       增量更新 - 只重建代码有变化的服务"
    echo "  --full     完整重建 - 清理构建缓存，重新构建所有镜像"
    echo "  --backend  只更新后端服务"
    echo "  --frontend 只更新前端服务"
    echo "  --logs     更新完成后查看日志"
    echo "  --help     显示帮助"
    echo ""
    echo "示例:"
    echo "  $0                 # 日常热更新"
    echo "  $0 --full          # 完整重建（首次部署或依赖变更）"
    echo "  $0 --backend       # 只更新后端代码"
    echo "  $0 --frontend      # 只更新前端代码"
    echo "  $0 --full --logs   # 完整重建并查看日志"
}

# 解析参数
FULL_REBUILD=false
BACKEND_ONLY=false
FRONTEND_ONLY=false
SHOW_LOGS=false

for arg in "$@"; do
    case $arg in
        --full) FULL_REBUILD=true ;;
        --backend) BACKEND_ONLY=true ;;
        --frontend) FRONTEND_ONLY=true ;;
        --logs) SHOW_LOGS=true ;;
        --help) show_help; exit 0 ;;
        *) log_error "未知参数: $arg"; show_help; exit 1 ;;
    esac
done

# 检查 docker 和 docker-compose
if ! command -v docker &> /dev/null; then
    log_error "未找到 docker，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    log_error "未找到 docker-compose，请先安装 Docker Compose"
    exit 1
fi

log_info "========================================"
log_info "   Blog Docker 热更新"
log_info "========================================"

# 可选：拉取最新代码
if [ -d .git ]; then
    log_info "检测到 Git 仓库，是否拉取最新代码? [y/N]"
    read -t 5 -r answer || true
    if [[ "$answer" =~ ^[Yy]$ ]]; then
        log_info "拉取最新代码..."
        git pull
    fi
fi

# 检查 .env 文件
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        log_warn "未找到 .env 文件，从 .env.example 创建"
        cp .env.example .env
        log_warn "请编辑 .env 文件修改默认密码和密钥！"
    fi
fi

# 执行更新
if [ "$FULL_REBUILD" = true ]; then
    log_info "执行完整重建..."
    log_info "停止并移除容器..."
    docker-compose down
    log_info "清理旧镜像..."
    docker-compose rm -f
    log_info "重新构建所有镜像（无缓存）..."
    docker-compose build --no-cache
    log_info "启动所有服务..."
    docker-compose up -d
elif [ "$BACKEND_ONLY" = true ]; then
    log_info "只更新后端服务..."
    docker-compose stop backend
    docker-compose rm -f backend
    docker-compose build --no-cache backend
    docker-compose up -d backend
elif [ "$FRONTEND_ONLY" = true ]; then
    log_info "只更新前端服务..."
    docker-compose stop frontend
    docker-compose rm -f frontend
    docker-compose build --no-cache frontend
    docker-compose up -d frontend
else
    log_info "执行增量更新..."
    docker-compose up -d --build
fi

# 等待服务启动
log_info "等待服务启动..."
sleep 5

# 检查容器状态
log_info "检查容器状态..."
docker-compose ps

# 健康检查
log_info "执行健康检查..."
HEALTH_URL="http://localhost:8080/api/v1/articles?page=1&size=1"
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s "$HEALTH_URL" > /dev/null 2>&1; then
        log_info "后端服务健康检查通过"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        log_warn "健康检查超时，但服务可能仍在启动中"
    else
        echo -n "."
        sleep 2
    fi
done

echo ""
log_info "========================================"
log_info "   热更新完成"
log_info "========================================"
log_info "博客首页: http://localhost"
log_info "后端API:  http://localhost:8080"
log_info ""
log_info "常用命令:"
log_info "  查看日志: docker-compose logs -f"
log_info "  查看后端: docker-compose logs -f backend"
log_info "  查看前端: docker-compose logs -f frontend"
log_info "  停止服务: docker-compose down"
log_info "  重启服务: docker-compose restart"

if [ "$SHOW_LOGS" = true ]; then
    log_info ""
    log_info "显示日志（按 Ctrl+C 退出）..."
    docker-compose logs -f
fi
