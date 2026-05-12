<template>
  <aside class="sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">
        <el-icon><Collection /></el-icon> 分类
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
      <h3 class="section-title">
        <el-icon><PriceTag /></el-icon> 标签
      </h3>
      <div class="tag-cloud">
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
}

.sidebar-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1em;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ecf0f1;
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
</style>
