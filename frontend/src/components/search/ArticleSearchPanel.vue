<template>
  <div class="article-search-panel">
    <!-- 收起状态：搜索触发按钮 -->
    <transition name="search-toggle">
      <div
        v-if="!expanded"
        class="search-trigger"
        @click="expand"
        title="搜索文章"
      >
        <el-icon class="search-icon"><Search /></el-icon>
        <span class="search-hint">搜索</span>
      </div>
    </transition>

    <!-- 展开状态：搜索输入框 -->
    <transition name="search-expand">
      <div v-if="expanded" class="search-input-wrapper">
        <el-icon class="search-icon-inner"><Search /></el-icon>
        <input
          ref="inputRef"
          :value="modelValue"
          type="text"
          class="search-input"
          :placeholder="placeholder"
          @input="onInput"
          @keyup.enter="handleEnter"
        />
        <div class="search-actions">
          <span
            v-if="modelValue"
            class="search-clear"
            title="清空"
            @click="clearInput"
          >
            <el-icon><Close /></el-icon>
          </span>
          <span
            class="search-close"
            title="收起"
            @click="collapse"
          >
            <el-icon><ArrowUp /></el-icon>
          </span>
        </div>
      </div>
    </transition>

    <!-- 搜索状态提示 -->
    <transition name="fade">
      <div v-if="showStatus" class="search-status">
        <span v-if="resultCount > 0" class="has-result">
          找到 {{ resultCount }} 篇相关文章
        </span>
        <span v-else-if="!loading && queryText" class="no-result">
          未找到与 "{{ queryText }}" 相关的文章
        </span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { Search, Close, ArrowUp } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  loading:    { type: Boolean, default: false },
  resultCount:{ type: Number,  default: 0 },
  isSearching:{ type: Boolean, default: false },
  placeholder:{ type: String, default: '输入关键词搜索标题、摘要、内容...' }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear'])

const expanded = ref(false)
const inputRef = ref(null)

const queryText = computed(() => props.modelValue?.trim() || '')
const showStatus = computed(() =>
  props.isSearching && !props.loading && queryText.value.length > 0
)

/** 展开搜索面板并聚焦输入框 */
const expand = () => {
  expanded.value = true
  nextTick(() => inputRef.value?.focus())
}

/** 收起搜索面板，清空关键词 */
const collapse = () => {
  expanded.value = false
  if (props.modelValue) {
    emit('update:modelValue', '')
    emit('clear')
  }
}

/** 清空输入 */
const clearInput = () => {
  emit('update:modelValue', '')
  emit('clear')
  nextTick(() => inputRef.value?.focus())
}

/** 输入事件（支持中文输入法延迟） */
const onInput = (e) => {
  emit('update:modelValue', e.target.value)
  if (e.target.value.trim()) {
    emit('search', e.target.value.trim())
  } else {
    emit('clear')
  }
}

/** 回车强制触发搜索 */
const handleEnter = () => {
  if (queryText.value) {
    emit('search', queryText.value)
  }
}

// 外部清空时同步收起面板
watch(() => props.modelValue, (val) => {
  if (!val && expanded.value && !props.isSearching) {
    expanded.value = false
  }
})

defineExpose({ expand, collapse })
</script>

<style lang="scss" scoped>
.article-search-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* ---------- 触发按钮 ---------- */
.search-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 500;
  color: #7b96e6;
  background: rgba(123, 150, 230, 0.08);
  border: 1px solid rgba(123, 150, 230, 0.2);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;

  &:hover {
    background: rgba(123, 150, 230, 0.14);
    border-color: rgba(123, 150, 230, 0.35);
    color: #6a85d6;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(123, 150, 230, 0.1);
  }

  .search-icon {
    font-size: 0.95em;
  }

  .search-hint {
    white-space: nowrap;
  }
}

/* ---------- 展开输入框 ---------- */
.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 24px;
  background: white;
  border: 1.5px solid rgba(123, 150, 230, 0.3);
  box-shadow: 0 2px 12px rgba(123, 150, 230, 0.1);
  transition: all 0.3s ease;
  width: 100%;
  max-width: 360px;

  &:focus-within {
    border-color: #7b96e6;
    box-shadow: 0 2px 16px rgba(123, 150, 230, 0.18);
  }

  .search-icon-inner {
    color: #a0aec0;
    font-size: 1em;
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-size: 0.9em;
    color: #2d3748;
    min-width: 160px;

    &::placeholder {
      color: #a0aec0;
      font-size: 0.92em;
    }
  }

  .search-actions {
    display: flex;
    align-items: center;
    gap: 2px;
    flex-shrink: 0;
  }

  .search-clear,
  .search-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    cursor: pointer;
    color: #a0aec0;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(0, 0, 0, 0.05);
      color: #718096;
    }
  }
}

/* ---------- 状态提示 ---------- */
.search-status {
  margin-top: 6px;
  font-size: 0.78em;
  text-align: right;
  white-space: nowrap;

  .has-result {
    color: #7b96e6;
  }

  .no-result {
    color: #d484b0;
  }
}

/* ---------- 过渡动画 ---------- */
.search-toggle-enter-active,
.search-toggle-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.search-toggle-enter-from,
.search-toggle-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.search-expand-enter-active,
.search-expand-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.search-expand-enter-from,
.search-expand-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---------- 移动端适配 ---------- */
@media (max-width: 768px) {
  .search-input-wrapper {
    max-width: 100%;

    .search-input {
      min-width: 100px;
    }
  }

  .search-status {
    text-align: left;
  }
}
</style>
