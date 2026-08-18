import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { getIntentDashboard } from '../features/admin/api'
import type { IntentDashboardData } from '../types/api'

type SortKey = 'productId' | 'scans' | 'rescanRate' | 'saveRate' | 'conversionRate'

function KpiTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-2 border-t p-4" style={{ borderColor: 'var(--hairline)' }}>
      <span className="t-label" style={{ color: 'var(--graphite)' }}>
        {label}
      </span>
      <span className="font-mono text-[32px] leading-none" style={{ color: 'var(--ink-700)' }}>
        {value}
      </span>
    </div>
  )
}

function pct(value: number): string {
  return `${Math.round(value * 100)}%`
}

// D1 의도 대시보드. 고객 화면과 달리 여권 은유를 쓰지 않는다(DESIGN_SYSTEM.md 9장).
export default function Admin() {
  const t = useT()
  const [data, setData] = useState<IntentDashboardData | null>(null)
  const [error, setError] = useState(false)
  const [sort, setSort] = useState<{ key: SortKey; dir: 1 | -1 }>({ key: 'scans', dir: -1 })

  useEffect(() => {
    getIntentDashboard().then(setData).catch(() => setError(true))
  }, [])

  function toggleSort(key: SortKey) {
    setSort((prev) => (prev.key === key ? { key, dir: prev.dir === 1 ? -1 : 1 } : { key, dir: -1 }))
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bone-050">
        <p className="t-body">{t('d1.loadFail')}</p>
      </div>
    )
  }

  if (!data) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bone-050">
        <Loading label={t('common.loading')} />
      </div>
    )
  }

  const totalScans = data.products.reduce((sum, p) => sum + p.scans, 0)
  const weightedAvg = (pick: (p: IntentDashboardData['products'][number]) => number) =>
    totalScans === 0 ? 0 : data.products.reduce((sum, p) => sum + pick(p) * p.scans, 0) / totalScans

  const sortedProducts = [...data.products].sort((a, b) => {
    const av = a[sort.key]
    const bv = b[sort.key]
    if (av < bv) return -1 * sort.dir
    if (av > bv) return 1 * sort.dir
    return 0
  })

  const columns: { key: SortKey; label: string }[] = [
    { key: 'productId', label: t('d1.colProduct') },
    { key: 'scans', label: t('d1.colScans') },
    { key: 'rescanRate', label: t('d1.colRescan') },
    { key: 'saveRate', label: t('d1.colSave') },
    { key: 'conversionRate', label: t('d1.colConversion') },
  ]

  return (
    <div className="min-h-screen bg-bone-050 px-10 py-8">
      <div className="mx-auto flex max-w-[1440px] flex-col gap-8">
        <h1 className="t-display-m">{t('d1.title')}</h1>

        <div className="grid grid-cols-4 gap-6">
          <KpiTile label={t('d1.totalScans')} value={String(totalScans)} />
          <KpiTile label={t('d1.rescanRate')} value={pct(weightedAvg((p) => p.rescanRate))} />
          <KpiTile label={t('d1.saveRate')} value={pct(weightedAvg((p) => p.saveRate))} />
          <KpiTile label={t('d1.transferRecoveryRate')} value={pct(data.transferRecovery.rate)} />
        </div>

        <div>
          <h2 className="t-label mb-3" style={{ color: 'var(--graphite)' }}>
            {t('d1.productTable')}
          </h2>
          {sortedProducts.length === 0 ? (
            <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
              {t('d1.empty')}
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--hairline)' }}>
                    {columns.map((col) => (
                      <th key={col.key} className="cursor-pointer pb-2" onClick={() => toggleSort(col.key)}>
                        <span className="t-label" style={{ color: 'var(--graphite)' }}>
                          {col.label} {sort.key === col.key ? (sort.dir === 1 ? '↑' : '↓') : ''}
                        </span>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {sortedProducts.map((p) => (
                    <tr key={p.productId} style={{ borderBottom: '1px solid var(--hairline)' }}>
                      <td className="py-2 font-mono text-[13px]">{p.productId}</td>
                      <td className="py-2 font-mono text-[13px]">{p.scans}</td>
                      <td className="py-2 font-mono text-[13px]">{pct(p.rescanRate)}</td>
                      <td className="py-2 font-mono text-[13px]">{pct(p.saveRate)}</td>
                      <td className="py-2 font-mono text-[13px]">{pct(p.conversionRate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="grid grid-cols-2 gap-8">
          <div>
            <h2 className="t-label mb-3" style={{ color: 'var(--graphite)' }}>
              {t('d1.unresolvedChart')}
            </h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={data.unresolvedDistribution}>
                <CartesianGrid stroke="var(--hairline)" vertical={false} />
                <XAxis dataKey="code" tick={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} />
                <YAxis tick={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="count" fill="var(--ink-700)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div>
            <h2 className="t-label mb-3" style={{ color: 'var(--graphite)' }}>
              {t('d1.marketChart')}
            </h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={data.marketDistribution}>
                <CartesianGrid stroke="var(--hairline)" vertical={false} />
                <XAxis dataKey="market" tick={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} />
                <YAxis tick={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="sessions" fill="var(--cognac)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div>
          <h2 className="t-label mb-3" style={{ color: 'var(--graphite)' }}>
            {t('d1.zoneTable')}
          </h2>
          <table className="w-full text-left">
            <thead>
              <tr style={{ borderBottom: '1px solid var(--hairline)' }}>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d1.colZone')}
                  </span>
                </th>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d1.colDwell')}
                  </span>
                </th>
                <th className="pb-2">
                  <span className="t-label" style={{ color: 'var(--graphite)' }}>
                    {t('d1.colSaves')}
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              {data.zonePerformance.map((z) => (
                <tr key={z.zoneId} style={{ borderBottom: '1px solid var(--hairline)' }}>
                  <td className="py-2 font-mono text-[13px]">{z.zoneId}</td>
                  <td className="py-2 font-mono text-[13px]">{z.avgDwellSeconds}</td>
                  <td className="py-2 font-mono text-[13px]">{z.saves}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data.actionHints.length > 0 && (
          <div className="border-t p-4" style={{ borderColor: 'var(--hairline)' }}>
            <h2 className="t-label mb-2" style={{ color: 'var(--graphite)' }}>
              {t('d1.actionHints')}
            </h2>
            <ul className="flex flex-col gap-1">
              {data.actionHints.map((hint) => (
                <li key={hint} className="t-body">
                  {hint}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}
