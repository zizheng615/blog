import { ref, watch } from 'vue'
import { searchArticles as searchApi } from '@/api/article'

/**
 * 文章搜索 Composable
 *
 * 封装文章搜索的完整状态与逻辑：防抖输入、分页查询、结果缓存。
 * 与原始文章列表完全解耦，通过 isActive 标识搜索模式，
 * 父组件据此切换渲染内容，不侵入原有数据流。
 *
 * @param {Object} options
 * @param {number} options.debounceMs   防抖延迟（默认 350ms）
 * @param {string} options.status       文章状态过滤（默认 'PUBLISHED'）
 * @param {number} options.pageSize     每页条数（默认 10）
 */
export function useArticleSearch(options = {}) {
  const { debounceMs = 350, status = 'PUBLISHED', pageSize = 10 } = options

  const keyword = ref('')
  const results = ref([])
  const total = ref(0)
  const loading = ref(false)
  const isActive = ref(false) // 标识是否处于搜索模式
  const page = ref(1)
  const size = ref(pageSize)

  let debounceTimer = null

  /**
   * 执行实际搜索请求
   */
  const doSearch = async () => {
    const q = keyword.value.trim()
    if (!q) {
      doClear()
      return
    }

    loading.value = true
    isActive.value = true

    try {
      const res = await searchApi({
        keyword: q,
        page: page.value,
        size: size.value,
        status
      })
      results.value = res.list || []
      total.value = res.total || 0
    } catch (e) {
      console.error('Search failed:', e)
      results.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 防抖包装：清空旧定时器，延迟执行搜索
   */
  const scheduleSearch = () => {
    clearTimeout(debounceTimer)
    const q = keyword.value.trim()
    if (!q) {
      doClear()
      return
    }
    debounceTimer = setTimeout(doSearch, debounceMs)
  }

  /**
   * 清空搜索状态，退出搜索模式
   */
  const doClear = () => {
    clearTimeout(debounceTimer)
    results.value = []
    total.value = 0
    loading.value = false
    isActive.value = false
    page.value = 1
  }

  /**
   * 关键词变化处理器
   */
  const onKeywordChange = (val) => {
    keyword.value = val
    if (!val || !val.trim()) {
      doClear()
    } else {
      scheduleSearch()
    }
  }

  /**
   * 分页变化处理器
   */
  const onPageChange = (p) => {
    page.value = p
    doSearch()
  }

  return {
    keyword,
    results,
    total,
    loading,
    isActive,
    page,
    size,
    onKeywordChange,
    onPageChange,
    doClear
  }
}
