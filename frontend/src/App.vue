<template>
  <div class="app">
    <NavBar />
    <main class="main-content">
      <router-view />
    </main>
    <FooterBar />
  </div>
</template>

<script setup>
import NavBar from './components/common/NavBar.vue'
import FooterBar from './components/common/FooterBar.vue'
</script>

<style lang="scss">
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #f8f6fc 50%, #faf8f5 100%);
  background-attachment: fixed;
}

.main-content {
  flex: 1;
  padding-top: 60px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #333;
  line-height: 1.6;
}

a {
  text-decoration: none;
  color: inherit;
}

/* 图标彩色立体效果工具类 */
.icon-glow-blue {
  color: #8fa8f7 !important;
  filter: drop-shadow(0 1px 2px rgba(143, 168, 247, 0.3));
}

.icon-glow-pink {
  color: #f0a8d8 !important;
  filter: drop-shadow(0 1px 2px rgba(240, 168, 216, 0.3));
}

.icon-glow-purple {
  color: #a893d1 !important;
  filter: drop-shadow(0 1px 2px rgba(168, 147, 209, 0.3));
}

.icon-glow-gray {
  color: #b8c5d6 !important;
  filter: drop-shadow(0 1px 2px rgba(184, 197, 214, 0.25));
}
</style>
