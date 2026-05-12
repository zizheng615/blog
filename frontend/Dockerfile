# 前端 Dockerfile - 多阶段构建
# 阶段 1: 用 Node 容器内构建静态文件(无需服务器装 Node.js)
# 阶段 2: 用 Nginx 托管 dist/ 产物

# ===== 阶段 1: 构建 =====
FROM node:22-alpine AS builder

WORKDIR /app

# 国内 npm 加速(阿里云镜像)
RUN npm config set registry https://registry.npmmirror.com

# 优先复制锁文件,利用 Docker 层缓存(代码改动不会重装依赖)
COPY package*.json ./

# 用 ci 走锁文件版本,不写 lock,关闭 audit/fund 提速
# 加 legacy-peer-deps: wangeditor 公式插件 peer 上 katex@0.15 而项目装的是 0.16(API 兼容)
RUN npm ci --no-audit --no-fund --legacy-peer-deps

# 再拷源码,前面的依赖层可以复用
COPY . .

# 给 Node 多分点堆内存,避免 2G 小机器构建时 OOM
ENV NODE_OPTIONS="--max-old-space-size=1536"

RUN npm run build


# ===== 阶段 2: Nginx =====
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

# 与 vite.config.js 的 base: '/blog/' 对齐
COPY --from=builder /app/dist /usr/share/nginx/html/blog

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/nginx-health > /dev/null || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
