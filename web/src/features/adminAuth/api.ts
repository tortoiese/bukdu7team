import { apiRequest } from '../client'
import type { AdminLoginResponse } from '../../types/api'

export function adminLogin(password: string) {
  return apiRequest<AdminLoginResponse>('/admin/login', { method: 'POST', body: { password } })
}
