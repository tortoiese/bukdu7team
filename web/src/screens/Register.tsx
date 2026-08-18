import { useState } from 'react'
import MobileFrame from '../components/MobileFrame'
import GuillocheBg from '../components/GuillocheBg'
import HairlineSection from '../components/HairlineSection'
import { FieldGrid } from '../components/Field'
import Button from '../components/Button'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { register } from '../features/preregistration/api'
import type { PreregistrationResponse } from '../types/api'
import brand from '../brand/mcm.json'

type Channel = 'EMAIL' | 'PHONE'
type LineCode = keyof typeof brand.lines

export default function Register() {
  const t = useT()
  const market = useSessionStore((s) => s.market)

  const [selectedLines, setSelectedLines] = useState<LineCode[]>([])
  const [channel, setChannel] = useState<Channel>('EMAIL')
  const [value, setValue] = useState('')
  const [consent, setConsent] = useState(false)
  const [result, setResult] = useState<PreregistrationResponse | null>(null)
  const [error, setError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  function toggleLine(code: LineCode) {
    setSelectedLines((prev) => (prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code]))
  }

  async function handleSubmit() {
    setError(false)
    setSubmitting(true)
    try {
      const res = await register({ channel, value, interestedLines: selectedLines, market, consent })
      setResult(res)
    } catch {
      setError(true)
    } finally {
      setSubmitting(false)
    }
  }

  if (result) {
    return (
      <MobileFrame>
        <div className="flex flex-1 flex-col gap-6 pt-6">
          <p className="t-body">{t('p8.doneTitle')}</p>
          <div className="relative border p-4" style={{ borderColor: 'var(--hairline)' }}>
            <GuillocheBg />
            <div className="relative">
              <FieldGrid
                fields={[
                  { label: t('p8.slotLabel'), value: t('p8.benefit') },
                  { label: t('p8.timeWindowLabel'), value: result.timeWindow },
                  { label: t('p8.codeLabel'), value: result.code },
                ]}
              />
            </div>
          </div>
        </div>
      </MobileFrame>
    )
  }

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pt-6">
        <div>
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            {brand.originStore.storeName}
          </p>
          <h1 className="t-display-m">{brand.popupName}</h1>
        </div>

        <HairlineSection title={t('p8.interestedLines')}>
          <div className="flex flex-col gap-2">
            {(Object.keys(brand.lines) as LineCode[]).map((code) => (
              <label key={code} className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={selectedLines.includes(code)}
                  onChange={() => toggleLine(code)}
                  className="h-[20px] w-[20px]"
                />
                <span className="t-body">{brand.lines[code]}</span>
              </label>
            ))}
          </div>
        </HairlineSection>

        <HairlineSection>
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
              <span className="t-body-s">{t('p8.consent')}</span>
            </label>
          </div>
        </HairlineSection>

        <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
          {t('p8.benefit')}
        </p>

        {error && (
          <p className="t-body-s" style={{ color: 'var(--ink-700)' }}>
            {t('p8.registerFail')}
          </p>
        )}

        <Button
          variant="primary"
          disabled={!consent || !value || selectedLines.length === 0 || submitting}
          onClick={() => void handleSubmit()}
        >
          {t('p8.submit')}
        </Button>
      </div>
    </MobileFrame>
  )
}
