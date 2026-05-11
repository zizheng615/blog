import request from '@/utils/request'

export const getFriendLinks = () => request.get('/api/v1/friend-links')

export const getAdminFriendLinks = () => request.get('/api/v1/admin/friend-links')

export const createFriendLink = (data) =>
  request.post('/api/v1/admin/friend-links', data)

export const updateFriendLink = (id, data) =>
  request.put(`/api/v1/admin/friend-links/${id}`, data)

export const deleteFriendLink = (id) =>
  request.delete(`/api/v1/admin/friend-links/${id}`)
