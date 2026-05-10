<template>
  <div class="article-list container page-wrapper">
    <div class="list-layout">
      <div class="main-area">
        <div class="filter-bar">
          <h2>{{ pageTitle }}</h2>
          <div class="filter-tags">
            <el-tag
              v-if="activeFilter"
              closable
              @close="clearFilter"
              type="primary"
            >
              {{ activeFilter }}
            </el-tag>
          </div>
        </div>
        <div class="articles">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadArticles"
          />
        </div>
      </div>
      <SideBar class="sidebar-area" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import SideBar from '@/components/common/SideBar.vue'

const route = useRoute()
const router = useRouter()

const articles = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryId = computed(() => route.query.categoryId)
const tagId = computed(() => route.query.tagId)
const articleType = computed(() => route.query.type)

const pageTitle = computed(() => {
  if (articleType.value === 'TECH') return '技术文章'
  if (articleType.value === 'LIFE') return '生活分享'
  if (categoryId.value) return '分类文章'
  if (tagId.value) return '标签文章'
  return '全部文章'
})

const activeFilter = computed(() => {
  if (articleType.value) return articleType.value === 'TECH' ? '技术文章' : '生活分享'
  return null
})

const loadArticles = async () => {
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...(categoryId.value && { categoryId: categoryId.value }),
      ...(tagId.value && { tagId: tagId.value }),
      ...(articleType.value && { type: articleType.value })
    }
    const res = await getArticles(params)
    articles.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  }
}

const clearFilter = () => {
  router.push('/articles')
}

watch(() => route.query, loadArticles, { deep: true })
onMounted(loadArticles)
</script>

<style lang="scss" scoped>
.list-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.main-area {
  min-width: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 1.3em;
    font-weight: 600;
    color: #2c3e50;
  }
}

.articles {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.sidebar-area {
  position: sticky;
  top: 80px;
  align-self: start;
}

@media (max-width: 768px) {
  .list-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-area {
    display: none;
  }
}
</style>
