import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import HairlineSection from '../components/HairlineSection'
import StockStatus from '../components/StockStatus'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useMrzStore } from '../features/mrz/store'
import { buildMrzLine } from '../features/mrz/format'
import { getArchive } from '../features/archive/api'
import type { ArchiveList } from '../types/api'

export default function Archive() {
  const t = useT()
  const market = useSessionStore((s) => s.market)
  const sessionReady = useSessionStore((s) => s.ready)
  const setMrz = useMrzStore((s) => s.set)

  const [list, setList] = useState<ArchiveList | null>(null)

  useEffect(() => {
    if (!sessionReady) return
    getArchive(market).then(setList)
  }, [sessionReady, market])

  useEffect(() => {
    if (!list) return
    const line1 = buildMrzLine('ENTRY', 'ARCHIVE')
    const line2 = buildMrzLine(`SAVED${list.items.length}`, `MKT<${market}`)
    setMrz([line1, line2], t('p5.mrzAccessible', { count: list.items.length, market }))
  }, [list, market, setMrz, t])

  if (!list) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <h1 className="t-display-m">{t('p5.title')}</h1>

        {list.items.length > 0 && (
          <div>
            <p className="t-body">{list.intentSummary.text}</p>
            {!list.intentSummary.aiUsed && (
              <p className="t-label mt-1" style={{ color: 'var(--graphite)' }}>
                {t('common.observedCaption')}
              </p>
            )}
          </div>
        )}

        {list.items.length === 0 ? (
          <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
            {t('empty.archive')}
          </p>
        ) : (
          <>
            <div className="flex flex-col">
              {list.items.map((item) => (
                <HairlineSection key={item.productId}>
                  <div className="flex flex-col gap-2">
                    <p className="t-body">{item.displayName}</p>
                    <p className="t-label" style={{ color: 'var(--graphite)' }}>
                      {item.savedAtStoreId} · {item.zoneId}
                    </p>
                    <StockStatus status={item.homeMarketStatus} label={t('p1.stockHome')} />
                  </div>
                </HairlineSection>
              ))}
            </div>
            <Link to="/transfer">
              <Button variant="primary">{t('p5.transferCta')}</Button>
            </Link>
          </>
        )}

        <div className="flex flex-col gap-3">
          <Link to="/passport" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('nav.viewPassport')}
          </Link>
          <Link to="/recap" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('nav.viewRecap')}
          </Link>
        </div>
      </div>
    </MobileFrame>
  )
}
