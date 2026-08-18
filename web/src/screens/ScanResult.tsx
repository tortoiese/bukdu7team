import { useEffect, useState } from 'react'
import { useParams, useSearchParams, Link } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import CharacterBubble from '../components/CharacterBubble'
import { FieldGrid } from '../components/Field'
import HairlineSection from '../components/HairlineSection'
import StockStatus from '../components/StockStatus'
import Stamp from '../components/Stamp'
import Toast from '../components/Toast'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useMrzStore } from '../features/mrz/store'
import { buildMrzLine } from '../features/mrz/format'
import { getProduct } from '../features/products/api'
import { postScan } from '../features/scan/api'
import { saveToArchive } from '../features/archive/api'
import brand from '../brand/mcm.json'
import type { Locale, Market, ProductData, ScanResponse } from '../types/api'

// docs/API_CONTRACT.md에는 QR이 zone을 담지 않는 경우도 있어 ?zone= 쿼리를 우선하고,
// 없으면 매대 태그 기본 구역(PRODUCT)으로 간주한다. TODO(entry-P1): #11 QR 시트에서 zone 파라미터 확정.
const DEFAULT_ZONE = 'ZONE04'

const MARKET_CYCLE: { market: Market; locale: Locale }[] = [
  { market: 'KR', locale: 'ko' },
  { market: 'HK', locale: 'zh-Hant' },
  { market: 'JP', locale: 'ja' },
  { market: 'US', locale: 'en' },
]

// 제품ID로부터 세션 내 고정된 스탬프 회전각을 만든다. 서버 rotationSeed가 없는 P1 저장 도장 전용.
function seedFromString(value: string): number {
  let hash = 0
  for (const char of value) hash = (hash * 31 + char.charCodeAt(0)) % 100
  return Math.abs(hash)
}

export default function ScanResult() {
  const { productId } = useParams<{ productId: string }>()
  const [searchParams] = useSearchParams()
  const zoneId = searchParams.get('zone') ?? DEFAULT_ZONE

  const t = useT()
  const market = useSessionStore((s) => s.market)
  const ready = useSessionStore((s) => s.ready)
  const setMarket = useSessionStore((s) => s.setMarket)
  const setMrz = useMrzStore((s) => s.set)

  const [product, setProduct] = useState<ProductData | null>(null)
  const [scan, setScan] = useState<ScanResponse | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [savedCount, setSavedCount] = useState<number | null>(null)
  const [toast, setToast] = useState<[string, string?] | null>(null)

  // 진입 시 1회: 스캔 이벤트 기록(POST /scans). 시장 전환과는 무관하게 한 번만 발생해야 한다.
  useEffect(() => {
    if (!ready || !productId) return
    let cancelled = false
    postScan({ storeId: brand.originStore.storeId, zoneId, productId, scannedAt: new Date().toISOString() })
      .then((res) => {
        if (!cancelled) setScan(res)
      })
      .catch(() => {
        if (!cancelled) setLoadError(true)
      })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ready, productId])

  // 제품/재고 조회: 시장이 바뀌면 재고 표기를 다시 가져온다.
  useEffect(() => {
    if (!ready || !productId) return
    let cancelled = false
    getProduct(productId, market)
      .then((res) => {
        if (!cancelled) setProduct(res)
      })
      .catch(() => {
        if (!cancelled) setLoadError(true)
      })
    return () => {
      cancelled = true
    }
  }, [ready, productId, market])

  // 하단 MRZ 밴드: 실제 관측값(구역/스캔 횟수/저장 여부/시장)만 담는다.
  useEffect(() => {
    if (!productId) return
    const scanCount = scan?.sessionScanCount ?? 0
    const saved = savedCount ?? 0
    const line1 = buildMrzLine('ENTRY', productId, zoneId)
    const line2 = buildMrzLine(saved > 0 ? `SAVED${saved}` : `SCAN${scanCount}`, `MKT<${market}`)
    setMrz([line1, line2], t('p1.mrzAccessible', { zone: zoneId, scanCount, savedCount: saved, market }))
  }, [productId, zoneId, scan, savedCount, market, setMrz, t])

  async function handleSave() {
    if (!productId) return
    try {
      const res = await saveToArchive(productId, scan?.scanId)
      setSavedCount(res.savedCount)
      setToast([t('p1.savedToast'), t('p1.savedToastCount', { count: res.savedCount })])
    } catch {
      setLoadError(true)
    }
  }

  function handleCallStaff() {
    setToast([t('p1.staffCalledToast')])
  }

  async function handleMarketSwitch() {
    const idx = MARKET_CYCLE.findIndex((m) => m.market === market)
    const next = MARKET_CYCLE[(idx + 1) % MARKET_CYCLE.length]
    await setMarket(next.market, next.locale)
  }

  if (loadError && !product) {
    return (
      <MobileFrame>
        <div className="flex flex-1 flex-col items-center justify-center gap-4 text-center">
          <p className="t-body" style={{ color: 'var(--ink-700)' }}>
            {t('p1.loadFail')}
          </p>
          <Button variant="secondary" onClick={() => window.location.reload()}>
            {t('common.retry')}
          </Button>
        </div>
      </MobileFrame>
    )
  }

  if (!product) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  const character = scan?.greeting.character
  const characterName = character ? brand.characters[character].name : ''

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <div className="flex justify-end">
          <Button variant="text" aria-label={t('common.marketSwitch')} onClick={() => void handleMarketSwitch()}>
            {market}
          </Button>
        </div>

        {scan && <CharacterBubble name={characterName} message={scan.greeting.message} />}

        <div className="flex flex-col gap-4">
          <div>
            <p className="t-label" style={{ color: 'var(--graphite)' }}>
              {product.line}
            </p>
            <h1 className="t-display-m">{product.displayName}</h1>
          </div>
          <FieldGrid
            fields={[
              { label: t('p1.material'), value: product.material },
              { label: t('p1.weight'), value: `${product.weightGram}g` },
              { label: t('p1.size'), value: `${product.sizeLabel.local} (${product.sizeLabel.origin})` },
            ]}
          />
        </div>

        <div className="flex-1">
          <h2 className="t-label mb-1" style={{ color: 'var(--graphite)' }}>
            {t('p1.craftSection')}
          </h2>
          {product.craftNotes.map((note) => (
            <HairlineSection key={note.heading} title={note.heading}>
              <p className="t-body">{note.body}</p>
            </HairlineSection>
          ))}
        </div>

        <HairlineSection>
          <div className="flex flex-col gap-3">
            <StockStatus status={product.stock.thisStore} label={t('p1.stockThisStore')} />
            {product.stock.domesticOther.map((store) => (
              <StockStatus
                key={store.storeId}
                status={store.status}
                label={t('p1.stockDomestic')}
                caption={store.storeName}
              />
            ))}
            <StockStatus
              status={product.stock.homeMarket.status}
              label={t('p1.stockHome')}
              caption={`${product.stock.homeMarket.storeName} · ${product.stock.homeMarket.market}`}
            />
          </div>
        </HairlineSection>

        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-3">
            <Button variant="primary" className="flex-1" onClick={() => void handleSave()}>
              {t('p1.saveButton')}
            </Button>
            {savedCount !== null && <Stamp label="SAVED" rotationSeed={seedFromString(productId ?? '')} size={44} />}
          </div>
          <Button variant="secondary" onClick={handleCallStaff}>
            {t('p1.callStaff')}
          </Button>
          {scan && (
            <Link to={`/talk/${scan.scanId}`} className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
              {t('p1.continueTalk')}
            </Link>
          )}
        </div>
      </div>

      {toast && <Toast lines={toast} onDismiss={() => setToast(null)} />}
    </MobileFrame>
  )
}
