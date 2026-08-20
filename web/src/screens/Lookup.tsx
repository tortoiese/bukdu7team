import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import Button from '../components/Button'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useKnownContactStore } from '../features/knownContact/store'
import { lookupSession } from '../features/recap/api'
import { ApiError } from '../features/client'

const STORAGE_KEY = 'entry.sid'

// 매직링크(/resume) 없이, 방문 리캡에서 연결해둔 이메일을 직접 입력해 돌아오는 경로.
// 서버는 이메일 원문을 저장하지 않으므로(해시만 대조) 여기서 입력한 값을 그대로 화면 상단
// 표시용으로만 이 브라우저에 남긴다(features/knownContact).
export default function Lookup() {
  const t = useT()
  const navigate = useNavigate()
  const bootstrap = useSessionStore((s) => s.bootstrap)
  const setKnownEmail = useKnownContactStore((s) => s.setEmail)

  const [email, setEmailInput] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [notFound, setNotFound] = useState(false)

  async function handleSubmit() {
    if (!email.trim() || submitting) return
    setSubmitting(true)
    setNotFound(false)
    try {
      const res = await lookupSession(email.trim())
      localStorage.setItem(STORAGE_KEY, res.sessionId)
      setKnownEmail(email.trim())
      await bootstrap()
      navigate('/passport', { replace: true })
    } catch (err) {
      if (err instanceof ApiError && err.code === 'CONTACT_NOT_FOUND') setNotFound(true)
      else setNotFound(true)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pt-6">
        <div>
          <h1 className="t-display-m">{t('lookup.title')}</h1>
          <p className="t-body-s mt-2" style={{ color: 'var(--graphite)' }}>
            {t('lookup.body')}
          </p>
        </div>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmailInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && void handleSubmit()}
          placeholder={t('p3.emailPlaceholder')}
          autoFocus
          className="t-body h-[44px] bg-transparent px-1"
          style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
        />
        {notFound && (
          <p className="t-body-s" style={{ color: 'var(--seal)' }}>
            {t('lookup.notFound')}
          </p>
        )}
        <Button variant="primary" onClick={() => void handleSubmit()} disabled={!email.trim() || submitting}>
          {t('lookup.submit')}
        </Button>
      </div>
    </MobileFrame>
  )
}
