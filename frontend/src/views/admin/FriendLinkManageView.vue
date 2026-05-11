<template>
  <div class="friend-link-manage">
    <div class="page-header">
      <h2>友链管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        <span>新增友链</span>
      </el-button>
    </div>

    <el-table :data="links" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="url" label="链接" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <a :href="row.url" target="_blank" class="link-cell">{{ row.url }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="icon" label="图标 key" width="120" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑友链' : '新增友链'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="GitHub / 哔哩哔哩 ..." clearable />
        </el-form-item>
        <el-form-item label="链接" prop="url">
          <el-input v-model="form.url" placeholder="https://..." clearable />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="一句话简介，可选" clearable />
        </el-form-item>
        <el-form-item label="图标 key" prop="icon">
          <el-input v-model="form.icon" placeholder="如 github、bilibili，可选" clearable />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getAdminFriendLinks,
  createFriendLink,
  updateFriendLink,
  deleteFriendLink,
} from '@/api/friendLink'

const links = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const emptyForm = () => ({
  id: null,
  name: '',
  url: '',
  description: '',
  icon: '',
  sortOrder: 0,
  isActive: true,
})

const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  url: [
    { required: true, message: '请输入链接', trigger: 'blur' },
    { type: 'url', message: '请输入合法的 URL', trigger: 'blur' },
  ],
}

const loadLinks = async () => {
  loading.value = true
  try {
    links.value = (await getAdminFriendLinks()) || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetForm = (data) => {
  Object.assign(form, emptyForm(), data || {})
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  resetForm({
    id: row.id,
    name: row.name,
    url: row.url,
    description: row.description || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    isActive: row.isActive !== false,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      url: form.url.trim(),
      description: form.description?.trim() || null,
      icon: form.icon?.trim() || null,
      sortOrder: form.sortOrder ?? 0,
      isActive: !!form.isActive,
    }
    if (form.id) {
      await updateFriendLink(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createFriendLink(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadLinks()
  } catch (e) {
    // request interceptor already shows the toast
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除友链「${row.name}」吗？`, '确认', {
      type: 'warning',
    })
    await deleteFriendLink(row.id)
    ElMessage.success('删除成功')
    loadLinks()
  } catch (e) {
    if (e !== 'cancel') {
      // request interceptor already shows the toast
    }
  }
}

onMounted(loadLinks)
</script>

<style lang="scss" scoped>
.friend-link-manage {
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

.link-cell {
  color: #409eff;
  word-break: break-all;
}

@media (max-width: 768px) {
  .friend-link-manage {
    padding: 16px;
    border-radius: 8px;
  }

  :deep(.el-table) {
    font-size: 0.85em;
  }

  :deep(.el-dialog) {
    width: 92vw !important;
  }
}
</style>
