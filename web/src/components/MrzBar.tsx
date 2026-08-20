import { useState } from 'react'
import { useScramble } from './useScramble'

interface MrzBarProps {
  lines: [string, string]
  accessibleLabel: string
  scrambleDurationMs?: number
}

// 하단 고정 여권 기계판독영역 밴드. 값이 바뀌면 글자 단위로 스크램블 후 확정된다(기본 240ms).
// scrambleDurationMs로 느리게 조정할 수 있다 — P4 국경 이전 MKT<KR→MKT<HK 전환(1.2초) 전용.
// 장식용이므로 스크린리더에는 aria-hidden 처리하고, 같은 정보를 accessibleLabel로 별도 제공한다.
export default function MrzBar({ lines, accessibleLabel, scrambleDurationMs }: MrzBarProps) {
  const [collapsed, setCollapsed] = useState(false)
  const line1 = useScramble(lines[0], scrambleDurationMs)
  const line2 = useScramble(lines[1], scrambleDurationMs)

  return (
    <>
      <button
        type="button"
        onClick={() => setCollapsed((v) => !v)}
        aria-hidden="true"
        tabIndex={-1}
        className="fixed inset-x-0 bottom-0 z-40 mx-auto w-full overflow-hidden text-left"
        style={{
          maxWidth: 'var(--mobile-max)',
          background: 'var(--ink-900)',
          color: 'var(--bone-050)',
          borderTop: '1px solid var(--hairline)',
          height: collapsed ? '10px' : '48px',
          transition: 'height 160ms ease',
        }}
      >
        <div className="t-mrz px-[20px] py-[8px] leading-[16px]" style={{ opacity: collapsed ? 0 : 1 }}>
          <div>{line1}</div>
          <div>{line2}</div>
        </div>
      </button>
      <span className="sr-only">{accessibleLabel}</span>
    </>
  )
}
