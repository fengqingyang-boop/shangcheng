import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  async function login(username, password) {
    const response = await api.post('/api/auth/login', { username, password })
    setToken(response.data.token)
    setUserInfo({
      id: response.data.id,
      username: response.data.username,
      role: response.data.role,
      points: response.data.points
    })
    return response.data
  }
  
  async function register(username, password, email) {
    const response = await api.post('/api/auth/register', { username, password, email })
    setToken(response.data.token)
    setUserInfo({
      id: response.data.id,
      username: response.data.username,
      role: response.data.role,
      points: response.data.points
    })
    return response.data
  }
  
  async function refreshUserInfo() {
    if (token.value) {
      try {
        const response = await api.get('/api/users/me')
        setUserInfo({
          id: response.data.id,
          username: response.data.username,
          role: response.data.role,
          points: response.data.points
        })
      } catch (error) {
        logout()
      }
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    setToken,
    setUserInfo,
    logout,
    login,
    register,
    refreshUserInfo
  }
})
