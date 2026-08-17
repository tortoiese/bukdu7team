import type { ReactNode } from 'react'

interface FieldProps {
  label: string
  value: ReactNode
}

// 단일 필드: mono 라벨 11px + Archivo 값. 값과 4px 간격.
export function Field({ label, value }: FieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <span className="t-label" style={{ color: 'var(--graphite)' }}>
        {label}
      </span>
      <span className="t-body" style={{ color: 'var(--ink-700)' }}>
        {value}
      </span>
    </div>
  )
}

// 2열 라벨/값 그리드. 라벨 열 고정폭 96px(docs/DESIGN_SYSTEM.md 4장).
export function FieldGrid({ fields }: { fields: FieldProps[] }) {
  return (
    <div className="grid gap-y-3" style={{ gridTemplateColumns: '96px 1fr' }}>
      {fields.map((f) => (
        <div className="contents" key={f.label}>
          <span className="t-label self-start pt-0.5" style={{ color: 'var(--graphite)' }}>
            {f.label}
          </span>
          <span className="t-body" style={{ color: 'var(--ink-700)' }}>
            {f.value}
          </span>
        </div>
      ))}
    </div>
  )
}
