<template>
  <div class="home container page-wrapper">
    <div class="home-layout">
      <div class="main-area">
        <div class="hero-banner">
          <div class="hero-content">
            <h1 class="hero-title">探索技术与生活</h1>
            <p class="hero-subtitle">记录学习的轨迹，分享生活的点滴</p>
          </div>
        </div>
        <div class="section-header">
          <h2>最新文章</h2>
          <router-link to="/articles" class="more">查看全部 <el-icon class="icon-glow-purple"><ArrowRight /></el-icon></router-link>
        </div>
        <div class="article-grid">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
      </div>
      <SideBar class="sidebar-area" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import SideBar from '@/components/common/SideBar.vue'

const articles = ref([])

onMounted(async () => {
  try {
    const res = await getArticles({ page: 1, size: 6 })
    articles.value = res.list || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.home-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.main-area {
  min-width: 0;
}

.hero-banner {
  background: linear-gradient(135deg, #8fa8f7 0%, #a893d1 100%);
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 1.25em;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
  letter-spacing: 0.02em;
}

.hero-subtitle {
  font-size: 0.88em;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 1.3em;
    font-weight: 600;
    color: #2c3e50;
  }

  .more {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 0.88em;
    font-weight: 500;
    color: #7b96e6;
    background: rgba(123, 150, 230, 0.08);
    border: 1px solid rgba(123, 150, 230, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .el-icon {
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 0.9em;
    }

    &:hover {
      background: rgba(123, 150, 230, 0.14);
      border-color: rgba(123, 150, 230, 0.35);
      color: #6a85d6;

      .el-icon {
        transform: translateX(3px);
      }
    }
  }
}

.article-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-area {
  position: sticky;
  top: 80px;
  align-self: start;
}

@media (max-width: 768px) {
  .home-layout {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .sidebar-area {
    position: static;
  }

  .hero-banner {
    padding: 16px 20px;
  }

  .hero-title {
    font-size: 1.1em;
  }

  .hero-subtitle {
    font-size: 0.85em;
  }
}
</style>
