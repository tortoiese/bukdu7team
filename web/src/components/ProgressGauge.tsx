// 진행 게이지. 높이 2px, 트랙 hairline, 채움 cognac.
export default function ProgressGauge({ progress }: { progress: number }) {
  const pct = Math.max(0, Math.min(1, progress)) * 100
  return (
    <div
      role="progressbar"
      aria-valuenow={Math.round(pct)}
      aria-valuemin={0}
      aria-valuemax={100}
      style={{ height: 2, background: 'var(--hairline)' }}
    >
      <div style={{ height: '100%', width: `${pct}%`, background: 'var(--cognac)', transition: 'width 240ms ease' }} />
    </div>
  )
}
