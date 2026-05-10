# 前端 Dockerfile - 直接将本地预构建好的 dist/ 拷贝进 Nginx
# 构建在宿主机执行（npm run build），容器内只负责静态托管，不再走 Node 构建阶段。

FROM nginx:alpine

# 自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制本地构建产物到 /blog 子目录（与 vite.config.js 的 base: '/blog/' 对齐）
COPY dist /usr/share/nginx/html/blog

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/nginx-health > /dev/null || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
