<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px" class="admin-aside">
        <div class="admin-logo">
          <el-icon><Notebook /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          class="admin-menu"
          background-color="#2c3e50"
          text-color="#ecf0f1"
          active-text-color="#409eff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="admin-header">
          <div class="header-right">
            <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-button type="danger" size="small" @click="logout">
              <el-icon><SwitchButton /></el-icon> 退出
            </el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const logout = () => {
  authStore.clearAuth()
  router.push('/admin/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  min-height: 100vh;

  :deep(.el-container) {
    min-height: 100vh;
  }
}

.admin-aside {
  background: #2c3e50;
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  color: white;
  font-size: 1.1em;
  font-weight: 600;
  border-bottom: 1px solid #34495e;

  .el-icon {
    font-size: 1.3em;
  }
}

.admin-menu {
  border-right: none;
}

.admin-header {
  background: white;
  border-bottom: 1px solid #e0e6ed;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #606266;
  font-size: 0.95em;
}

.admin-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
