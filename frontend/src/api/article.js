import request from '@/utils/request'

export const getArticles = (params) => request.get('/api/v1/articles', { params })
export const getArticleById = (id) => request.get(`/api/v1/articles/${id}`)
export const getArticleBySlug = (slug) => request.get(`/api/v1/articles/slug/${slug}`)

export const getAdminArticles = (params) => request.get('/api/v1/admin/articles', { params })
export const createArticle = (data) => request.post('/api/v1/admin/articles', data)
export const updateArticle = (id, data) => request.put(`/api/v1/admin/articles/${id}`, data)
export const deleteArticle = (id) => request.delete(`/api/v1/admin/articles/${id}`)
