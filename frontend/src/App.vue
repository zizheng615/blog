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
  background: linear-gradient(135deg, #f0f4f8 0%, #f5f0ff 50%, #f8f4f0 100%);
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
</style>
