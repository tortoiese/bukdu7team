import type { ReactNode } from 'react'
import { useDemoStore } from '../features/demo/store'

// P1~P8 공용 모바일 컨테이너. 402px 기준, 좌우 20px 여백.
// 데스크톱 뷰포트에서는 중앙 정렬해 프레임처럼 보이게 한다.
// 데모 모드일 때는 상단에 mono 캡션 한 줄로만 알린다 — 에러 모달을 띄우지 않는다(PROMPTS.md #11).
export default function MobileFrame({ children }: { children: ReactNode }) {
  const isDemo = useDemoStore((s) => s.isDemo)

  return (
    <div className="flex min-h-screen justify-center bg-bone-100">
      <div
        className="flex min-h-screen w-full flex-col bg-bone-050"
        style={{ maxWidth: 'var(--mobile-max)', paddingLeft: 'var(--gutter)', paddingRight: 'var(--gutter)' }}
      >
        {isDemo && (
          <p className="t-mrz pt-2" style={{ color: 'var(--graphite)' }}>
            DEMO MODE
          </p>
        )}
        {children}
      </div>
    </div>
  )
}
