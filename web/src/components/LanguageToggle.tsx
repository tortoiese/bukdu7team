import { useSessionStore } from '../features/session/store'
import type { Locale, Market } from '../types/api'

// 한/중/일/영 네이티브 표기 — 언어 이름은 자기 언어로 쓰는 게 관례라 i18n을 거치지 않는다.
// market은 locale과 함께 세션에 저장된다(CLAUDE.md 5장) — 시장이 곧 판매/재고 표기 기준이라
// 언어만 따로 떼어 바꿀 수 없고, 각 언어의 대표 시장으로 함께 전환한다.
const LANGUAGES: { market: Market; locale: Locale; label: string }[] = [
  { market: 'KR', locale: 'ko', label: '한국어' },
  { market: 'HK', locale: 'zh-Hant', label: '繁中' },
  { market: 'JP', locale: 'ja', label: '日本語' },
  { market: 'US', locale: 'en', label: 'EN' },
]

// 전 화면 상단에 상시 노출되는 언어 토글. 탭 한 번으로 즉시 그 언어 사전으로 바뀐다.
export default function LanguageToggle() {
  const locale = useSessionStore((s) => s.locale)
  const setMarket = useSessionStore((s) => s.setMarket)
  const ready = useSessionStore((s) => s.ready)

  return (
    <div className="flex items-center gap-2" role="group" aria-label="언어 선택">
      {LANGUAGES.map((lang) => {
        const active = lang.locale === locale
        return (
          <button
            key={lang.locale}
            type="button"
            disabled={!ready || active}
            onClick={() => void setMarket(lang.market, lang.locale)}
            className="t-label"
            style={{
              color: active ? 'var(--cognac)' : 'var(--graphite)',
              textDecoration: active ? 'none' : 'underline',
              textUnderlineOffset: '4px',
              opacity: !ready ? 0.4 : 1,
            }}
          >
            {lang.label}
          </button>
        )
      })}
    </div>
  )
}
