#!/bin/bash
# 一键配置域名 + HTTPS（Let's Encrypt 免费证书）
# 用法: sudo ./setup-ssl.sh yourdomain.com

set -e

DOMAIN="${1:-}"

if [ -z "$DOMAIN" ]; then
    echo "用法: sudo $0 yourdomain.com"
    echo "示例: sudo $0 example.com"
    exit 1
fi

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

# 检查 root 权限
if [ "$EUID" -ne 0 ]; then
    echo "请使用 sudo 运行"
    exit 1
fi

log_info "========================================"
log_info "   域名 + HTTPS 配置"
log_info "   域名: $DOMAIN"
log_info "========================================"

# 1. 安装 Nginx
log_info "安装 Nginx..."
if command -v apt &> /dev/null; then
    apt update
    apt install -y nginx
elif command -v yum &> /dev/null; then
    yum install -y nginx
else
    echo "不支持的系统，请手动安装 Nginx"
    exit 1
fi

# 2. 启动 Nginx
systemctl start nginx
systemctl enable nginx

# 3. 安装 Certbot
log_info "安装 Certbot..."
if command -v apt &> /dev/null; then
    apt install -y certbot python3-certbot-nginx
elif command -v yum &> /dev/null; then
    yum install -y certbot python3-certbot-nginx
fi

# 4. 复制配置文件
log_info "配置 Nginx..."
mkdir -p /var/www/certbot

cp nginx/blog.conf /etc/nginx/conf.d/blog.conf
sed -i "s/yourdomain.com/$DOMAIN/g" /etc/nginx/conf.d/blog.conf

# 测试配置
nginx -t

# 5. 申请证书
log_info "申请 Let's Encrypt 证书..."
certbot --nginx -d "$DOMAIN" -d "www.$DOMAIN" --non-interactive --agree-tos --email "admin@$DOMAIN"

# 6. 设置自动续期
log_info "配置证书自动续期..."
systemctl enable certbot.timer
systemctl start certbot.timer

# 7. 重启 Nginx
systemctl reload nginx

log_info "========================================"
log_info "   HTTPS 配置完成"
log_info "========================================"
log_info "访问地址:"
log_info "  https://$DOMAIN"
log_info ""
log_info "证书续期状态:"
certbot certificates
