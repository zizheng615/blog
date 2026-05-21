<template>
  <router-link
    :to="`/articles/${article.id}`"
    class="article-card"
    :data-type="article.articleType?.toLowerCase()"
  >
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
          <span
            v-for="tag in article.tags"
            :key="tag.id"
            class="tag"
            :style="tagStyle(tag.color)"
          >
            {{ tag.name }}
          </span>
        </div>
        <div class="stats">
          <span><el-icon class="icon-glow-blue"><View /></el-icon> {{ article.viewCount || 0 }}</span>
          <span><el-icon class="icon-glow-purple"><ChatLineRound /></el-icon> {{ article.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import dayjs from 'dayjs'
import { readableColor, rgbaBg } from '@/utils/color'

const props = defineProps({
  article: { type: Object, required: true }
})

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const tagStyle = (color) => ({
  color: readableColor(color),
  backgroundColor: rgbaBg(color, 0.14),
})
</script>

<style lang="scss" scoped>
.article-card {
  display: block;
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 24px 24px 24px 28px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 16px;
    bottom: 16px;
    width: 3px;
    border-radius: 0 3px 3px 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    transform: translateY(-4px);
    border-color: #e0e6ed;

    &::before {
      top: 12px;
      bottom: 12px;
      width: 4px;
      border-radius: 0 4px 4px 0;
    }
  }

  &[data-type='tech']::before {
    background: linear-gradient(180deg, #8fa8f7 0%, #7bb3f0 100%);
  }

  &[data-type='tech']:hover {
    box-shadow: 0 8px 24px rgba(143, 168, 247, 0.14);
  }

  &[data-type='life']::before {
    background: linear-gradient(180deg, #f0a8d8 0%, #e88ab5 100%);
  }

  &[data-type='life']:hover {
    box-shadow: 0 8px 24px rgba(240, 168, 216, 0.14);
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
  position: relative;
  transition: all 0.3s ease;

  &.tech {
    background: linear-gradient(135deg, #eef4ff 0%, #f5f8ff 100%);
    color: #7b96e6;

    &::after {
      background: linear-gradient(90deg, #8fa8f7 0%, #a893d1 100%);
    }
  }

  &.life {
    background: linear-gradient(135deg, #fef0f6 0%, #fff5fa 100%);
    color: #d484b0;

    &::after {
      background: linear-gradient(90deg, #f0a8d8 0%, #e88ab5 100%);
    }
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 50%;
    width: 0;
    height: 2px;
    border-radius: 1px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    transform: translateX(-50%);
  }

  &:hover::after {
    width: 60%;
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
