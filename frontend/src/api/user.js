import http from './http'

export function getUserById(id) {
  return http.get(`/user/${id}`)
}
