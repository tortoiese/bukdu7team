import { useEffect } from 'react'

interface ToastProps {
  lines: [string, string?]
  onDismiss: () => void
}

// MRZ 밴드 바로 위. mono 2행, 2.4초 후 자동으로 사라진다.
export default function Toast({ lines, onDismiss }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 2400)
    return () => clearTimeout(timer)
  }, [onDismiss])

  return (
    <div
      role="status"
      className="fixed inset-x-0 z-50 mx-auto w-full px-[20px]"
      style={{ maxWidth: 'var(--mobile-max)', bottom: '58px' }}
    >
      <div className="t-mrz px-3 py-2" style={{ background: 'var(--ink-700)', color: 'var(--bone-050)' }}>
        <div>{lines[0]}</div>
        {lines[1] && <div>{lines[1]}</div>}
      </div>
    </div>
  )
}
