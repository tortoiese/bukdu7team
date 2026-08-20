import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import HairlineSection from '../components/HairlineSection'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useMrzStore } from '../features/mrz/store'
import { buildMrzLine } from '../features/mrz/format'
import { getRecap, linkAccount } from '../features/recap/api'
import type { RecapData } from '../types/api'

type Channel = 'EMAIL' | 'PHONE'

export default function Recap() {
  const t = useT()
  const market = useSessionStore((s) => s.market)
  const sessionReady = useSessionStore((s) => s.ready)
  const setMrz = useMrzStore((s) => s.set)

  const [recap, setRecap] = useState<RecapData | null>(null)
  const [channel, setChannel] = useState<Channel>('EMAIL')
  const [value, setValue] = useState('')
  const [consent, setConsent] = useState(false)
  const [linked, setLinked] = useState(false)
  const [emailSent, setEmailSent] = useState(false)
  const [linkError, setLinkError] = useState(false)
  const [skipped, setSkipped] = useState(false)

  useEffect(() => {
    if (!sessionReady) return
    getRecap().then(setRecap)
  }, [sessionReady])

  useEffect(() => {
    if (!recap) return
    const line1 = buildMrzLine('ENTRY', 'RECAP')
    const line2 = buildMrzLine(`VIEW${recap.viewedProducts.length}`, `MKT<${market}`)
    setMrz([line1, line2], t('p3.mrzAccessible', { count: recap.viewedProducts.length, market }))
  }, [recap, market, setMrz, t])

  async function handleLink() {
    setLinkError(false)
    try {
      const res = await linkAccount(channel, value, consent)
      setLinked(true)
      setEmailSent(res.emailSent)
    } catch {
      setLinkError(true)
    }
  }

  if (!recap) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  const showLinkForm = !linked && !skipped

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <div>
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            {recap.visitDate}
          </p>
          <h1 className="t-display-m">{recap.storeName}</h1>
        </div>

        <HairlineSection title={t('p3.viewedToday')}>
          <ul className="flex flex-col gap-3">
            {recap.viewedProducts.map((item) => (
              <li key={item.productId} className="flex items-baseline justify-between gap-3">
                <span className="t-body">
                  <span className="t-label mr-2" style={{ color: 'var(--graphite)' }}>
                    {String(item.order).padStart(2, '0')}
                  </span>
                  {item.displayName}
                </span>
                <span className="t-label" style={{ color: 'var(--graphite)' }}>
                  x{item.scanCount}
                </span>
              </li>
            ))}
          </ul>
        </HairlineSection>

        <HairlineSection title={t('p3.interestTrend')}>
          <p className="t-body">{recap.interestSummary.text}</p>
          {!recap.interestSummary.aiUsed && (
            <p className="t-label mt-1" style={{ color: 'var(--graphite)' }}>
              {t('common.observedCaption')}
            </p>
          )}
        </HairlineSection>

        {recap.unresolvedFactors.length > 0 && (
          <HairlineSection title={t('p3.unresolved')}>
            <div className="flex flex-col gap-3">
              {recap.unresolvedFactors.map((factor) => (
                <div key={factor.code} className="flex items-stretch gap-3">
                  <span aria-hidden="true" style={{ width: 4, background: 'var(--seal)' }} />
                  <span className="t-body py-0.5">{factor.label}</span>
                </div>
              ))}
            </div>
          </HairlineSection>
        )}

        <HairlineSection title={t('p3.accountLink')}>
          <p className="t-body-s mb-4" style={{ color: 'var(--graphite)' }}>
            {t('p3.accountLinkNote')}
          </p>

          {linked && (
            <div className="flex flex-col gap-1">
              <p className="t-body">{t('p3.linked')}</p>
              {emailSent && (
                <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
                  {t('p3.linkEmailSent')}
                </p>
              )}
            </div>
          )}

          {showLinkForm && (
            <div className="flex flex-col gap-4">
              <div className="flex gap-4">
                <Button variant={channel === 'EMAIL' ? 'primary' : 'secondary'} onClick={() => setChannel('EMAIL')} className="flex-1">
                  {t('p3.emailLabel')}
                </Button>
                <Button variant={channel === 'PHONE' ? 'primary' : 'secondary'} onClick={() => setChannel('PHONE')} className="flex-1">
                  {t('p3.phoneLabel')}
                </Button>
              </div>
              <input
                type={channel === 'EMAIL' ? 'email' : 'tel'}
                value={value}
                onChange={(e) => setValue(e.target.value)}
                placeholder={t(channel === 'EMAIL' ? 'p3.emailPlaceholder' : 'p3.phonePlaceholder')}
                className="t-body h-[44px] bg-transparent px-1"
                style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
              />
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={consent} onChange={(e) => setConsent(e.target.checked)} className="h-[20px] w-[20px]" />
                <span className="t-body-s">{t('p3.consent')}</span>
              </label>
              {linkError && (
                <p className="t-body-s" style={{ color: 'var(--ink-700)' }}>
                  {t('p3.linkError')}
                </p>
              )}
              <div className="flex gap-4">
                <Button variant="primary" className="flex-1" disabled={!consent || !value} onClick={() => void handleLink()}>
                  {t('p3.linkSubmit')}
                </Button>
                <Button variant="text" onClick={() => setSkipped(true)}>
                  {t('p3.skip')}
                </Button>
              </div>
            </div>
          )}
        </HairlineSection>

        <Link to="/archive">
          <Button variant="secondary">{t('p3.continueArchive')}</Button>
        </Link>
        <Link to="/passport" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
          {t('nav.viewPassport')}
        </Link>
      </div>
    </MobileFrame>
  )
}
