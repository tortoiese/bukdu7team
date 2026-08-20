import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import { useT } from '../i18n'
import { useAdminAuthStore } from '../features/adminAuth/store'
import { ApiError } from '../features/client'

// /entryadmin — D1/D2 전용 진입점. 게스트 메뉴(Home.tsx)에는 어디에도 링크하지 않는다.
// 여권 은유를 쓰지 않는다(DESIGN_SYSTEM.md 9장 D1/D2와 같은 층위).
export default function AdminLogin() {
  const t = useT()
  const navigate = useNavigate()
  const login = useAdminAuthStore((s) => s.login)

  const [password, setPassword] = useState('')
  const [error, setError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit() {
    if (!password || submitting) return
    setSubmitting(true)
    setError(false)
    try {
      await login(password)
      navigate('/admin')
    } catch (err) {
      if (err instanceof ApiError && err.code === 'ADMIN_PASSWORD_INVALID') setError(true)
      else setError(true)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="theme-light flex min-h-screen items-center justify-center bg-bone-050">
      <div className="flex w-full max-w-[360px] flex-col gap-6 px-6">
        <h1 className="t-display-m">{t('adminAuth.title')}</h1>
        <div className="flex flex-col gap-2">
          <label className="t-label" style={{ color: 'var(--graphite)' }}>
            {t('adminAuth.passwordLabel')}
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && void handleSubmit()}
            autoFocus
            className="t-body h-[44px] bg-transparent px-1"
            style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
          />
        </div>
        {error && (
          <p className="t-body-s" style={{ color: 'var(--seal)' }}>
            {t('adminAuth.invalid')}
          </p>
        )}
        <Button variant="primary" onClick={() => void handleSubmit()} disabled={!password || submitting}>
          {t('adminAuth.submit')}
        </Button>
      </div>
    </div>
  )
}
