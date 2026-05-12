<template>
  <div class="comment-section">
    <h3 class="section-title">
      <el-icon><ChatLineRound /></el-icon>
      评论 ({{ totalCommentCount }})
    </h3>

    <div class="comment-form">
      <el-input
        v-model="form.nickname"
        placeholder="你的昵称"
        class="input"
        maxlength="50"
      />
      <el-input
        v-model="form.email"
        placeholder="邮箱（可选，用于显示头像）"
        class="input"
        type="email"
      />
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        class="input"
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button type="primary" @click="submitComment" :loading="submitting">
          <el-icon><Position /></el-icon> 发表评论
        </el-button>
      </div>
    </div>

    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <div class="avatar" :style="{ backgroundColor: getAvatarColor(comment.nickname) }">
            {{ comment.nickname?.charAt(0)?.toUpperCase() }}
          </div>
          <div class="comment-meta">
            <span class="nickname">
              {{ comment.nickname }}
              <el-tag v-if="comment.isAdmin" size="small" type="success">博主</el-tag>
            </span>
            <span class="time">{{ formatDate(comment.createdAt) }}</span>
          </div>
        </div>
        <div class="comment-content" v-html="comment.content"></div>

        <div v-if="comment.replies?.length" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <div class="reply-header">
              <span class="nickname">{{ reply.nickname }}</span>
              <el-tag v-if="reply.isAdmin" size="small" type="success">博主</el-tag>
              <span class="time">{{ formatDate(reply.createdAt) }}</span>
            </div>
            <div class="reply-content" v-html="reply.content"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getComments, createComment } from '@/api/comment'

const props = defineProps({
  articleId: { type: Number, required: true }
})

const comments = ref([])
const submitting = ref(false)
const form = ref({ nickname: '', email: '', content: '' })

const totalCommentCount = computed(() => {
  let count = comments.value.length
  for (const c of comments.value) {
    count += c.replies?.length || 0
  }
  return count
})

const loadComments = async () => {
  try {
    comments.value = await getComments(props.articleId)
  } catch (e) {
    console.error(e)
  }
}

const submitComment = async () => {
  if (!form.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await createComment(props.articleId, form.value)
    ElMessage.success('评论已提交，审核通过后将显示')
    form.value = { nickname: '', email: '', content: '' }
    await loadComments()
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
}

const getAvatarColor = (name) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#8e44ad']
  let hash = 0
  for (let i = 0; i < name?.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

onMounted(loadComments)
</script>

<style lang="scss" scoped>
.comment-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-top: 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1em;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 24px;

  .input {
    margin-bottom: 12px;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.9em;
}

.comment-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nickname {
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 6px;
}

.time {
  font-size: 0.8em;
  color: #a0aec0;
}

.comment-content {
  color: #4a5568;
  line-height: 1.7;
  padding-left: 48px;
}

.replies {
  margin-top: 12px;
  margin-left: 48px;
  padding-left: 16px;
  border-left: 2px solid #e0e6ed;
}

.reply-item {
  padding: 10px 0;

  &:not(:last-child) {
    border-bottom: 1px solid #edf2f7;
  }
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.reply-content {
  color: #4a5568;
  font-size: 0.95em;
}
</style>
