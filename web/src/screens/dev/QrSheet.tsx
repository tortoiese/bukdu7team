import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import QRCode from 'qrcode'
import { getProductList } from '../../features/products/api'
import brand from '../../brand/mcm.json'
import type { Market, ProductSummary } from '../../types/api'

interface QrEntry {
  code: string
  label: string
  url: string
  dataUrl: string
}

interface StoreEntry {
  storeId: string
  storeName: string
  city: string
}

const PRINT_STYLE = `
@media print {
  @page { size: A4; margin: 12mm; }
  .qr-sheet-grid { grid-template-columns: repeat(4, 1fr) !important; }
  .qr-sheet-item { break-inside: avoid; }
  .qr-sheet-image { width: 40mm !important; height: 40mm !important; }
}
`

// 국경 이전(P4) 데모용 — 실제 MCM 매장이 있는 해외 도시 위주로 구성했다.
const TRANSFER_MARKETS: { market: Market; label: string }[] = [
  { market: 'HK', label: '홍콩 · 코즈웨이베이' },
  { market: 'JP', label: '도쿄 · 오모테산도' },
  { market: 'SG', label: '싱가포르 · 아이온 오차드' },
  { market: 'US', label: '뉴욕 · 소호' },
]

async function toQr(url: string): Promise<string> {
  return QRCode.toDataURL(url, { margin: 1, width: 240 })
}

// 발표 시연용 QR 인쇄 시트. 제품 20종은 /s/{productId}?store=, 구역 4개는 /z/{zoneId}?store=로 인코딩한다.
// ORIGIN은 window.location.origin이라 배포 URL로 자동 반영된다(PROMPTS.md #11).
// 매장(성수/더현대/합정/문래)마다 스캔 시 패스포트에 찍히는 발급 장소가 다르므로, 매장을 고르면
// 그 매장 전용 QR 세트로 다시 생성된다.
export default function QrSheet() {
  const stores = brand.stores as StoreEntry[]
  const [selectedStore, setSelectedStore] = useState(stores[0]!.storeId)
  const [entries, setEntries] = useState<QrEntry[] | null>(null)
  const [transferEntries, setTransferEntries] = useState<QrEntry[] | null>(null)

  useEffect(() => {
    const origin = window.location.origin

    async function build() {
      setEntries(null)
      const products = await getProductList()
      const zoneEntries = Object.entries(brand.zones).map(([zoneId, name]) => ({
        code: zoneId,
        label: name,
        url: `${origin}/z/${zoneId}?store=${selectedStore}`,
      }))
      const productEntries = products.map((p: ProductSummary) => ({
        code: p.productId,
        label: p.displayName,
        url: `${origin}/s/${p.productId}?store=${selectedStore}`,
      }))

      const all = [...zoneEntries, ...productEntries]
      const withQr = await Promise.all(all.map(async (item) => ({ ...item, dataUrl: await toQr(item.url) })))
      setEntries(withQr)
    }

    void build()
  }, [selectedStore])

  useEffect(() => {
    const origin = window.location.origin
    async function build() {
      const withQr = await Promise.all(
        TRANSFER_MARKETS.map(async ({ market, label }) => {
          const url = `${origin}/transfer?market=${market}`
          return { code: market, label, url, dataUrl: await toQr(url) }
        }),
      )
      setTransferEntries(withQr)
    }
    void build()
  }, [])

  return (
    <div className="theme-light min-h-screen bg-bone-050 px-8 py-8">
      <style>{PRINT_STYLE}</style>
      <div className="mx-auto max-w-[1000px]">
        <div className="print:hidden mb-6 flex items-center justify-between">
          <div>
            <Link to="/admin" className="t-label underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
              메뉴로
            </Link>
            <h1 className="t-display-m mt-1">ENTRY QR SHEET</h1>
          </div>
          <button
            onClick={() => window.print()}
            className="t-label h-[44px] border px-4"
            style={{ borderColor: 'var(--ink-700)', color: 'var(--ink-700)' }}
          >
            PRINT
          </button>
        </div>

        <div className="print:hidden mb-6">
          <h2 className="t-label mb-2" style={{ color: 'var(--graphite)' }}>
            매장 선택 (지역별 팝업)
          </h2>
          <div className="flex flex-wrap gap-3">
            {stores.map((store) => (
              <button
                key={store.storeId}
                onClick={() => setSelectedStore(store.storeId)}
                className="t-label h-[40px] border px-4"
                style={{
                  borderColor: selectedStore === store.storeId ? 'var(--cognac)' : 'var(--hairline)',
                  color: selectedStore === store.storeId ? 'var(--cognac)' : 'var(--ink-700)',
                }}
              >
                {store.storeName} · {store.city}
              </button>
            ))}
          </div>
        </div>

        <h2 className="t-label mb-3" style={{ color: 'var(--graphite)' }}>
          {stores.find((s) => s.storeId === selectedStore)?.storeName} — 구역 4 · 제품 20
        </h2>
        {!entries ? (
          <p className="t-body">PROCESSING...</p>
        ) : (
          <div className="qr-sheet-grid grid grid-cols-3 gap-6">
            {entries.map((entry) => (
              <div key={entry.code} className="qr-sheet-item flex flex-col items-center gap-2 border-t pt-3" style={{ borderColor: 'var(--hairline)' }}>
                <img src={entry.dataUrl} alt={entry.code} className="qr-sheet-image h-[120px] w-[120px]" />
                <p className="t-body-s text-center">{entry.label}</p>
                <p className="t-mrz" style={{ color: 'var(--graphite)' }}>
                  {entry.code}
                </p>
              </div>
            ))}
          </div>
        )}

        <h2 className="t-label mb-3 mt-10" style={{ color: 'var(--graphite)' }}>
          국경 이전(P4) — 해외 매장 4곳 바로가기
        </h2>
        {!transferEntries ? (
          <p className="t-body">PROCESSING...</p>
        ) : (
          <div className="qr-sheet-grid grid grid-cols-3 gap-6">
            {transferEntries.map((entry) => (
              <div key={entry.code} className="qr-sheet-item flex flex-col items-center gap-2 border-t pt-3" style={{ borderColor: 'var(--hairline)' }}>
                <img src={entry.dataUrl} alt={entry.code} className="qr-sheet-image h-[120px] w-[120px]" />
                <p className="t-body-s text-center">{entry.label}</p>
                <p className="t-mrz" style={{ color: 'var(--graphite)' }}>
                  {entry.code}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
