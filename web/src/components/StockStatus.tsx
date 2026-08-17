import type { StockStatusCode } from '../types/api'

const COLOR: Record<StockStatusCode, string> = {
  IN_STOCK: 'var(--stamp)',
  TRANSFERABLE: 'var(--cognac)',
  ONLINE_ONLY: 'var(--cognac)',
  OUT_OF_STOCK: 'var(--seal)',
}

interface StockStatusProps {
  status: StockStatusCode
  label: string
  caption?: string
}

// 좌측 4px 컬러 바 + 텍스트. IN_STOCK/TRANSFERABLE/ONLINE_ONLY/OUT_OF_STOCK.
export default function StockStatus({ status, label, caption }: StockStatusProps) {
  return (
    <div className="flex items-stretch gap-3">
      <span aria-hidden="true" style={{ width: 4, background: COLOR[status] }} />
      <div className="flex flex-col gap-0.5 py-0.5">
        <span className="t-body" style={{ color: 'var(--ink-700)' }}>
          {label}
        </span>
        {caption && (
          <span className="t-body-s" style={{ color: 'var(--graphite)' }}>
            {caption}
          </span>
        )}
      </div>
    </div>
  )
}
