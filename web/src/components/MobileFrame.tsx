import type { ReactNode } from 'react'

// P1~P8 공용 모바일 컨테이너. 402px 기준, 좌우 20px 여백.
// 데스크톱 뷰포트에서는 중앙 정렬해 프레임처럼 보이게 한다.
export default function MobileFrame({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen justify-center bg-bone-100">
      <div
        className="flex min-h-screen w-full flex-col bg-bone-050"
        style={{ maxWidth: 'var(--mobile-max)', paddingLeft: 'var(--gutter)', paddingRight: 'var(--gutter)' }}
      >
        {children}
      </div>
    </div>
  )
}
