import request from '@/utils/request'

export const getFriendLinks = () => request.get('/api/v1/friend-links')
