import request from '@/utils/request'

export const login = (data) => request.post('/api/v1/auth/login', data)

export const changePassword = (data) => request.post('/api/v1/admin/account/change-password', data)
