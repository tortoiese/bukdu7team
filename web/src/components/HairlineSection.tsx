import type { ReactNode } from 'react'

interface HairlineSectionProps {
  title?: string
  children: ReactNode
  className?: string
}

// 카드/그림자 없이 1px 괘선으로만 섹션을 구분한다.
export default function HairlineSection({ title, children, className = '' }: HairlineSectionProps) {
  return (
    <section
      className={`py-4 ${className}`}
      style={{ borderTop: '1px solid var(--hairline)' }}
    >
      {title && (
        <h3 className="t-label mb-2" style={{ color: 'var(--graphite)' }}>
          {title}
        </h3>
      )}
      {children}
    </section>
  )
}
