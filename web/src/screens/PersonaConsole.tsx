import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import HairlineSection from '../components/HairlineSection'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { getPersonas, simulatePersona } from '../features/persona/api'
import type { Persona, PersonaSimulationResult } from '../types/api'

const HYPOTHESES = ['H1', 'H2', 'H3', 'H4', 'H5']

// D2 페르소나봇 콘솔. 여권 은유 없이 밀도 높은 운영 도구 레이아웃을 쓴다(DESIGN_SYSTEM.md 9장).
export default function PersonaConsole() {
  const t = useT()
  const [personas, setPersonas] = useState<Persona[] | null>(null)
  const [selectedPersona, setSelectedPersona] = useState<string | null>(null)
  const [hypothesis, setHypothesis] = useState(HYPOTHESES[0]!)
  const [variantA, setVariantA] = useState('INFO_LIST')
  const [variantB, setVariantB] = useState('CONVERSATIONAL')
  const [productId, setProductId] = useState('SKY-STREAM-W260')
  const [result, setResult] = useState<PersonaSimulationResult | null>(null)
  const [running, setRunning] = useState(false)
  const [error, setError] = useState(false)

  useEffect(() => {
    getPersonas()
      .then((list) => {
        setPersonas(list)
        setSelectedPersona(list[0]?.id ?? null)
      })
      .catch(() => setError(true))
  }, [])

  async function handleRun() {
    if (!selectedPersona) return
    setRunning(true)
    setError(false)
    try {
      const res = await simulatePersona(selectedPersona, { hypothesis, variantA, variantB, productId })
      setResult(res)
    } catch {
      setError(true)
    } finally {
      setRunning(false)
    }
  }

  if (!personas) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bone-050">
        <Loading label={t('common.loading')} />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-bone-050 px-10 py-8">
      <div className="mx-auto flex max-w-[1440px] flex-col gap-6">
        <div className="flex items-center justify-between">
          <h1 className="t-display-m">{t('d2.title')}</h1>
          <div className="flex items-center gap-6">
            <Link to="/admin" className="t-label underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
              {t('d2.viewDashboard')}
            </Link>
            <Link to="/" className="t-label underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
              {t('nav.backToMenu')}
            </Link>
          </div>
        </div>

        {result && (
          <div className="border-t p-4" style={{ borderColor: 'var(--hairline)' }}>
            <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
              {result.disclaimer}
            </p>
          </div>
        )}

        <div className="grid grid-cols-5 gap-4">
          {personas.map((p) => (
            <button
              key={p.id}
              onClick={() => setSelectedPersona(p.id)}
              className="flex flex-col gap-2 border-t p-4 text-left"
              style={{ borderColor: 'var(--hairline)', background: selectedPersona === p.id ? 'var(--bone-100)' : 'transparent' }}
            >
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {p.id}
              </span>
              <span className="t-body">{p.name}</span>
              <span className="t-body-s" style={{ color: 'var(--graphite)' }}>
                {p.description}
              </span>
            </button>
          ))}
        </div>

        <HairlineSection>
          <div className="grid grid-cols-4 gap-4">
            <label className="flex flex-col gap-1">
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {t('d2.hypothesis')}
              </span>
              <select
                value={hypothesis}
                onChange={(e) => setHypothesis(e.target.value)}
                className="t-body h-[44px] bg-transparent"
                style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
              >
                {HYPOTHESES.map((h) => (
                  <option key={h} value={h}>
                    {h}
                  </option>
                ))}
              </select>
            </label>
            <label className="flex flex-col gap-1">
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {t('d2.variantA')}
              </span>
              <input
                value={variantA}
                onChange={(e) => setVariantA(e.target.value)}
                className="t-body h-[44px] bg-transparent"
                style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {t('d2.variantB')}
              </span>
              <input
                value={variantB}
                onChange={(e) => setVariantB(e.target.value)}
                className="t-body h-[44px] bg-transparent"
                style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
              />
            </label>
            <label className="flex flex-col gap-1">
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {t('d2.product')}
              </span>
              <input
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
                className="t-body h-[44px] bg-transparent"
                style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
              />
            </label>
          </div>
          <Button variant="primary" className="mt-4 w-auto px-8" onClick={() => void handleRun()} disabled={running || !selectedPersona}>
            {t('d2.run')}
          </Button>
          {error && (
            <p className="t-body-s mt-2" style={{ color: 'var(--ink-700)' }}>
              {t('d2.loadFail')}
            </p>
          )}
        </HairlineSection>

        {result && (
          <table className="w-full text-left">
            <thead>
              <tr style={{ borderBottom: '1px solid var(--hairline)' }}>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d2.resultVariant')}
                  </span>
                </th>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d2.resultSaved')}
                  </span>
                </th>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d2.resultReason')}
                  </span>
                </th>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d2.resultUnresolved')}
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              {result.results.map((r) => (
                <tr key={r.variant} style={{ borderBottom: '1px solid var(--hairline)' }}>
                  <td className="py-2 font-mono text-[13px]">{r.variant}</td>
                  <td className="py-2 t-body-s">{r.saved ? t('d2.savedYes') : t('d2.savedNo')}</td>
                  <td className="py-2 t-body-s">{r.reason}</td>
                  <td className="py-2 font-mono text-[13px]">{r.unresolved}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
