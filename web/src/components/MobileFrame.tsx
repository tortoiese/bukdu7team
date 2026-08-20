import type { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useDemoStore } from '../features/demo/store'
import { useKnownContactStore } from '../features/knownContact/store'
import { useT } from '../i18n'
import LanguageToggle from './LanguageToggle'

// P1~P8 공용 모바일 컨테이너. 402px 기준, 좌우 20px 여백.
// 데스크톱 뷰포트에서는 중앙 정렬해 프레임처럼 보이게 한다.
// 상단에 항상 메뉴(홈)로 돌아가는 링크 + 언어 토글을 둔다 — 화면들이 서로 고립되지 않도록
// 어느 화면에서 시작해도 전체 이동 허브(/)로 돌아갈 수 있고, 어느 화면에서든 즉시 언어를 바꿀 수 있어야 한다.
// 데모 모드일 때는 그 옆에 mono 캡션 한 줄로만 알린다 — 에러 모달을 띄우지 않는다(PROMPTS.md #11).
export default function MobileFrame({ children }: { children: ReactNode }) {
  const isDemo = useDemoStore((s) => s.isDemo)
  const knownEmail = useKnownContactStore((s) => s.email)
  const location = useLocation()
  const t = useT()
  const isHome = location.pathname === '/'

  return (
    <div className="flex min-h-screen justify-center bg-ink-900">
      <div
        className="flex min-h-screen w-full flex-col bg-ink-900"
        style={{ maxWidth: 'var(--mobile-max)', paddingLeft: 'var(--gutter)', paddingRight: 'var(--gutter)' }}
      >
        <div className="flex flex-wrap items-center justify-between gap-y-1 pt-2">
          <div className="flex flex-col gap-1">
            {!isHome ? (
              <Link to="/" className="t-label underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
                {t('nav.backToMenu')}
              </Link>
            ) : (
              <span />
            )}
            {/* 이메일로 돌아가기(/lookup)로 들어온 경우에만 표시 — 서버는 원문을 저장하지 않으므로
                이 값은 지금 이 브라우저에서 입력한 값 그대로일 뿐, 서버 조회 결과가 아니다. */}
            {knownEmail && (
              <span className="t-mrz" style={{ color: 'var(--cognac)' }} title={t('nav.myEmail')}>
                {knownEmail}
              </span>
            )}
          </div>
          <div className="flex items-center gap-3">
            {isDemo && (
              <span className="t-mrz" style={{ color: 'var(--graphite)' }}>
                DEMO MODE
              </span>
            )}
            <LanguageToggle />
          </div>
        </div>
        {children}
      </div>
    </div>
  )
}
