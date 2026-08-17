import { useEffect, useState } from 'react'
import { getHealth, type HealthData } from '../features/system/api'
import { useT } from '../i18n'
import MobileFrame from '../components/MobileFrame'

type Status = 'checking' | 'ok' | 'fail'

export default function Home() {
  const t = useT()
  const [status, setStatus] = useState<Status>('checking')
  const [health, setHealth] = useState<HealthData | null>(null)

  useEffect(() => {
    getHealth()
      .then((data) => {
        setHealth(data)
        setStatus('ok')
      })
      .catch(() => setStatus('fail'))
  }, [])

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col justify-center gap-4">
        <h1 className="t-display-l">{t('common.health.title')}</h1>
        <p className="t-label" style={{ color: 'var(--graphite)' }}>
          {status === 'checking' && t('common.health.checking')}
          {status === 'ok' && t('common.health.ok')}
          {status === 'fail' && t('common.health.fail')}
        </p>
        {health && (
          <div className="t-mrz" style={{ color: 'var(--ink-700)' }}>
            STATUS&lt;{health.status}&gt;&lt;VERSION&lt;{health.version}&gt;&lt;PROFILE&lt;{health.profile}&gt;
          </div>
        )}
      </div>
    </MobileFrame>
  )
}
