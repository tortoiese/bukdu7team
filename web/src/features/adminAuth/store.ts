// D1/D2 운영자 인증 상태. 게스트 익명 세션(features/session)과는 완전히 분리된 별도 토큰이다.
// /entryadmin에서 비밀번호로 발급받고, localStorage["entry.admin.token"]에 저장한다.
import { create } from 'zustand'
import { adminLogin } from './api'

const TOKEN_KEY = 'entry.admin.token'
const EXPIRES_KEY = 'entry.admin.token.expiresAt'

interface AdminAuthState {
  adminToken: string | null
  expiresAt: string | null
  login: (password: string) => Promise<void>
  logout: () => void
  isValid: () => boolean
}

function loadToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY)
  const expiresAt = localStorage.getItem(EXPIRES_KEY)
  if (!token || !expiresAt) return null
  if (new Date(expiresAt).getTime() <= Date.now()) return null
  return token
}

export const useAdminAuthStore = create<AdminAuthState>((set, get) => ({
  adminToken: loadToken(),
  expiresAt: localStorage.getItem(EXPIRES_KEY),

  async login(password) {
    const res = await adminLogin(password)
    localStorage.setItem(TOKEN_KEY, res.adminToken)
    localStorage.setItem(EXPIRES_KEY, res.expiresAt)
    set({ adminToken: res.adminToken, expiresAt: res.expiresAt })
  },

  logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EXPIRES_KEY)
    set({ adminToken: null, expiresAt: null })
  },

  isValid() {
    const { adminToken, expiresAt } = get()
    if (!adminToken || !expiresAt) return false
    return new Date(expiresAt).getTime() > Date.now()
  },
}))
