# 前端 Dockerfile - Vue 3 + Vite + Nginx
# 多阶段构建：先构建静态文件，再用 Nginx 托管

# 阶段1：构建
FROM node:22-alpine AS builder

WORKDIR /app

# 先复制 package.json 缓存依赖层
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# 阶段2：运行（Nginx）
FROM nginx:alpine

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建产物到 /blog 子目录（与 vite.config.js 的 base: '/blog/' 保持一致）
COPY --from=builder /app/dist /usr/share/nginx/html/blog

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/nginx-health > /dev/null || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
