import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { useT } from '../i18n'

interface PassportCoverProps {
  isOpen: boolean
  onOpen: () => void
  passportNo?: string
}

// P2 진입 시 덮여 있다가 탭하면 책처럼 왼쪽 경첩을 축으로 펼쳐지며 실물 문서(Passport.tsx의
// .theme-light 패널)가 드러난다. 대한민국 여권 표지의 골드 프레임 · 중앙 원형 문장 · 워드마크
// 배치를 참고하되, 국장(무궁화 문장)은 쓰지 않고 ENTRY 고유의 추상 마크(이중 원 + 게이트 형상)로
// 대체한다 — CLAUDE.md "실제 저작물을 외부에서 가져오지 않는다"에 맞춘 원본 그래픽.
export default function PassportCover({ isOpen, onOpen, passportNo }: PassportCoverProps) {
  const t = useT()
  const reduceMotion = useReducedMotion()

  return (
    <AnimatePresence>
      {!isOpen && (
        <motion.button
          type="button"
          key="passport-cover"
          onClick={onOpen}
          aria-label={t('p2.coverOpen')}
          initial={false}
          exit={reduceMotion ? { opacity: 0 } : { rotateY: -100, opacity: 0 }}
          transition={{ duration: reduceMotion ? 0.001 : 0.65, ease: [0.4, 0, 0.2, 1] }}
          className="flex flex-col items-center justify-between"
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 45,
            maxWidth: 'var(--mobile-max)',
            margin: '0 auto',
            background: 'var(--ink-900)',
            transformOrigin: 'left center',
            transformPerspective: 1400,
            paddingTop: '20%',
            paddingBottom: '10%',
          }}
        >
          <span aria-hidden="true" style={{ position: 'absolute', inset: 18, border: '1px solid var(--cognac)' }} />

          <div className="flex flex-col items-center gap-6">
            <svg width="88" height="88" viewBox="0 0 88 88" aria-hidden="true">
              <circle cx="44" cy="44" r="40" fill="none" stroke="var(--cognac)" strokeWidth="1.5" />
              <circle cx="44" cy="44" r="33" fill="none" stroke="var(--cognac)" strokeWidth="0.75" />
              <path d="M28 55 V38 Q28 25 44 25 Q60 25 60 38 V55" fill="none" stroke="var(--cognac)" strokeWidth="2" />
            </svg>
            <div className="text-center">
              <p className="t-display-l" style={{ color: 'var(--cognac)', letterSpacing: '0.08em' }}>
                {t('common.health.title')}
              </p>
              <p className="t-label mt-2" style={{ color: 'var(--ink-700)', letterSpacing: '0.3em' }}>
                PASSPORT
              </p>
            </div>
          </div>

          <div className="flex flex-col items-center gap-4">
            <span aria-hidden="true" style={{ position: 'relative', width: 28, height: 20, border: '1px solid var(--cognac)' }}>
              <span aria-hidden="true" style={{ position: 'absolute', left: 0, right: 0, top: '50%', height: 1, background: 'var(--cognac)' }} />
            </span>
            {passportNo && (
              <p className="t-mrz" style={{ color: 'var(--graphite)' }}>
                {passportNo}
              </p>
            )}
            <motion.p
              className="t-label"
              style={{ color: 'var(--graphite)' }}
              animate={reduceMotion ? undefined : { opacity: [0.5, 1, 0.5] }}
              transition={reduceMotion ? undefined : { duration: 1.8, repeat: Infinity, ease: 'easeInOut' }}
            >
              {t('p2.coverTapHint')}
            </motion.p>
          </div>
        </motion.button>
      )}
    </AnimatePresence>
  )
}
