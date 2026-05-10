<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑文章' : '新建文章'"
    width="900px"
    destroy-on-close
  >
    <el-form :model="form" label-width="80px" ref="formRef">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="别名">
        <el-input v-model="form.slug" placeholder="URL-friendly 标识" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="form.categoryId" placeholder="选择分类">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-radio-group v-model="form.articleType">
          <el-radio-button label="TECH">技术</el-radio-button>
          <el-radio-button label="LIFE">生活</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签">
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          placeholder="文章摘要"
        />
      </el-form-item>
      <el-form-item label="内容" required>
        <div class="editor-wrapper">
          <Editor
            v-model="form.content"
            :defaultConfig="editorConfig"
            mode="default"
            style="height: 400px"
          />
        </div>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio-button label="PUBLISHED">发布</el-radio-button>
          <el-radio-button label="DRAFT">草稿</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Editor } from '@wangeditor/editor-for-vue'
import { createArticle, updateArticle } from '@/api/article'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] }
})

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const isEdit = ref(false)
const formRef = ref()

const editorConfig = {
  placeholder: '请输入文章内容...',
  MENU_CONF: {
    uploadImage: { server: '/api/upload', fieldName: 'file' }
  }
}

const form = reactive({
  id: null,
  title: '',
  slug: '',
  categoryId: null,
  articleType: 'TECH',
  tagIds: [],
  summary: '',
  content: '',
  status: 'PUBLISHED'
})

const open = (article = null) => {
  isEdit.value = !!article
  if (article) {
    Object.assign(form, article)
    form.tagIds = article.tags?.map(t => t.id) || []
  } else {
    Object.assign(form, {
      id: null, title: '', slug: '', categoryId: null,
      articleType: 'TECH', tagIds: [], summary: '',
      content: '', status: 'PUBLISHED'
    })
  }
  visible.value = true
}

const submit = async () => {
  if (!form.title || !form.categoryId || !form.content) {
    ElMessage.warning('请填写必填项')
    return
  }
  loading.value = true
  try {
    const data = { ...form }
    data.tags = form.tagIds.map(id => ({ id }))
    if (isEdit.value) {
      await updateArticle(form.id, data)
    } else {
      await createArticle(data)
    }
    ElMessage.success('保存成功')
    visible.value = false
    emit('success')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style lang="scss" scoped>
.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;

  :deep(.w-e-text-container) {
    min-height: 350px;
  }
}
</style>
