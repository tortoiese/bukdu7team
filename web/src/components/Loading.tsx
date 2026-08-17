import { useEffect, useState } from 'react'

// 스피너 금지. "PROCESSING<<<"의 < 개수가 늘어나는 mono 텍스트로 로딩을 표시한다.
export default function Loading({ label = 'PROCESSING' }: { label?: string }) {
  const [count, setCount] = useState(1)

  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    const timer = setInterval(() => setCount((c) => (c % 5) + 1), 240)
    return () => clearInterval(timer)
  }, [])

  return (
    <div role="status" className="t-mrz" style={{ color: 'var(--graphite)' }}>
      {label}
      {'<'.repeat(count)}
    </div>
  )
}
