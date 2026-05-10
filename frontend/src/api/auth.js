import request from '@/utils/request'

export const login = (data) => request.post('/api/v1/auth/login', data)
