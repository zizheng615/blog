<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="logo">
        <el-icon><Notebook /></el-icon>
        <span>我的博客</span>
      </router-link>

      <div class="nav-links" :class="{ open: menuOpen }">
        <router-link
          :to="{ path: '/articles', query: { type: 'TECH' } }"
          class="nav-link"
          :class="{ 'is-active': isTypeActive('TECH') }"
          active-class=""
          exact-active-class=""
        >
          <el-icon><Cpu /></el-icon>技术文章
        </router-link>
        <router-link
          :to="{ path: '/articles', query: { type: 'LIFE' } }"
          class="nav-link"
          :class="{ 'is-active': isTypeActive('LIFE') }"
          active-class=""
          exact-active-class=""
        >
          <el-icon><Coffee /></el-icon>生活分享
        </router-link>
        <router-link to="/contact" class="nav-link">
          <el-icon><Message /></el-icon>联系我
        </router-link>
      </div>

      <button class="menu-toggle" @click="menuOpen = !menuOpen">
        <el-icon><Menu /></el-icon>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
const menuOpen = ref(false)
const route = useRoute()
const isTypeActive = (type) => route.path === '/articles' && route.query.type === type
</script>

<style lang="scss" scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e0e6ed;
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.3em;
  font-weight: 600;
  color: #409eff;

  .el-icon {
    font-size: 1.2em;
  }
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.95em;
  color: #606266;
  transition: all 0.3s;

  &:hover, &.router-link-active, &.is-active {
    background: #ecf5ff;
    color: #409eff;
  }
}

.menu-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  color: #606266;
}

@media (max-width: 768px) {
  .nav-links {
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    flex-direction: column;
    background: white;
    padding: 16px;
    border-bottom: 1px solid #e0e6ed;
    transform: translateY(-100%);
    opacity: 0;
    pointer-events: none;
    transition: all 0.3s;

    &.open {
      transform: translateY(0);
      opacity: 1;
      pointer-events: auto;
    }
  }

  .menu-toggle {
    display: block;
  }
}
</style>
