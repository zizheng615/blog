<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑文章' : '新建文章'"
    :width="dialogWidth"
    :fullscreen="isMobile"
    class="article-editor-dialog"
    destroy-on-close
  >
    <el-form :model="form" :label-width="isMobile ? '70px' : '80px'" ref="formRef">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="别名">
        <el-input v-model="form.slug" placeholder="URL-friendly 标识" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
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
          <el-radio-button :value="'TECH'">技术</el-radio-button>
          <el-radio-button :value="'LIFE'">生活</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="标签">
        <div class="tag-control">
          <el-select
            v-model="form.tagIds"
            multiple
            filterable
            placeholder="选择标签"
            class="tag-select"
          >
            <el-option
              v-for="tag in localTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-input
            v-model="newTagName"
            placeholder="输入名称后回车新建"
            class="tag-input"
            @keyup.enter.prevent="addNewTag"
          >
            <template #append>
              <el-button
                @click="addNewTag"
                :loading="addingTag"
                :disabled="!newTagName.trim()"
              >
                新建
              </el-button>
            </template>
          </el-input>
        </div>
      </el-form-item>
      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          placeholder="文章摘要"
          resize="none"
        />
      </el-form-item>
      <el-form-item label="内容" required>
        <div class="editor-wrapper">
          <Toolbar
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
            class="editor-toolbar"
          />
          <Editor
            v-model="form.content"
            :defaultConfig="editorConfig"
            mode="default"
            class="editor-body"
            @onCreated="onEditorCreated"
          />
        </div>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio-button :value="'PUBLISHED'">发布</el-radio-button>
          <el-radio-button :value="'DRAFT'">草稿</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="isEdit" label="重新发布">
        <el-checkbox v-model="form.republish">
          保存时将发布时间更新为当前，本文会重新排到最新文章顶部
        </el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, shallowRef, watch, onBeforeUnmount, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { Boot } from '@wangeditor/editor'
import formulaModule from '@wangeditor/plugin-formula'
import { createArticle, updateArticle } from '@/api/article'
import { createTag } from '@/api/tag'

const mathMenuIcon = (label) =>
  `<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><text x="80" y="780" font-size="700" font-family="Cambria,Times,serif" font-style="italic">${label}</text></svg>`

const createBlockFormulaMenu = () => ({
  title: '插入块级公式',
  tag: 'button',
  iconSvg: mathMenuIcon('∫'),
  exec(editor) {
    ElMessageBox.prompt(
      '输入 LaTeX 块级公式（独占一行渲染）',
      '插入块级公式',
      {
        confirmButtonText: '插入',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：\\int_0^1 x^2 dx',
        inputValidator: (v) => (v && v.trim()) ? true : '公式不能为空'
      }
    ).then(({ value }) => {
      const latex = value.trim()
      editor.insertText(`$$${latex}$$`)
    }).catch(() => {})
  },
  getValue() { return '' },
  isActive() { return false },
  isDisabled() { return false }
})

if (!window.__blogFormulaRegistered) {
  try {
    Boot.registerModule(formulaModule)
    Boot.registerMenu({ key: 'insertMathBlock', factory: createBlockFormulaMenu })
    window.__blogFormulaRegistered = true
  } catch (e) {
    console.warn('Formula module/menu register skipped:', e)
  }
}

const props = defineProps({
  categories: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] }
})

const emit = defineEmits(['success', 'tags-updated'])

const visible = ref(false)
const loading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const editorRef = shallowRef()

const localTags = ref([])
const newTagName = ref('')
const addingTag = ref(false)
watch(() => props.tags, (val) => {
  localTags.value = [...(val || [])]
}, { immediate: true })

const addNewTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  if (localTags.value.some(t => t.name === name)) {
    ElMessage.warning('标签已存在')
    return
  }
  addingTag.value = true
  try {
    const newTag = await createTag({ name })
    localTags.value = [...localTags.value, newTag]
    if (!form.tagIds.includes(newTag.id)) {
      form.tagIds.push(newTag.id)
    }
    newTagName.value = ''
    emit('tags-updated', newTag)
    ElMessage.success('标签已创建')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '标签创建失败')
  } finally {
    addingTag.value = false
  }
}

const viewportWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
const onResize = () => { viewportWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const isMobile = computed(() => viewportWidth.value <= 768)
const dialogWidth = computed(() => {
  if (viewportWidth.value >= 1200) return '900px'
  if (viewportWidth.value >= 992) return '80%'
  return '95%'
})

const uploadHeaders = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const toolbarConfig = {
  excludeKeys: [],
  insertKeys: {
    index: 30,
    keys: ['insertFormula', 'insertMathBlock']
  }
}

const editorConfig = {
  placeholder: '请输入文章内容...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/v1/admin/upload/image',
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      headers: uploadHeaders(),
      timeout: 30 * 1000,
      meta: {},
      metaWithUrl: false,
      withCredentials: false,
    },
    uploadVideo: {
      server: '/api/v1/admin/upload/video',
      fieldName: 'file',
      maxFileSize: 100 * 1024 * 1024,
      allowedFileTypes: ['video/*'],
      headers: uploadHeaders(),
      timeout: 5 * 60 * 1000,
      meta: {},
      metaWithUrl: false,
      withCredentials: false,
    },
  }
}

const onEditorCreated = (editor) => {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) editor.destroy()
})

const form = reactive({
  id: null,
  title: '',
  slug: '',
  categoryId: null,
  articleType: 'TECH',
  tagIds: [],
  summary: '',
  content: '',
  status: 'PUBLISHED',
  republish: false
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
  form.republish = false
  // refresh upload headers in case token changed between opens
  editorConfig.MENU_CONF.uploadImage.headers = uploadHeaders()
  editorConfig.MENU_CONF.uploadVideo.headers = uploadHeaders()
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
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor-toolbar {
  position: sticky;
  top: 0;
  z-index: 5;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  border-radius: 4px 4px 0 0;
}

.editor-body {
  height: 400px;
  overflow-y: auto;
  border-radius: 0 0 4px 4px;
}

.tag-control {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.tag-select {
  flex: 1 1 60%;
  min-width: 0;
}

.tag-input {
  flex: 1 1 220px;
  min-width: 180px;
}

:deep(.w-e-text-container) {
  height: 100% !important;
}

:deep(.w-e-scroll) {
  height: 100% !important;
}

@media (max-width: 768px) {
  .editor-body {
    height: 320px;
  }

  .tag-select,
  .tag-input {
    flex: 1 1 100%;
  }
}
</style>

<style lang="scss">
.article-editor-dialog {
  .el-dialog__body {
    max-height: calc(100vh - 200px);
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .article-editor-dialog.is-fullscreen .el-dialog__body {
    max-height: calc(100vh - 160px);
  }
}
</style>
