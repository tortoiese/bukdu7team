import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'

const STORAGE_KEY = 'entry.sid'

// 이메일로 받은 저장 링크(P3 계정 연결 시 발송)의 도착 지점. sessionId를 로컬에 심고
// 곧바로 패스포트로 보낸다 — 세션 ID를 아는 사람은 그 세션에 접근할 수 있다는 기존
// 익명 세션 신뢰 모델을 그대로 확장한 것뿐이라 별도 인증 절차를 두지 않는다.
export default function Resume() {
  const { sessionId } = useParams<{ sessionId: string }>()
  const navigate = useNavigate()
  const t = useT()
  const bootstrap = useSessionStore((s) => s.bootstrap)

  useEffect(() => {
    if (!sessionId) {
      navigate('/', { replace: true })
      return
    }
    localStorage.setItem(STORAGE_KEY, sessionId)
    void bootstrap().then(() => navigate('/passport', { replace: true }))
  }, [sessionId, navigate, bootstrap])

  return (
    <MobileFrame>
      <div className="flex flex-1 items-center justify-center">
        <Loading label={t('common.loading')} />
      </div>
    </MobileFrame>
  )
}
