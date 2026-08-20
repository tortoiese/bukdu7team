import { useEffect, useState } from 'react'
import { useParams, useSearchParams, Link } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import PassportCover from '../components/PassportCover'
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
import { useDemoStore } from '../features/demo/store'
import { DEMO_PRODUCT, DEMO_SCAN } from '../features/scan/demoData'
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
  const isDemo = useDemoStore((s) => s.isDemo)

  const [product, setProduct] = useState<ProductData | null>(null)
  const [scan, setScan] = useState<ScanResponse | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [savedCount, setSavedCount] = useState<number | null>(null)
  const [toast, setToast] = useState<[string, string?] | null>(null)
  // QR로 도착할 때마다(=마운트마다) 매대 태그를 처음 대는 순간처럼 표지가 먼저 뜬다.
  // 패스포트(P2)와 달리 세션당 1회 제한은 두지 않는다 — 스캔마다 다른 실물 태그를 댄 결과이므로.
  const [coverOpen, setCoverOpen] = useState(false)

  // 진입 시 1회: 스캔 이벤트 기록(POST /scans). 시장 전환과는 무관하게 한 번만 발생해야 한다.
  // 데모 모드(?demo=1 또는 네트워크 실패 후 자동 전환)에서는 호출 없이 고정 데이터를 쓴다.
  useEffect(() => {
    if (!ready || !productId) return
    let cancelled = false
    if (isDemo) {
      Promise.resolve(DEMO_SCAN).then((res) => {
        if (!cancelled) setScan(res)
      })
      return () => {
        cancelled = true
      }
    }
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
  }, [ready, productId, isDemo])

  // 제품/재고 조회: 시장이 바뀌면 재고 표기를 다시 가져온다.
  useEffect(() => {
    if (!ready || !productId) return
    let cancelled = false
    if (isDemo) {
      Promise.resolve(DEMO_PRODUCT).then((res) => {
        if (!cancelled) setProduct(res)
      })
      return () => {
        cancelled = true
      }
    }
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
  }, [ready, productId, market, isDemo])

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
    if (isDemo) {
      const next = (savedCount ?? 0) + 1
      setSavedCount(next)
      setToast([t('p1.savedToast'), t('p1.savedToastCount', { count: next })])
      return
    }
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
      <PassportCover isOpen={coverOpen} onOpen={() => setCoverOpen(true)} />
      <div aria-hidden={!coverOpen} className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <div className="flex justify-end">
          <Button variant="text" aria-label={t('common.marketSwitch')} onClick={() => void handleMarketSwitch()}>
            {market}
          </Button>
        </div>

        {scan && <CharacterBubble name={characterName} message={scan.greeting.message} />}

        {/* 제품 정보 카드. 다크 앱 크롬 위 밝은 내지 카드로 띄운다(피그마 P1 참조). */}
        <div className="theme-light flex flex-col gap-4 border p-4" style={{ background: 'var(--bone-050)', borderColor: 'var(--hairline)' }}>
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
            {savedCount !== null && (
              // 도장은 mix-blend-mode: multiply로 그려져 밝은 종이 위에서만 잉크처럼 보인다.
              // 다크 화면 위에 찍히므로 도장 뒤에 작은 밝은 패치를 둔다.
              <span
                className="inline-flex shrink-0 items-center justify-center rounded-full"
                style={{ background: 'var(--bone-050)', width: 52, height: 52 }}
              >
                <Stamp label="SAVED" rotationSeed={seedFromString(productId ?? '')} size={44} />
              </span>
            )}
          </div>
          <Button variant="secondary" onClick={handleCallStaff}>
            {t('p1.callStaff')}
          </Button>
          {scan && (
            <Link to={`/talk/${scan.scanId}`} className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
              {t('p1.continueTalk')}
            </Link>
          )}
          <Link to="/passport" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('nav.viewPassport')}
          </Link>
        </div>
      </div>

      {toast && <Toast lines={toast} onDismiss={() => setToast(null)} />}
    </MobileFrame>
  )
}
