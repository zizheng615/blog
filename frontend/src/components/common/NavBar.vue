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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px) saturate(1.2);
  -webkit-backdrop-filter: blur(16px) saturate(1.2);
  border-bottom: 1px solid rgba(224, 230, 237, 0.5);
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
  font-weight: 700;
  background: linear-gradient(135deg, #7b96e6 0%, #9a7fc2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;

  .el-icon {
    font-size: 1.2em;
    background: linear-gradient(135deg, #7b96e6 0%, #9a7fc2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
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
  position: relative;
  transition: color 0.3s ease;

  &::after {
    content: '';
    position: absolute;
    bottom: 2px;
    left: 50%;
    width: 0;
    height: 2px;
    border-radius: 1px;
    background: linear-gradient(90deg, #8fa8f7 0%, #a893d1 100%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    transform: translateX(-50%);
  }

  &:hover {
    color: #7b96e6;
  }

  &:hover::after {
    width: calc(100% - 32px);
  }

  &.router-link-active,
  &.is-active {
    color: #7b96e6;
    font-weight: 500;
  }

  &.router-link-active::after,
  &.is-active::after {
    width: calc(100% - 32px);
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
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(16px);
    padding: 16px;
    border-bottom: 1px solid rgba(224, 230, 237, 0.5);
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
