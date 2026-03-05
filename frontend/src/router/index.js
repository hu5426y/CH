import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import StudentLayout from '../components/StudentLayout.vue'
import AdminLayout from '../components/AdminLayout.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/register', component: RegisterView, meta: { public: true } },
  {
    path: '/student',
    component: StudentLayout,
    meta: { role: 'STUDENT' },
    children: [
      { path: 'seats', component: () => import('../views/student/SeatReservationView.vue') },
      { path: 'reservations', component: () => import('../views/student/MyReservationsView.vue') },
      { path: 'profile', component: () => import('../views/student/ProfileView.vue') },
      { path: '', redirect: '/student/seats' }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { role: 'ADMIN' },
    children: [
      { path: 'dashboard', component: () => import('../views/admin/DashboardView.vue') },
      { path: 'users', component: () => import('../views/admin/UserManageView.vue') },
      { path: 'rooms', component: () => import('../views/admin/RoomManageView.vue') },
      { path: 'seats', component: () => import('../views/admin/SeatManageView.vue') },
      { path: 'reservations', component: () => import('../views/admin/ReservationManageView.vue') },
      { path: 'violations', component: () => import('../views/admin/ViolationManageView.vue') },
      { path: 'rules', component: () => import('../views/admin/RuleConfigView.vue') },
      { path: 'logs', component: () => import('../views/admin/LogView.vue') },
      { path: '', redirect: '/admin/dashboard' }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token) {
    next('/login')
    return
  }
  if (to.path.startsWith('/admin') && role !== 'ADMIN') {
    next('/student/seats')
    return
  }
  if (to.path.startsWith('/student') && role !== 'STUDENT') {
    next('/admin/dashboard')
    return
  }
  next()
})

export default router
