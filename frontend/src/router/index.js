import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
  { path: '/articles', name: 'ArticleList', component: () => import('@/views/ArticleListView.vue') },
  { path: '/articles/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetailView.vue') },
  { path: '/contact', name: 'Contact', component: () => import('@/views/ContactView.vue') },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/LoginView.vue'),
  },
  {
    path: '/admin',
    component: () => import('@/components/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('@/views/admin/CommentManageView.vue') },
      { path: 'friend-links', name: 'AdminFriendLinks', component: () => import('@/views/admin/FriendLinkManageView.vue') },
      { path: 'contact', name: 'AdminContact', component: () => import('@/views/admin/ContactManageView.vue') },
      { path: 'account', name: 'AdminAccount', component: () => import('@/views/admin/AccountView.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next('/admin/login')
  } else {
    next()
  }
})

export default router
