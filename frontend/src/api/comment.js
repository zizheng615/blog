import request from '@/utils/request'

export const getComments = (articleId) => request.get(`/api/v1/articles/${articleId}/comments`)
export const createComment = (articleId, data) => request.post(`/api/v1/articles/${articleId}/comments`, data)

export const getAdminComments = () => request.get('/api/v1/admin/comments')
export const updateCommentStatus = (id, status) => request.put(`/api/v1/admin/comments/${id}/status?status=${status}`)
export const deleteComment = (id) => request.delete(`/api/v1/admin/comments/${id}`)
