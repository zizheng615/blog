import request from '@/utils/request'

export const getSiteConfig = () => request.get('/api/v1/site-config')

export const getAdminSiteConfig = () => request.get('/api/v1/admin/site-config')

export const updateSiteConfig = (data) =>
  request.put('/api/v1/admin/site-config', data)
