<template>
  <div class="admin-layout" :class="{ 'mobile-open': mobileSidebarOpen }">
    <el-container>
      <el-aside class="admin-aside" :width="asideWidth">
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
          @select="onMenuSelect"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/friend-links">
            <el-icon><Link /></el-icon>
            <span>友链管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/contact">
            <el-icon><Message /></el-icon>
            <span>联系我编辑</span>
          </el-menu-item>
          <el-menu-item index="/admin/account">
            <el-icon><Lock /></el-icon>
            <span>账号设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <div
        v-if="isMobile && mobileSidebarOpen"
        class="aside-mask"
        @click="mobileSidebarOpen = false"
      ></div>
      <el-container>
        <el-header class="admin-header">
          <button
            v-if="isMobile"
            class="menu-toggle"
            aria-label="菜单"
            @click="mobileSidebarOpen = !mobileSidebarOpen"
          >
            <el-icon><Menu /></el-icon>
          </button>
          <div class="header-right">
            <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-button type="danger" size="small" @click="logout">
              <el-icon><SwitchButton /></el-icon>
              <span class="logout-text">退出</span>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const viewportWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
const onResize = () => { viewportWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const isMobile = computed(() => viewportWidth.value <= 768)
const mobileSidebarOpen = ref(false)
const asideWidth = computed(() => {
  if (!isMobile.value) return '200px'
  return mobileSidebarOpen.value ? '220px' : '0px'
})

watch(() => route.path, () => {
  if (isMobile.value) mobileSidebarOpen.value = false
})

const onMenuSelect = () => {
  if (isMobile.value) mobileSidebarOpen.value = false
}

const logout = () => {
  authStore.clearAuth()
  router.push('/admin/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  min-height: 100vh;
  position: relative;

  :deep(.el-container) {
    min-height: 100vh;
  }
}

.admin-aside {
  background: #2c3e50;
  transition: width 0.25s ease;
  overflow: hidden;
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
  white-space: nowrap;

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
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
}

.menu-toggle {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.4em;
  color: #2c3e50;
  display: flex;
  align-items: center;
  padding: 6px 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.username {
  color: #606266;
  font-size: 0.95em;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-main {
  background: #f5f7fa;
  padding: 20px;
}

.aside-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1999;
}

@media (max-width: 768px) {
  .admin-aside {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 2000;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }

  .admin-main {
    padding: 12px;
  }

  .username {
    max-width: 80px;
  }

  .logout-text {
    display: none;
  }
}
</style>
