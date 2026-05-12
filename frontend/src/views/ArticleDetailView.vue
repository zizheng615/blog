<template>
  <div class="article-detail container page-wrapper">
    <div class="detail-layout">
      <div class="article-main">
        <div v-if="loading" class="loading">
          <el-skeleton :rows="10" animated />
        </div>
        <template v-else-if="article">
          <div class="article-header">
            <div class="meta">
              <span class="type-badge" :class="article.articleType?.toLowerCase()">
                {{ article.articleType === 'TECH' ? '技术' : '生活' }}
              </span>
              <span class="category">{{ article.category?.name }}</span>
              <span class="date">{{ formatDate(article.publishedAt) }}</span>
            </div>
            <h1 class="title">{{ article.title }}</h1>
            <div class="stats">
              <span><el-icon><View /></el-icon> {{ article.viewCount || 0 }}</span>
              <span><el-icon><ChatLineRound /></el-icon> {{ article.commentCount || 0 }}</span>
            </div>
          </div>

          <div
            ref="articleBodyRef"
            class="article-body"
            :class="article.articleType === 'TECH' ? 'article-tech' : 'article-life'"
            v-html="sanitizedContent"
          ></div>

          <div class="article-footer">
            <div class="tags">
              <router-link
                v-for="tag in article.tags"
                :key="tag.id"
                :to="{ path: '/articles', query: { tagId: tag.id } }"
                class="tag"
                :style="tagStyle(tag.color)"
              >
                #{{ tag.name }}
              </router-link>
            </div>
          </div>

          <CommentSection :articleId="article.id" />
        </template>
      </div>
      <SideBar class="sidebar-area" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import DOMPurify from 'dompurify'
import katex from 'katex'
import 'katex/dist/katex.min.css'
import renderMathInElement from 'katex/contrib/auto-render'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { getArticleById } from '@/api/article'
import { recordVisit } from '@/api/visitor'
import { readableColor, rgbaBg } from '@/utils/color'
import CommentSection from '@/components/article/CommentSection.vue'
import SideBar from '@/components/common/SideBar.vue'

const route = useRoute()
const article = ref(null)
const loading = ref(true)
const articleBodyRef = ref(null)

const sanitizedContent = computed(() => {
  return article.value?.content ? DOMPurify.sanitize(article.value.content, {
    ADD_TAGS: ['video', 'source', 'track', 'iframe', 'embed'],
    ADD_ATTR: ['data-w-e-type', 'data-w-e-is-void', 'data-w-e-is-inline', 'data-value', 'src', 'controls', 'autoplay', 'loop', 'muted', 'poster', 'width', 'height', 'type', 'frameborder', 'allowfullscreen', 'allow', 'style', 'preload', 'playsinline']
  }) : ''
})

const renderFormulaSpans = (container) => {
  const spans = container.querySelectorAll('[data-w-e-type="formula"]')
  spans.forEach(span => {
    const latex = span.getAttribute('data-value')
    if (!latex) return
    const isInline = span.hasAttribute('data-w-e-is-inline')
    try {
      katex.render(latex, span, {
        displayMode: !isInline,
        throwOnError: false
      })
    } catch (e) {
      span.textContent = latex
    }
  })
}

watch(sanitizedContent, () => {
  if (!articleBodyRef.value) return
  renderFormulaSpans(articleBodyRef.value)
  // Syntax-highlight code blocks
  articleBodyRef.value.querySelectorAll('pre code').forEach((block) => {
    try { hljs.highlightElement(block) } catch (e) { /* best-effort */ }
  })
  renderMathInElement(articleBodyRef.value, {
    delimiters: [
      { left: '$$', right: '$$', display: true },
      { left: '$', right: '$', display: false },
      { left: '\\(', right: '\\)', display: false },
      { left: '\\[', right: '\\]', display: true }
    ],
    ignoredTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code'],
    ignoredClasses: ['katex'],
    throwOnError: false
  })
}, { flush: 'post' })

const tagStyle = (color) => ({
  color: readableColor(color),
  backgroundColor: rgbaBg(color, 0.14),
})

const loadArticle = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    article.value = await getArticleById(id)
    recordVisit({ pageUrl: window.location.href })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

onMounted(loadArticle)
</script>

<style lang="scss" scoped>
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.article-main {
  min-width: 0;
}

.loading {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.article-header {
  background: white;
  border-radius: 12px 12px 0 0;
  padding: 32px;
  border-bottom: 1px solid #e0e6ed;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.type-badge {
  padding: 2px 12px;
  border-radius: 4px;
  font-size: 0.8em;
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

.category {
  color: #409eff;
  font-size: 0.9em;
}

.date {
  color: #a0aec0;
  font-size: 0.9em;
}

.title {
  font-size: 2em;
  font-weight: 700;
  color: #1a202c;
  line-height: 1.4;
  margin-bottom: 12px;
}

.stats {
  display: flex;
  gap: 20px;
  color: #a0aec0;
  font-size: 0.9em;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.article-body {
  background: white;
  padding: 32px;
  border-radius: 0 0 12px 12px;
  min-height: 300px;
}

.article-footer {
  background: white;
  padding: 16px 32px;
  margin-top: 1px;
  border-radius: 0;

  .tags {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    .tag {
      font-size: 0.9em;
      padding: 4px 12px;
      border-radius: 4px;
      text-decoration: none;
      transition: transform 0.15s ease, box-shadow 0.15s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
      }
    }
  }
}

.sidebar-area {
  position: sticky;
  top: 80px;
  align-self: start;
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-area {
    display: none;
  }

  .title {
    font-size: 1.5em;
  }
}
</style>
