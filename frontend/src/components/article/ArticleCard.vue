<template>
  <router-link :to="`/articles/${article.id}`" class="article-card">
    <div class="card-content">
      <div class="meta">
        <span class="type-badge" :class="article.articleType?.toLowerCase()">
          {{ article.articleType === 'TECH' ? '技术' : '生活' }}
        </span>
        <span class="date">{{ formatDate(article.publishedAt) }}</span>
      </div>
      <h3 class="title">{{ article.title }}</h3>
      <p class="summary">{{ article.summary }}</p>
      <div class="footer">
        <div class="tags">
          <span v-for="tag in article.tags" :key="tag.id" class="tag" :style="{ color: tag.color }">
            {{ tag.name }}
          </span>
        </div>
        <div class="stats">
          <span><el-icon><View /></el-icon> {{ article.viewCount || 0 }}</span>
          <span><el-icon><ChatLineRound /></el-icon> {{ article.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import dayjs from 'dayjs'

const props = defineProps({
  article: { type: Object, required: true }
})

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}
</script>

<style lang="scss" scoped>
.article-card {
  display: block;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
    border-color: #e0e6ed;
  }
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.type-badge {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 0.75em;
  font-weight: 500;

  &.tech {
    background: #e6f7ff;
    color: #1890ff;
  }

  &.life {
    background: #fff0f6;
    color: #eb2f96;
  }
}

.date {
  font-size: 0.85em;
  color: #a0aec0;
}

.title {
  font-size: 1.2em;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
  line-height: 1.4;
}

.summary {
  font-size: 0.95em;
  color: #718096;
  line-height: 1.7;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  font-size: 0.8em;
  padding: 2px 8px;
  background: #f7fafc;
  border-radius: 4px;
}

.stats {
  display: flex;
  gap: 16px;
  font-size: 0.85em;
  color: #a0aec0;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
