import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import QRCode from 'qrcode'
import { getProductList } from '../../features/products/api'
import brand from '../../brand/mcm.json'
import type { ProductSummary } from '../../types/api'

interface QrEntry {
  code: string
  label: string
  url: string
  dataUrl: string
}

const PRINT_STYLE = `
@media print {
  @page { size: A4; margin: 12mm; }
  .qr-sheet-grid { grid-template-columns: repeat(4, 1fr) !important; }
  .qr-sheet-item { break-inside: avoid; }
  .qr-sheet-image { width: 40mm !important; height: 40mm !important; }
}
`

// 발표 시연용 QR 인쇄 시트. 제품 20종은 /s/{productId}, 구역 4개는 /z/{zoneId}로 인코딩한다.
// ORIGIN은 window.location.origin이라 배포 URL로 자동 반영된다(PROMPTS.md #11).
export default function QrSheet() {
  const [entries, setEntries] = useState<QrEntry[] | null>(null)

  useEffect(() => {
    const origin = window.location.origin

    async function build() {
      const products = await getProductList()
      const zoneEntries = Object.entries(brand.zones).map(([zoneId, name]) => ({
        code: zoneId,
        label: name,
        url: `${origin}/z/${zoneId}`,
      }))
      const productEntries = products.map((p: ProductSummary) => ({
        code: p.productId,
        label: p.displayName,
        url: `${origin}/s/${p.productId}`,
      }))

      const all = [...zoneEntries, ...productEntries]
      const withQr = await Promise.all(
        all.map(async (item) => ({ ...item, dataUrl: await QRCode.toDataURL(item.url, { margin: 1, width: 240 }) })),
      )
      setEntries(withQr)
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
      </div>
    </div>
  )
}
