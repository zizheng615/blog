import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useArticleStore = defineStore('article', () => {
  const categories = ref([])
  const tags = ref([])
  const visitorStats = ref({ totalPv: 0, totalUv: 0, todayPv: 0, todayUv: 0 })

  const setCategories = (data) => { categories.value = data }
  const setTags = (data) => { tags.value = data }
  const setVisitorStats = (data) => { visitorStats.value = data }

  return { categories, tags, visitorStats, setCategories, setTags, setVisitorStats }
})
