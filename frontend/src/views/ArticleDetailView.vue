<template>
  <div class="article-detail container page-wrapper">
    <div class="detail-layout">
      <!-- 目录 -->
      <aside v-if="tocItems.length > 0" class="toc-sidebar">
        <div class="toc-sticky">
          <div class="toc-header">
            <el-icon><Document /></el-icon>
            <span>目录</span>
          </div>
          <nav class="toc-nav">
            <a
              v-for="item in tocItems"
              :key="item.id"
              :href="`#${item.id}`"
              class="toc-link"
              :class="{
                'toc-active': activeTocId === item.id,
                [`toc-level-${item.level}`]: true
              }"
              @click.prevent="scrollToHeading(item.id)"
            >
              {{ item.text }}
            </a>
          </nav>
        </div>
      </aside>

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
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
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
const tocItems = ref([])
const activeTocId = ref('')
const observer = ref(null)

const sanitizedContent = computed(() => {
  return article.value?.content ? DOMPurify.sanitize(article.value.content, {
    ADD_TAGS: ['video', 'source', 'track', 'iframe', 'embed'],
    ADD_ATTR: ['data-w-e-type', 'data-w-e-is-void', 'data-w-e-is-inline', 'data-value', 'src', 'controls', 'autoplay', 'loop', 'muted', 'poster', 'width', 'height', 'type', 'frameborder', 'allowfullscreen', 'allow', 'style', 'preload', 'playsinline']
  }) : ''
})

const generateToc = () => {
  if (!articleBodyRef.value) return
  const headings = articleBodyRef.value.querySelectorAll('h1, h2, h3, h4')
  const items = []
  headings.forEach((heading, index) => {
    let id = heading.id
    if (!id) {
      id = `heading-${index}`
      heading.id = id
    }
    items.push({
      id,
      text: heading.textContent.trim(),
      level: parseInt(heading.tagName[1])
    })
  })
  tocItems.value = items
}

const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (!element) return
  const offset = 80
  const top = element.getBoundingClientRect().top + window.scrollY - offset
  window.scrollTo({ top, behavior: 'smooth' })
}

const setupScrollObserver = () => {
  if (observer.value) observer.value.disconnect()
  if (tocItems.value.length === 0) return

  const options = {
    root: null,
    rootMargin: '-80px 0px -60% 0px',
    threshold: 0
  }

  observer.value = new IntersectionObserver((entries) => {
    const visible = entries.filter(e => e.isIntersecting)
    if (visible.length > 0) {
      activeTocId.value = visible[0].target.id
    }
  }, options)

  tocItems.value.forEach(item => {
    const el = document.getElementById(item.id)
    if (el) observer.value.observe(el)
  })
}

watch(sanitizedContent, () => {
  if (!articleBodyRef.value) return
  renderFormulaSpans(articleBodyRef.value)
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

  // 生成目录并设置滚动监听
  generateToc()
  requestAnimationFrame(() => {
    setupScrollObserver()
  })
}, { flush: 'post' })

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

onUnmounted(() => {
  if (observer.value) observer.value.disconnect()
})
</script>

<style lang="scss" scoped>
.detail-layout {
  display: grid;
  grid-template-columns: 220px 1fr 300px;
  gap: 24px;
}

.article-main {
  min-width: 0;
}

/* 目录 */
.toc-sidebar {
  display: block;
}

.toc-sticky {
  position: sticky;
  top: 80px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  max-height: calc(100vh - 100px);
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #e0e6ed;
    border-radius: 2px;
  }
}

.toc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ecf0f1;
}

.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toc-link {
  display: block;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 0.85em;
  color: #4a5568;
  text-decoration: none;
  line-height: 1.5;
  transition: all 0.2s ease;
  border-left: 2px solid transparent;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
}

.toc-level-1 {
  font-weight: 600;
  font-size: 0.88em;
}

.toc-level-2 {
  padding-left: 18px;
}

.toc-level-3 {
  padding-left: 32px;
  font-size: 0.82em;
  color: #718096;
}

.toc-level-4 {
  padding-left: 46px;
  font-size: 0.8em;
  color: #a0aec0;
}

.toc-active {
  background: #ecf5ff;
  color: #409eff;
  border-left-color: #409eff;
  font-weight: 500;
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

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr 300px;
  }

  .toc-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-area,
  .toc-sidebar {
    display: none;
  }

  .title {
    font-size: 1.5em;
  }
}
</style>
