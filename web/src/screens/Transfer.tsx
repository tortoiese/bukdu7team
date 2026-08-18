import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { motion, useReducedMotion } from 'motion/react'
import MobileFrame from '../components/MobileFrame'
import HairlineSection from '../components/HairlineSection'
import StockStatus from '../components/StockStatus'
import Button from '../components/Button'
import Loading from '../components/Loading'
import Toast from '../components/Toast'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useMrzStore } from '../features/mrz/store'
import { buildMrzLine } from '../features/mrz/format'
import { getTransfer } from '../features/transfer/api'
import type { Market, TransferData, TransferItem } from '../types/api'

const CLIMAX_DURATION_MS = 1200
const STAGGER_MS = 120

function stockLabel(item: TransferItem, t: ReturnType<typeof useT>): string {
  switch (item.status) {
    case 'IN_STOCK':
      return item.storeName ?? ''
    case 'TRANSFERABLE':
      return t('p4.transferableDays', { days: item.transferDays ?? 0 })
    case 'ONLINE_ONLY':
      return t('p4.onlineAvailable')
    case 'OUT_OF_STOCK':
    default:
      return t('p4.outOfStock')
  }
}

interface TransferCardProps {
  item: TransferItem
  index: number
  revealed: boolean
  onAction: (item: TransferItem) => void
}

// 저장 카드 1건. 좌(-12px)에서 제자리로 이동하며 미확인 상태 → 실제 시장 재고로 내용이 교체된다.
function TransferCard({ item, index, revealed, onAction }: TransferCardProps) {
  const t = useT()
  const reduceMotion = useReducedMotion()

  return (
    <motion.div
      initial={false}
      animate={{ x: revealed || reduceMotion ? 0 : -12, opacity: revealed || reduceMotion ? 1 : 0.5 }}
      transition={reduceMotion ? { duration: 0 } : { duration: 0.5, delay: (index * STAGGER_MS) / 1000, ease: [0.2, 0.9, 0.2, 1] }}
    >
      <HairlineSection>
        <div className="flex flex-col gap-2">
          <p className="t-body">{item.displayName}</p>
          {revealed || reduceMotion ? (
            <StockStatus status={item.status} label={stockLabel(item, t)} />
          ) : (
            <p className="t-label" style={{ color: 'var(--graphite)' }}>
              {t('p4.checking')}
            </p>
          )}
          <motion.div
            animate={{ opacity: revealed || reduceMotion ? 1 : 0 }}
            transition={{ duration: 0.3, delay: reduceMotion ? 0 : (index * STAGGER_MS) / 1000 + 0.3 }}
          >
            {item.action.type === 'ONLINE' && item.action.url ? (
              <a href={item.action.url} target="_blank" rel="noreferrer">
                <Button variant="secondary">{item.action.label}</Button>
              </a>
            ) : (
              <Button variant="secondary" onClick={() => onAction(item)}>
                {item.action.label}
              </Button>
            )}
          </motion.div>
        </div>
      </HairlineSection>
    </motion.div>
  )
}

export default function Transfer() {
  const t = useT()
  const sessionMarket = useSessionStore((s) => s.market)
  const sessionReady = useSessionStore((s) => s.ready)
  const setMrz = useMrzStore((s) => s.set)
  const reduceMotion = useReducedMotion()
  const [searchParams] = useSearchParams()

  const targetMarket = (searchParams.get('market') as Market | null) ?? sessionMarket
  const replayFlag = searchParams.get('replay')

  const [data, setData] = useState<TransferData | null>(null)
  const [revealed, setRevealed] = useState(false)
  const [toast, setToast] = useState<[string, string?] | null>(null)

  useEffect(() => {
    if (!sessionReady) return
    let cancelled = false
    getTransfer(targetMarket).then((res) => {
      if (cancelled) return
      setData(res)
      setRevealed(false)
    })
    return () => {
      cancelled = true
    }
  }, [sessionReady, targetMarket, replayFlag])

  // 진입 시: MRZ를 KR 상태로 먼저 세팅했다가, 카드 스태거 시작과 동시에 대상 시장으로 1.2초 전환한다.
  // reduced-motion에서는 같은 경로를 지연 0ms로 태워 최종 상태를 즉시 반영한다.
  useEffect(() => {
    if (!data) return
    const fromLine = buildMrzLine(`ITEMS${data.items.length}`, data.mrzTransition.from)
    setMrz([buildMrzLine('ENTRY', 'TRANSFER'), fromLine], t('p4.mrzAccessible', { count: data.items.length, market: 'KR' }))

    const timer = setTimeout(
      () => {
        setRevealed(true)
        const toLine = buildMrzLine(`ITEMS${data.items.length}`, data.mrzTransition.to)
        setMrz(
          [buildMrzLine('ENTRY', 'TRANSFER'), toLine],
          t('p4.mrzAccessible', { count: data.items.length, market: data.targetMarket }),
          reduceMotion ? undefined : CLIMAX_DURATION_MS,
        )
      },
      reduceMotion ? 0 : 200,
    )
    return () => clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data, reduceMotion])

  function handleAction(item: TransferItem) {
    setToast([t('p4.actionConfirmToast'), item.displayName])
  }

  if (!data) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  const actionsVisible = revealed || reduceMotion

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <div>
          <h1 className="t-display-m">{data.originStore}</h1>
          <div className="my-3 flex items-center gap-3">
            <span aria-hidden="true" style={{ flex: 1, borderTop: '1px solid var(--hairline)' }} />
            <span className="t-mrz" style={{ color: 'var(--cognac)' }}>
              {data.targetMarket}
            </span>
            <span aria-hidden="true" style={{ flex: 1, borderTop: '1px solid var(--hairline)' }} />
          </div>
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            {t('p4.title')} · {data.currency}
          </p>
        </div>

        {data.items.length === 0 ? (
          <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
            {t('p4.empty')}
          </p>
        ) : (
          <div className="flex flex-col">
            {data.items.map((item, index) => (
              <TransferCard key={item.productId} item={item} index={index} revealed={revealed} onAction={handleAction} />
            ))}
          </div>
        )}

        {data.unresolvedAnswers.length > 0 && (
          <motion.div animate={{ opacity: actionsVisible ? 1 : 0 }} transition={{ duration: 0.3 }}>
            <HairlineSection title={t('p4.unresolvedAnswers')}>
              <div className="flex flex-col gap-4">
                {data.unresolvedAnswers.map((answer) => (
                  <div key={answer.code} className="flex flex-col gap-1">
                    <p className="t-body">{answer.question}</p>
                    <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
                      {answer.answer}
                    </p>
                    {!answer.aiUsed && (
                      <p className="t-label" style={{ color: 'var(--graphite)' }}>
                        {t('common.observedCaption')}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </HairlineSection>
          </motion.div>
        )}

        <motion.p
          className="t-label"
          style={{ color: 'var(--graphite)' }}
          animate={{ opacity: actionsVisible ? 1 : 0 }}
          transition={{ duration: 0.3 }}
        >
          {t('p4.sendTimingCaption')}: {data.sendTiming.rationale}
        </motion.p>
      </div>

      {toast && <Toast lines={toast} onDismiss={() => setToast(null)} />}
    </MobileFrame>
  )
}
