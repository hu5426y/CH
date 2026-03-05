import http from './http'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data)
}

export const userApi = {
  profile: () => http.get('/user/profile'),
  updateProfile: (data) => http.put('/user/profile', data),
  changePassword: (data) => http.put('/user/password', data),
  listStudents: (keyword) => http.get('/admin/users', { params: { keyword } }),
  updateStatus: (data) => http.put('/admin/users/status', data),
  resetPassword: (data) => http.put('/admin/users/reset-password', data)
}

export const roomApi = {
  list: () => http.get('/rooms'),
  create: (data) => http.post('/admin/rooms', data),
  update: (id, data) => http.put(`/admin/rooms/${id}`, data),
  remove: (id) => http.delete(`/admin/rooms/${id}`)
}

export const seatApi = {
  list: (params) => http.get('/seats', { params }),
  create: (data) => http.post('/admin/seats', data),
  update: (id, data) => http.put(`/admin/seats/${id}`, data),
  remove: (id) => http.delete(`/admin/seats/${id}`)
}

export const reservationApi = {
  add: (data) => http.post('/reservation/add', data),
  cancel: (id) => http.post(`/reservation/${id}/cancel`),
  checkin: (id) => http.post(`/reservation/${id}/checkin`),
  checkout: (id) => http.post(`/reservation/${id}/checkout`),
  myList: () => http.get('/reservation/my'),
  adminList: (params) => http.get('/admin/reservation', { params }),
  adminCancel: (id) => http.post(`/admin/reservation/${id}/cancel`)
}

export const violationApi = {
  list: (keyword) => http.get('/admin/violation', { params: { keyword } }),
  revoke: (id) => http.post(`/admin/violation/${id}/revoke`)
}

export const ruleApi = {
  list: () => http.get('/admin/rule'),
  save: (data) => http.post('/admin/rule', data)
}

export const statisticsApi = {
  overview: () => http.get('/admin/statistics/overview'),
  roomUsage: () => http.get('/admin/statistics/room-usage'),
  trend: (days = 7) => http.get('/admin/statistics/trend', { params: { days } })
}

export const logApi = {
  list: (params) => http.get('/admin/logs', { params })
}
