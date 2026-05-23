<template>
  <div class="article-detail container page-wrapper">
    <!-- 目录悬浮按钮 -->
    <div
      v-if="tocItems.length > 0 && !tocVisible"
      ref="tocBtnRef"
      class="toc-float-btn"
      :class="{
        'toc-float-btn-active': tocVisible,
        'toc-float-btn-dragging': isDragging
      }"
      :style="btnStyle"
      @click="onBtnClick"
      @mousedown="onDragStart"
      @touchstart="onDragStart"
      title="目录"
    >
      <div class="toc-float-ring">
        <el-icon><Document /></el-icon>
      </div>
    </div>

    <div class="detail-layout" :class="{ 'has-toc': tocVisible && tocItems.length > 0 }">
      <aside
        v-if="tocItems.length > 0"
        v-show="tocVisible"
        class="toc-sidebar"
      >
        <div class="toc-sidebar-inner">
          <div class="toc-sidebar-header">
            <span class="toc-sidebar-title">
              <el-icon class="icon-glow-purple"><Document /></el-icon>
              目录
            </span>
            <button class="toc-sidebar-close" @click="tocVisible = false">
              <el-icon class="icon-glow-gray"><Close /></el-icon>
            </button>
          </div>
          <nav ref="tocNavRef" class="toc-sidebar-nav">
            <a
              v-for="item in tocItems"
              :key="item.id"
              :ref="el => { if (el) tocLinkRefs[item.id] = el }"
              :href="`#${item.id}`"
              class="toc-sidebar-link"
              :class="{
                'toc-sidebar-active': activeTocId === item.id,
                [`toc-sidebar-level-${item.level}`]: true
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
              <span><el-icon class="icon-glow-blue"><View /></el-icon> {{ article.viewCount || 0 }}</span>
              <span><el-icon class="icon-glow-purple"><ChatLineRound /></el-icon> {{ article.commentCount || 0 }}</span>
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
                <el-icon class="icon-glow-pink"><PriceTag /></el-icon>
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

        </template>
      </div>
      <SideBar class="sidebar-area" />
    </div>
    <div class="comment-layout">
      <div v-show="tocVisible && tocItems.length > 0" class="comment-spacer"></div>
      <CommentSection v-if="article" :articleId="article.id" class="comment-main" />
      <div class="comment-sidebar-spacer"></div>
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
const tocVisible = ref(true)
const tagsExpanded = ref(true)

// 目录滚动相关
const tocNavRef = ref(null)
const tocLinkRefs = ref({})

// 拖拽相关
const tocBtnRef = ref(null)
const isDragging = ref(false)
const hasDragged = ref(false)
const btnPos = ref({ x: 0, y: 0 })
const dragOffset = ref({ x: 0, y: 0 })

const TOC_POS_KEY = 'toc-btn-position'

const loadSavedPosition = () => {
  try {
    const saved = localStorage.getItem(TOC_POS_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      btnPos.value = { x: parsed.x || 0, y: parsed.y || 0 }
      return
    }
  } catch (e) { /* ignore */ }
  // 默认位置：左侧居中偏上一点
  btnPos.value = { x: 0, y: -120 }
}

const savePosition = () => {
  try {
    localStorage.setItem(TOC_POS_KEY, JSON.stringify(btnPos.value))
  } catch (e) { /* ignore */ }
}

const btnStyle = computed(() => ({
  transform: `translate3d(${btnPos.value.x}px, ${btnPos.value.y}px, 0)`
}))

const onDragStart = (e) => {
  if (!tocBtnRef.value) return
  isDragging.value = true
  hasDragged.value = false

  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY

  const rect = tocBtnRef.value.getBoundingClientRect()
  dragOffset.value = {
    x: clientX - rect.left,
    y: clientY - rect.top
  }

  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDragMove, { passive: false })
  document.addEventListener('touchend', onDragEnd)
}

const onDragMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()

  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY

  const btnSize = 48
  const margin = 16

  let newX = clientX - dragOffset.value.x - margin
  let newY = clientY - dragOffset.value.y - margin

  // 限制在视口内
  const maxX = window.innerWidth - btnSize - margin * 2
  const maxY = window.innerHeight - btnSize - margin * 2

  newX = Math.max(0, Math.min(newX, maxX))
  newY = Math.max(0, Math.min(newY, maxY))

  // 相对于默认位置（左侧 margin 处）的偏移
  const defaultX = margin
  const defaultY = window.innerHeight / 2

  btnPos.value = {
    x: newX - defaultX,
    y: newY - defaultY
  }

  hasDragged.value = true
}

const onDragEnd = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDragMove)
  document.removeEventListener('touchend', onDragEnd)
  savePosition()
}

const onBtnClick = () => {
  if (hasDragged.value) return
  tocVisible.value = !tocVisible.value
}

onMounted(() => {
  loadSavedPosition()
})

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

watch(activeTocId, (newId) => {
  if (!newId || !tocNavRef.value) return
  const linkEl = tocLinkRefs.value[newId]
  if (!linkEl) return

  const navRect = tocNavRef.value.getBoundingClientRect()
  const linkRect = linkEl.getBoundingClientRect()

  const navTop = navRect.top
  const navBottom = navRect.bottom
  const linkTop = linkRect.top
  const linkBottom = linkRect.bottom

  const padding = 8

  if (linkTop < navTop + padding) {
    tocNavRef.value.scrollTop -= (navTop + padding - linkTop)
  } else if (linkBottom > navBottom - padding) {
    tocNavRef.value.scrollTop += (linkBottom - navBottom + padding)
  }
})

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
  display: flex;
  gap: 32px;
  align-items: flex-start;
  min-height: calc(100vh - 140px);
}

.article-main {
  flex: 1;
  min-width: 0;
}

/* 侧边目录 */
.toc-sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  align-self: flex-start;
  height: fit-content;
  max-height: calc(100vh - 100px);
}

.toc-sidebar-inner {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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

.toc-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ecf0f1;
}

.toc-sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  font-weight: 600;
  color: #2c3e50;
}

.toc-sidebar-close {
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

.toc-sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toc-sidebar-link {
  display: block;
  padding: 6px 10px;
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

.toc-sidebar-level-1 {
  font-weight: 600;
  font-size: 0.88em;
}

.toc-sidebar-level-2 {
  padding-left: 18px;
}

.toc-sidebar-level-3 {
  padding-left: 32px;
  font-size: 0.82em;
  color: #718096;
}

.toc-sidebar-level-4 {
  padding-left: 46px;
  font-size: 0.8em;
  color: #a0aec0;
}

.toc-sidebar-active {
  background: #ecf5ff;
  color: #409eff;
  border-left-color: #409eff;
  font-weight: 500;
}

/* 目录悬浮按钮 */
.toc-float-btn {
  position: fixed;
  left: 16px;
  top: 50%;
  z-index: 100;
  cursor: grab;
  touch-action: none;
  user-select: none;

  &:active {
    cursor: grabbing;
  }
}

.toc-float-ring {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1em;
  color: #4a5568;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px) saturate(1.2);
  -webkit-backdrop-filter: blur(12px) saturate(1.2);
  border: 1px solid rgba(224, 230, 237, 0.8);
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.06),
    0 4px 12px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    color: #409eff;
    border-color: rgba(64, 158, 255, 0.35);
    box-shadow:
      0 2px 6px rgba(0, 0, 0, 0.08),
      0 8px 20px rgba(64, 158, 255, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.6);
    transform: scale(1.08);
  }
}

.toc-float-btn-active .toc-float-ring {
  color: #409eff;
  background: rgba(236, 245, 255, 0.9);
  border-color: rgba(64, 158, 255, 0.4);
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.08),
    0 6px 16px rgba(64, 158, 255, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.toc-float-btn-dragging .toc-float-ring {
  transform: scale(1.12);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.1),
    0 12px 28px rgba(64, 158, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.5);
  cursor: grabbing;
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
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #8fa8f7 0%, #a893d1 100%);
  }
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.type-badge {
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 0.78em;
  font-weight: 500;
  position: relative;
  transition: all 0.3s ease;

  &.tech {
    background: linear-gradient(135deg, #eef4ff 0%, #f0f4ff 100%);
    color: #7b96e6;
    border: 1px solid rgba(123, 150, 230, 0.15);

    &::after {
      background: linear-gradient(90deg, #8fa8f7 0%, #a893d1 100%);
    }
  }

  &.life {
    background: linear-gradient(135deg, #fef0f6 0%, #fff5fa 100%);
    color: #d484b0;
    border: 1px solid rgba(212, 132, 176, 0.15);

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

.category {
  font-size: 0.9em;
  font-weight: 500;
  background: linear-gradient(135deg, #7b96e6 0%, #9a7fc2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
  letter-spacing: -0.01em;
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

    .el-icon {
      color: #b8c5d6;
      transition: color 0.3s ease;
    }

    &:hover .el-icon {
      color: #8fa8f7;
    }
  }
}

.article-body {
  background: white;
  padding: 32px;
  min-height: 300px;
}

.article-footer {
  background: white;
  padding: 0;
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
  transition: max-height 0.25s ease, opacity 0.25s ease;
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
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  align-self: flex-start;
  height: fit-content;
}

 .comment-layout {
  display: flex;
  gap: 32px;
  margin-top: 16px;
  align-items: flex-start;
}

.comment-spacer {
  width: 240px;
  flex-shrink: 0;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-sidebar-spacer {
  width: 300px;
  flex-shrink: 0;
}

@media (max-width: 1024px) {
  .detail-layout,
  .comment-layout {
    gap: 16px;
  }

  .sidebar-area {
    width: 260px;
  }

  .toc-sidebar,
  .comment-spacer {
    display: none;
  }

  .toc-float-btn {
    left: 12px;
  }
}

@media (max-width: 768px) {
  .detail-layout,
  .comment-layout {
    flex-direction: column;
  }

  .sidebar-area,
  .toc-float-btn,
  .comment-sidebar-spacer {
    display: none !important;
  }

  .title {
    font-size: 1.5em;
  }
}
</style>
