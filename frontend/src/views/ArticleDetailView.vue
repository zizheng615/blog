<template>
  <div class="article-detail container page-wrapper">
    <!-- 目录悬浮按钮 -->
    <button
      v-if="tocItems.length > 0"
      class="toc-float-btn"
      :class="{ 'toc-float-btn-active': tocVisible }"
      @click="tocVisible = !tocVisible"
      title="目录"
    >
      <el-icon><Document /></el-icon>
      <span class="toc-float-label">目录</span>
    </button>

    <!-- 目录抽屉 -->
    <transition name="toc-drawer">
      <aside
        v-if="tocVisible && tocItems.length > 0"
        class="toc-drawer"
        @click.stop
      >
        <div class="toc-drawer-inner">
          <div class="toc-drawer-header">
            <span class="toc-drawer-title">
              <el-icon><Document /></el-icon>
              目录
            </span>
            <button class="toc-drawer-close" @click="tocVisible = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <nav class="toc-drawer-nav">
            <a
              v-for="item in tocItems"
              :key="item.id"
              :href="`#${item.id}`"
              class="toc-drawer-link"
              :class="{
                'toc-drawer-active': activeTocId === item.id,
                [`toc-drawer-level-${item.level}`]: true
              }"
              @click.prevent="scrollToHeading(item.id)"
            >
              {{ item.text }}
            </a>
          </nav>
        </div>
      </aside>
    </transition>

    <!-- 遮罩层 -->
    <transition name="toc-fade">
      <div
        v-if="tocVisible && tocItems.length > 0"
        class="toc-overlay"
        @click="tocVisible = false"
      ></div>
    </transition>

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
            <div class="tags-header" @click="tagsExpanded = !tagsExpanded">
              <div class="tags-header-left">
                <el-icon><PriceTag /></el-icon>
                <span class="tags-title">标签</span>
                <span class="tags-count" v-if="article.tags?.length">({{ article.tags.length }})</span>
              </div>
              <el-icon class="tags-toggle-icon" :class="{ 'tags-toggle-icon-rotated': !tagsExpanded }">
                <ArrowDown />
              </el-icon>
            </div>
            <transition name="tags-collapse">
              <div v-show="tagsExpanded" class="tags-body">
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
            </transition>
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
const tocVisible = ref(false)
const tagsExpanded = ref(true)

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
  tocVisible.value = false
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
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.article-main {
  min-width: 0;
}

/* 目录悬浮按钮 */
.toc-float-btn {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: white;
  border: 1px solid #e0e6ed;
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  font-size: 0.85em;
  color: #4a5568;
  transition: all 0.25s ease;

  &:hover {
    color: #409eff;
    border-color: #409eff;
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
  }
}

.toc-float-btn-active {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
}

.toc-float-label {
  font-size: 0.85em;
}

/* 目录抽屉 */
.toc-drawer {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 200;
  width: 280px;
  padding: 16px;
  pointer-events: none;
}

.toc-drawer-inner {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  height: 100%;
  display: flex;
  flex-direction: column;
  pointer-events: auto;
  overflow: hidden;
}

.toc-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.toc-drawer-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  font-weight: 600;
  color: #2c3e50;
}

.toc-drawer-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
}

.toc-drawer-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 2px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #e0e6ed;
    border-radius: 2px;
  }
}

.toc-drawer-link {
  display: block;
  padding: 7px 12px;
  border-radius: 6px;
  font-size: 0.85em;
  color: #4a5568;
  text-decoration: none;
  line-height: 1.5;
  transition: all 0.2s ease;
  border-left: 2px solid transparent;
  word-break: break-all;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
}

.toc-drawer-level-1 {
  font-weight: 600;
  font-size: 0.88em;
}

.toc-drawer-level-2 {
  padding-left: 22px;
}

.toc-drawer-level-3 {
  padding-left: 38px;
  font-size: 0.82em;
  color: #718096;
}

.toc-drawer-level-4 {
  padding-left: 54px;
  font-size: 0.8em;
  color: #a0aec0;
}

.toc-drawer-active {
  background: #ecf5ff;
  color: #409eff;
  border-left-color: #409eff;
  font-weight: 500;
}

/* 遮罩 */
.toc-overlay {
  position: fixed;
  inset: 0;
  z-index: 150;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(2px);
}

/* 过渡动画 */
.toc-drawer-enter-active,
.toc-drawer-leave-active {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.25s ease;
}

.toc-drawer-enter-from,
.toc-drawer-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.toc-fade-enter-active,
.toc-fade-leave-active {
  transition: opacity 0.25s ease;
}

.toc-fade-enter-from,
.toc-fade-leave-to {
  opacity: 0;
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
  padding: 0;
  margin-top: 1px;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

.tags-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 32px;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s ease;

  &:hover {
    background: #fafafa;
  }
}

.tags-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-size: 0.9em;
  font-weight: 500;
}

.tags-title {
  font-weight: 600;
}

.tags-count {
  color: #a0aec0;
  font-size: 0.85em;
}

.tags-toggle-icon {
  color: #a0aec0;
  transition: transform 0.25s ease;
  font-size: 0.9em;
}

.tags-toggle-icon-rotated {
  transform: rotate(-90deg);
}

.tags-body {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: 0 32px 18px;

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

/* 标签折叠过渡动画 */
.tags-collapse-enter-active,
.tags-collapse-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.tags-collapse-enter-from,
.tags-collapse-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.tags-collapse-enter-to,
.tags-collapse-leave-from {
  opacity: 1;
  max-height: 200px;
}

.sidebar-area {
  position: sticky;
  top: 80px;
  align-self: start;
}

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr 260px;
    gap: 16px;
  }

  .toc-float-btn {
    left: 12px;
  }
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-area,
  .toc-float-btn,
  .toc-drawer,
  .toc-overlay {
    display: none !important;
  }

  .title {
    font-size: 1.5em;
  }
}
</style>
