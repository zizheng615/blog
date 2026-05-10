<template>
  <div class="comment-manage">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <el-table :data="comments" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="文章" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="goToArticle(row.articleId)">
            {{ row.articleTitle || '未知文章' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="success"
            size="small"
            @click="handleStatus(row.id, 'APPROVED')"
          >通过</el-button>
          <el-button
            v-if="row.status === 'APPROVED'"
            type="warning"
            size="small"
            @click="handleStatus(row.id, 'PENDING')"
          >待审</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment'

const comments = ref([])
const loading = ref(false)

const loadComments = async () => {
  loading.value = true
  try {
    comments.value = await getAdminComments()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleStatus = async (id, status) => {
  try {
    await updateCommentStatus(id, status)
    ElMessage.success('状态更新成功')
    loadComments()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认', { type: 'warning' })
    await deleteComment(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const statusLabel = (status) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', SPAM: '垃圾' }
  return map[status] || status
}

const statusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', SPAM: 'danger' }
  return map[status] || 'info'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const goToArticle = (articleId) => {
  window.open(`/articles/${articleId}`, '_blank')
}

onMounted(() => {
  loadComments()
})
</script>

<style lang="scss" scoped>
.comment-manage {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    font-size: 1.2em;
    font-weight: 600;
    color: #2c3e50;
  }
}
</style>
