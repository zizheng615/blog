<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="openEditor()">
        <el-icon><Plus /></el-icon> 新建文章
      </el-button>
    </div>

    <el-table :data="articles" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.articleType === 'TECH' ? 'primary' : 'success'">
            {{ row.articleType === 'TECH' ? '技术' : '生活' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="标签" min-width="160">
        <template #default="{ row }">
          <div class="admin-tag-list">
            <span
              v-for="tag in row.tags"
              :key="tag.id"
              class="admin-tag"
              :style="tagStyle(tag.color)"
            >
              #{{ tag.name }}
            </span>
            <span v-if="!row.tags?.length" class="no-tag">-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.publishedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEditor(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadArticles"
      />
    </div>

    <ArticleEditor
      ref="editorRef"
      :categories="categories"
      :tags="tags"
      @success="loadArticles"
      @tags-updated="onTagsUpdated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getAdminArticles, deleteArticle, getArticleById } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { readableColor, rgbaBg } from '@/utils/color'
import ArticleEditor from '@/components/admin/ArticleEditor.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editorRef = ref()

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getAdminArticles({ page: page.value, size: pageSize.value })
    articles.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadMeta = async () => {
  try {
    categories.value = await getCategories()
    tags.value = await getTags()
  } catch (e) {
    console.error(e)
  }
}

const openEditor = async (article = null) => {
  if (article) {
    // ArticleSummary 缺少 content 大字段，需获取完整文章数据
    try {
      const fullArticle = await getArticleById(article.id)
      editorRef.value?.open(fullArticle)
    } catch (e) {
      ElMessage.error('获取文章详情失败')
    }
  } else {
    editorRef.value?.open()
  }
}

const onTagsUpdated = () => {
  loadMeta()
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '确认', { type: 'warning' })
    await deleteArticle(id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const tagStyle = (color) => ({
  color: readableColor(color),
  backgroundColor: rgbaBg(color, 0.14),
  border: 'none',
  borderRadius: '0',
  padding: '2px 10px',
  fontSize: '0.75em',
  fontWeight: 500,
  letterSpacing: '0.4px',
  lineHeight: '1.5',
  opacity: 0.82,
})

onMounted(() => {
  loadArticles()
  loadMeta()
})
</script>

<style lang="scss" scoped>
.dashboard {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;

  h2 {
    font-size: 1.2em;
    font-weight: 600;
    color: #2c3e50;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.admin-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.admin-tag {
  display: inline-block;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover {
    opacity: 1;
    filter: brightness(0.92);
  }
}

.no-tag {
  color: #c0c4cc;
  font-size: 0.9em;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
    border-radius: 8px;
  }

  :deep(.el-table) {
    font-size: 0.85em;
  }
}
</style>
