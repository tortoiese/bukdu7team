import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import HairlineSection from '../components/HairlineSection'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { getAdvisorBriefing, addAdvisorNote } from '../features/advisor/api'
import { ApiError } from '../features/client'
import type { AdvisorBriefing } from '../types/api'

function formatCountdown(ms: number): string {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

// P7 어드바이저 뷰. 태블릿 가로 기준 — 고객 화면의 여권 은유를 쓰지 않고 밀도 높은 응대용 레이아웃을 쓴다.
export default function Advisor() {
  const { grantToken } = useParams<{ grantToken: string }>()
  const t = useT()
  const sessionReady = useSessionStore((s) => s.ready)

  const [briefing, setBriefing] = useState<AdvisorBriefing | null>(null)
  const [expired, setExpired] = useState(false)
  const [loadError, setLoadError] = useState(false)
  const [remainingMs, setRemainingMs] = useState(0)
  const [note, setNote] = useState('')
  const [noteSaved, setNoteSaved] = useState(false)

  useEffect(() => {
    if (!sessionReady || !grantToken) return
    getAdvisorBriefing(grantToken)
      .then(setBriefing)
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.code === 'GRANT_EXPIRED') setExpired(true)
        else setLoadError(true)
      })
  }, [sessionReady, grantToken])

  useEffect(() => {
    if (!briefing) return
    const tick = () => {
      const remaining = new Date(briefing.expiresAt).getTime() - Date.now()
      setRemainingMs(remaining)
      if (remaining <= 0) setExpired(true)
    }
    tick()
    const timer = setInterval(tick, 1000)
    return () => clearInterval(timer)
  }, [briefing])

  async function handleSaveNote() {
    if (!grantToken || !note.trim()) return
    try {
      await addAdvisorNote(grantToken, note.trim())
      setNote('')
      setNoteSaved(true)
      setTimeout(() => setNoteSaved(false), 2400)
    } catch {
      setLoadError(true)
    }
  }

  if (loadError) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-ink-900">
        <p className="t-body">{t('p7.loadFail')}</p>
      </div>
    )
  }

  if (expired) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-ink-900 text-center">
        <p className="t-display-m">{t('p7.expiredTitle')}</p>
        <p className="t-body" style={{ color: 'var(--graphite)' }}>
          {t('p7.expiredBody')}
        </p>
      </div>
    )
  }

  if (!briefing) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-ink-900">
        <Loading label={t('common.loading')} />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-ink-900 px-8 py-8">
      <div className="mx-auto flex max-w-[960px] flex-col gap-6">
        <div className="flex items-center justify-between">
          <p className="t-display-l">{briefing.briefing.text}</p>
          <span className="t-label shrink-0 pl-4" style={{ color: 'var(--graphite)' }}>
            {t('p7.expiresIn', { time: formatCountdown(remainingMs) })}
          </span>
        </div>
        {!briefing.briefing.aiUsed && (
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            {t('common.observedCaption')}
          </p>
        )}

        <div className="grid grid-cols-2 gap-6">
          <HairlineSection title={t('p7.unresolved')}>
            {briefing.unresolved.length === 0 ? (
              <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
                —
              </p>
            ) : (
              <ul className="flex flex-col gap-2">
                {briefing.unresolved.map((u) => (
                  <li key={u.code} className="flex items-stretch gap-3">
                    <span aria-hidden="true" style={{ width: 4, background: 'var(--seal)' }} />
                    <span className="t-body py-0.5">{u.label}</span>
                  </li>
                ))}
              </ul>
            )}
          </HairlineSection>

          <HairlineSection title={t('p7.savedItems')}>
            <ul className="flex flex-col gap-2">
              {briefing.savedItems.map((item) => (
                <li key={item.productId} className="t-body">
                  {item.displayName}
                </li>
              ))}
            </ul>
          </HairlineSection>
        </div>

        <HairlineSection title={t('p7.customerLocale')}>
          <div className="flex items-center gap-6">
            <span className="t-mrz" style={{ color: 'var(--ink-700)' }}>
              {briefing.locale}
            </span>
            {briefing.keyPhrases.length > 0 && (
              <ul className="flex flex-wrap gap-4">
                {briefing.keyPhrases.map((phrase) => (
                  <li key={phrase.ko} className="t-body-s">
                    {phrase.ko} → {phrase.target}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </HairlineSection>

        <HairlineSection title={t('p7.noteLabel')}>
          <div className="flex gap-3">
            <input
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder={t('p7.notePlaceholder')}
              className="t-body h-[44px] flex-1 bg-transparent px-1"
              style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
            />
            <Button variant="primary" className="w-auto px-6" onClick={() => void handleSaveNote()} disabled={!note.trim()}>
              {t('p7.noteSubmit')}
            </Button>
          </div>
          {noteSaved && (
            <p className="t-label mt-2" style={{ color: 'var(--graphite)' }}>
              {t('p7.noteSaved')}
            </p>
          )}
        </HairlineSection>
      </div>
    </div>
  )
}
