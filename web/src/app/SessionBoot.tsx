import { useEffect, type ReactNode } from 'react'
import { useSessionStore } from '../features/session/store'

// 앱 부팅 시 익명 세션을 1회 발급/복원한다. 세션 발급 실패로 화면이 막히면 안 되므로
// 실패해도 children은 그대로 렌더한다(요청 단계에서 재시도된다).
export default function SessionBoot({ children }: { children: ReactNode }) {
  const bootstrap = useSessionStore((s) => s.bootstrap)

  useEffect(() => {
    void bootstrap()
  }, [bootstrap])

  return <>{children}</>
}
