import request from '@/utils/request'

export const getTags = () => request.get('/api/v1/tags')
export const getArticlesByTag = (slug) => request.get(`/api/v1/tags/${slug}/articles`)
export const createTag = (data) => request.post('/api/v1/admin/tags', data)
export const deleteTag = (id) => request.delete(`/api/v1/admin/tags/${id}`)
