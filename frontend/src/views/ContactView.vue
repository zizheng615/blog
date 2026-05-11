<template>
  <div class="contact container page-wrapper">
    <div class="contact-card">
      <div class="contact-header">
        <el-icon :size="48" color="#409eff"><Message /></el-icon>
        <h2>联系我</h2>
        <p>有任何问题或建议，欢迎随时交流</p>
      </div>

      <div class="contact-info">
        <div class="info-item">
          <el-icon><Message /></el-icon>
          <div>
            <h4>邮箱</h4>
            <a :href="`mailto:${email}`">{{ email }}</a>
          </div>
        </div>
        <div class="info-item">
          <el-icon><Platform /></el-icon>
          <div>
            <h4>GitHub</h4>
            <a :href="github" target="_blank">{{ githubLabel }}</a>
          </div>
        </div>
        <div class="info-item">
          <el-icon><VideoPlay /></el-icon>
          <div>
            <h4>哔哩哔哩</h4>
            <a :href="bilibili" target="_blank">{{ bilibiliLabel }}</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSiteConfig } from '@/api/siteConfig'

const DEFAULTS = {
  contact_email: '2788906816@qq.com',
  contact_github: 'https://github.com/zizheng615',
  contact_bilibili: 'https://space.bilibili.com/291245814',
}

const config = ref({ ...DEFAULTS })

const email = computed(() => config.value.contact_email || DEFAULTS.contact_email)
const github = computed(() => config.value.contact_github || DEFAULTS.contact_github)
const bilibili = computed(() => config.value.contact_bilibili || DEFAULTS.contact_bilibili)

const stripProtocol = (url) =>
  (url || '').replace(/^https?:\/\//, '').replace(/\/$/, '')

const githubLabel = computed(() => stripProtocol(github.value))
const bilibiliLabel = computed(() => stripProtocol(bilibili.value))

onMounted(async () => {
  try {
    const data = await getSiteConfig()
    if (data && typeof data === 'object') {
      config.value = { ...DEFAULTS, ...data }
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.contact {
  max-width: 600px;
}

.contact-card {
  background: white;
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  text-align: center;
}

.contact-header {
  margin-bottom: 40px;

  h2 {
    font-size: 1.8em;
    font-weight: 600;
    color: #2c3e50;
    margin: 16px 0 8px;
  }

  p {
    color: #718096;
  }
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: #f8fafc;
  border-radius: 12px;
  text-align: left;

  .el-icon {
    font-size: 1.5em;
    color: #409eff;
  }

  h4 {
    font-size: 0.95em;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 4px;
  }

  p, a {
    color: #409eff;
    font-size: 0.9em;
    word-break: break-all;
  }
}
</style>
