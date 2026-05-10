<template>
  <div class="home container page-wrapper">
    <div class="home-layout">
      <div class="main-area">
        <div class="section-header">
          <h2>最新文章</h2>
          <router-link to="/articles" class="more">查看全部 <el-icon><ArrowRight /></el-icon></router-link>
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
    gap: 4px;
    color: #409eff;
    font-size: 0.9em;

    &:hover {
      text-decoration: underline;
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
  }

  .sidebar-area {
    position: static;
  }
}
</style>
