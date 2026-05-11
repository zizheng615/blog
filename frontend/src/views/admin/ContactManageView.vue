<template>
  <div class="contact-manage">
    <div class="page-header">
      <h2>联系我编辑</h2>
      <p class="hint">这里的内容会展示在前台「联系我」页面和页脚「关注我」板块。</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
      class="contact-form"
    >
      <el-form-item label="邮箱" prop="contact_email">
        <el-input v-model="form.contact_email" placeholder="example@mail.com" clearable />
      </el-form-item>
      <el-form-item label="GitHub" prop="contact_github">
        <el-input v-model="form.contact_github" placeholder="https://github.com/your-name" clearable />
      </el-form-item>
      <el-form-item label="哔哩哔哩" prop="contact_bilibili">
        <el-input v-model="form.contact_bilibili" placeholder="https://space.bilibili.com/xxxxx" clearable />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="loadConfig">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminSiteConfig, updateSiteConfig } from '@/api/siteConfig'

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  contact_email: '',
  contact_github: '',
  contact_bilibili: '',
})

const rules = {
  contact_email: [
    { type: 'email', message: '请输入合法的邮箱', trigger: 'blur' },
  ],
  contact_github: [
    { type: 'url', message: '请输入合法的 URL', trigger: 'blur' },
  ],
  contact_bilibili: [
    { type: 'url', message: '请输入合法的 URL', trigger: 'blur' },
  ],
}

const loadConfig = async () => {
  loading.value = true
  try {
    const data = await getAdminSiteConfig()
    form.contact_email = data?.contact_email || ''
    form.contact_github = data?.contact_github || ''
    form.contact_bilibili = data?.contact_bilibili || ''
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const payload = {
      contact_email: form.contact_email?.trim() || '',
      contact_github: form.contact_github?.trim() || '',
      contact_bilibili: form.contact_bilibili?.trim() || '',
    }
    await updateSiteConfig(payload)
    ElMessage.success('保存成功')
  } catch (e) {
    // request interceptor already shows the toast
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<style lang="scss" scoped>
.contact-manage {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 1.2em;
    font-weight: 600;
    color: #2c3e50;
  }

  .hint {
    margin-top: 6px;
    color: #909399;
    font-size: 0.9em;
  }
}

.contact-form {
  max-width: 560px;
}

@media (max-width: 768px) {
  .contact-manage {
    padding: 16px;
    border-radius: 8px;
  }

  :deep(.el-form-item__label) {
    width: 80px !important;
  }
}
</style>
