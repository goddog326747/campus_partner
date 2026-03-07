import { createStore } from 'vuex'
import { login, getUserInfo } from '../api/auth'

export default createStore({
  state: {
    user: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || ''
  },
  getters: {
    isLoggedIn: state => !!state.token,
    currentUser: state => state.user
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('userInfo', JSON.stringify(user))
    },
    LOGOUT(state) {
      state.token = ''
      state.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    async login({ commit }, loginForm) {
      const res = await login(loginForm)
      if (res.code === 200) {
        commit('SET_TOKEN', res.data.token)
        commit('SET_USER', res.data.userInfo)
        return true
      }
      return false
    },
    async fetchUser({ commit }) {
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          commit('SET_USER', res.data)
        }
      } catch (e) {
        console.error(e)
      }
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  modules: {
  }
})
