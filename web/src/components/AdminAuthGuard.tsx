import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAdminAuthStore } from '../features/adminAuth/store'

// D1/D2 라우트를 감싼다. 유효한 관리자 토큰이 없으면 /entryadmin으로 보낸다.
// 토큰이 서버에서 만료된 경우는 각 화면의 API 에러 처리(ADMIN_AUTH_REQUIRED)에서 로그아웃 처리한다.
export default function AdminAuthGuard({ children }: { children: ReactNode }) {
  const isValid = useAdminAuthStore((s) => s.isValid())

  if (!isValid) return <Navigate to="/entryadmin" replace />
  return <>{children}</>
}
