import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userId: localStorage.getItem('userId') || '',
    realName: localStorage.getItem('realName') || ''
  }),
  actions: {
    setLogin(payload) {
      this.token = payload.token
      this.role = payload.role
      this.userId = String(payload.userId)
      this.realName = payload.realName || ''
      localStorage.setItem('token', this.token)
      localStorage.setItem('role', this.role)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('realName', this.realName)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.realName = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      localStorage.removeItem('realName')
    }
  }
})
