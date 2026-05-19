import axios from 'axios'
import Vue from 'vue'

axios.defaults.baseURL = '/api'
axios.defaults.timeout = 10000

axios.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

Vue.prototype.$axios = axios
