import request from '@/utils/request'

export const recordVisit = (data) => request.post('/api/v1/visitors/record', data)
export const getVisitorStats = () => request.get('/api/v1/visitors/stats')
