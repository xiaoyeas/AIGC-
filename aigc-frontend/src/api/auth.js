import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

export function getUserConfig() {
  return request({
    url: '/config/user',
    method: 'get'
  })
}

export function saveUserConfig(data) {
  return request({
    url: '/config/save',
    method: 'post',
    data
  })
}