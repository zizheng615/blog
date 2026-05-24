<template>
  <aside class="sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">
        <el-icon class="icon-glow-blue"><Collection /></el-icon> 分类
      </h3>
      <div class="category-list">
        <router-link
          v-for="cat in visibleCategories"
          :key="cat.id"
          :to="`/articles?categoryId=${cat.id}`"
          class="category-item"
        >
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-count">{{ cat.articleCount || 0 }}</span>
        </router-link>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-header" @click="tagsExpanded = !tagsExpanded">
        <h3 class="section-title">
          <el-icon class="icon-glow-pink"><PriceTag /></el-icon> 标签
        </h3>
        <el-icon class="section-toggle icon-glow-gray" :class="{ 'section-toggle-collapsed': !tagsExpanded }">
          <ArrowDown />
        </el-icon>
      </div>
      <transition name="tag-fold">
        <div v-show="tagsExpanded" class="tag-cloud">
          <router-link
            v-for="tag in visibleTags"
            :key="tag.id"
            :to="`/articles?tagId=${tag.id}`"
            class="tag-item"
            :style="tagStyle(tag.color)"
          >
            {{ tag.name }}
            <span class="tag-count">{{ tag.articleCount || 0 }}</span>
          </router-link>
        </div>
      </transition>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { readableColor, rgbaBg, rgbaBorder } from '@/utils/color'

const categories = ref([])
const tags = ref([])
const tagsExpanded = ref(true)

const visibleCategories = computed(() =>
  categories.value.filter(c => (c.articleCount || 0) > 0)
)
const visibleTags = computed(() =>
  tags.value.filter(t => (t.articleCount || 0) > 0)
)

const tagStyle = (color) => ({
  backgroundColor: rgbaBg(color, 0.14),
  color: readableColor(color),
  borderColor: rgbaBorder(color, 0.45),
})

onMounted(async () => {
  try {
    categories.value = await getCategories()
    tags.value = await getTags()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.sidebar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  max-height: calc(100vh - 100px);
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
    transition: background 0.25s ease;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: #e0e6ed;
  }
}

.sidebar-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 2px 4px;
  margin: -2px -4px;
  border-radius: 8px;
  transition: background 0.2s ease;

  &:hover {
    background: #f8f9fa;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  font-weight: 600;
  color: #1a1a2e;
  padding-bottom: 0;
  margin-bottom: 0;
  border-bottom: none;
  letter-spacing: 0.02em;
}

.section-toggle {
  color: #a0aec0;
  font-size: 0.8em;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.section-toggle-collapsed {
  transform: rotate(-90deg);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-radius: 8px;
  transition: all 0.3s;
  color: #4a5568;

  &:hover {
    background: #ecf5ff;
    color: #409eff;
  }
}

.cat-count {
  background: #f0f4f8;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  color: #718096;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85em;
  border: 1px solid;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }
}

.tag-count {
  font-size: 0.8em;
  opacity: 0.7;
}

/* 标签折叠动画 */
.tag-fold-enter-active,
.tag-fold-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.tag-fold-enter-from,
.tag-fold-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
}

.tag-fold-enter-to,
.tag-fold-leave-from {
  opacity: 1;
  max-height: 500px;
  margin-top: 12px;
}
</style>
