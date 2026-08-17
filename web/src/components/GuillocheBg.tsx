import { useId } from 'react'

// 여권 내지 미세 패턴. 이미지 대신 SVG pattern으로 생성한다. opacity 0.04 이하 고정.
export default function GuillocheBg() {
  const patternId = `guilloche-${useId()}`

  return (
    <svg
      aria-hidden="true"
      className="pointer-events-none absolute inset-0 h-full w-full"
      style={{ opacity: 0.04 }}
    >
      <defs>
        <pattern id={patternId} width="28" height="28" patternUnits="userSpaceOnUse">
          <circle cx="14" cy="14" r="12" fill="none" stroke="var(--ink-900)" strokeWidth="0.5" />
          <circle cx="0" cy="0" r="12" fill="none" stroke="var(--ink-900)" strokeWidth="0.5" />
          <circle cx="28" cy="0" r="12" fill="none" stroke="var(--ink-900)" strokeWidth="0.5" />
          <circle cx="0" cy="28" r="12" fill="none" stroke="var(--ink-900)" strokeWidth="0.5" />
          <circle cx="28" cy="28" r="12" fill="none" stroke="var(--ink-900)" strokeWidth="0.5" />
        </pattern>
      </defs>
      <rect width="100%" height="100%" fill={`url(#${patternId})`} />
    </svg>
  )
}
