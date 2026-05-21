<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-section">
        <h4>友链</h4>
        <div class="friend-links">
          <a v-for="link in friendLinks" :key="link.id" :href="link.url" target="_blank" class="friend-link">
            {{ link.name }}
          </a>
        </div>
      </div>

      <div class="footer-section">
        <h4>关注我</h4>
        <div class="social-links">
          <a :href="github" target="_blank" class="social-link">
            <el-icon style="color: #8fa8f7; filter: drop-shadow(0 1px 2px rgba(143,168,247,0.3));"><Platform /></el-icon> GitHub
          </a>
          <a :href="bilibili" target="_blank" class="social-link">
            <el-icon style="color: #f0a8d8; filter: drop-shadow(0 1px 2px rgba(240,168,216,0.3));"><VideoPlay /></el-icon> B站
          </a>
        </div>
      </div>

      <div class="footer-section">
        <h4>访客统计</h4>
        <div class="stats">
          <span>总PV: {{ stats.totalPv || 0 }}</span>
          <span>总UV: {{ stats.totalUv || 0 }}</span>
          <span>今日PV: {{ stats.todayPv || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="copyright">
      © LZZ's blog
    </div>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFriendLinks } from '@/api/friendLink'
import { getVisitorStats } from '@/api/visitor'
import { getSiteConfig } from '@/api/siteConfig'

const DEFAULTS = {
  contact_github: 'https://github.com/zizheng615',
  contact_bilibili: 'https://space.bilibili.com/291245814',
}

const friendLinks = ref([])
const stats = ref({})
const siteConfig = ref({ ...DEFAULTS })

const github = computed(() => siteConfig.value.contact_github || DEFAULTS.contact_github)
const bilibili = computed(() => siteConfig.value.contact_bilibili || DEFAULTS.contact_bilibili)

onMounted(async () => {
  try {
    const [links, visitorStats, config] = await Promise.all([
      getFriendLinks().catch(() => []),
      getVisitorStats().catch(() => ({})),
      getSiteConfig().catch(() => ({})),
    ])
    friendLinks.value = links || []
    stats.value = visitorStats || {}
    if (config && typeof config === 'object') {
      siteConfig.value = { ...DEFAULTS, ...config }
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.footer {
  background: linear-gradient(135deg, #2d3a4f 0%, #3a4a63 50%, #4a5d7a 100%);
  color: #ecf0f1;
  padding: 40px 0 20px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.footer-section {
  h4 {
    font-size: 1.1em;
    margin-bottom: 16px;
    color: #fff;
  }
}

.friend-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.friend-link {
  color: #bdc3c7;
  font-size: 0.9em;
  transition: color 0.3s;

  &:hover {
    color: #3498db;
  }
}

.social-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #bdc3c7;
  transition: color 0.3s;

  &:hover {
    color: #3498db;
  }
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.9em;
  color: #bdc3c7;
}

.copyright {
  text-align: center;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  font-size: 0.85em;
  color: #95a5a6;
}

@media (max-width: 768px) {
  .footer-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>
