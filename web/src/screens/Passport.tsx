import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import PassportCover from '../components/PassportCover'
import GuillocheBg from '../components/GuillocheBg'
import { FieldGrid } from '../components/Field'
import HairlineSection from '../components/HairlineSection'
import Stamp from '../components/Stamp'
import ProgressGauge from '../components/ProgressGauge'
import Toast from '../components/Toast'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { useMrzStore } from '../features/mrz/store'
import { getPassport, isNotFound, issuePassport } from '../features/passport/api'
import { getArchive } from '../features/archive/api'
import brand from '../brand/mcm.json'
import type { ArchiveItem, PassportData, PassportZone } from '../types/api'

const COVER_SEEN_KEY = 'entry.passportCoverSeen'

interface ZoneCellProps {
  zone: PassportZone
}

// 방문 구역은 검인 도장, 미방문 구역은 점선 사각형 + 구역 코드만(자물쇠·물음표 금지).
function ZoneCell({ zone }: ZoneCellProps) {
  return (
    <div className="flex aspect-square flex-col items-center justify-center gap-1" style={{ border: zone.visited ? 'none' : '1px dashed var(--hairline)' }}>
      {zone.visited && zone.rotationSeed !== undefined && zone.rotationSeed !== null ? (
        <Stamp label={zone.name} rotationSeed={zone.rotationSeed} />
      ) : (
        <span className="t-label" style={{ color: 'var(--graphite)' }}>
          {zone.zoneId}
        </span>
      )}
    </div>
  )
}

export default function Passport() {
  const t = useT()
  const market = useSessionStore((s) => s.market)
  const sessionReady = useSessionStore((s) => s.ready)
  const setMrz = useMrzStore((s) => s.set)
  const location = useLocation()
  const navigate = useNavigate()

  const [passport, setPassport] = useState<PassportData | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [issuing, setIssuing] = useState(false)
  const [previewItems, setPreviewItems] = useState<ArchiveItem[]>([])
  const [toast, setToast] = useState<[string, string?] | null>(null)
  // 표지 펼치기 연출은 이번 세션에서 처음 볼 때만 보여준다. 그렇지 않으면 구역 검인/저장 직후
  // 패스포트로 넘어왔을 때 방금 반영된 내용이 닫힌 표지 뒤에 가려져 "반영이 안 됐다"로 보인다.
  const [coverOpen, setCoverOpen] = useState(() => sessionStorage.getItem(COVER_SEEN_KEY) === '1')
  const consumedTierState = useRef(false)

  function handleCoverOpen() {
    sessionStorage.setItem(COVER_SEEN_KEY, '1')
    setCoverOpen(true)
  }

  function load() {
    getPassport()
      .then((data) => {
        setPassport(data)
        setNotFound(false)
      })
      .catch((err) => {
        if (isNotFound(err)) setNotFound(true)
      })
  }

  useEffect(() => {
    if (!sessionReady) return
    load()
  }, [sessionReady])

  useEffect(() => {
    if (!sessionReady) return
    getArchive(market)
      .then((list) => setPreviewItems(list.items.slice(0, 3)))
      .catch(() => setPreviewItems([]))
  }, [sessionReady, market])

  // /z/:zoneId 에서 넘어온 결과 상태를 1회만 안내하고 지운다(새로고침 시 재노출 방지).
  useEffect(() => {
    const state = location.state as { tierUnlocked?: boolean; toastKey?: string } | null
    if ((state?.tierUnlocked || state?.toastKey) && !consumedTierState.current) {
      consumedTierState.current = true
      setToast([t(state.tierUnlocked ? 'p2.tierUnlockedToast' : state.toastKey!)])
      navigate('.', { replace: true, state: null })
    }
  }, [location.state, navigate, t])

  useEffect(() => {
    if (!passport) return
    setMrz(passport.mrz, t('p2.mrzAccessible', { passportNo: passport.passportNo, tier: passport.accessTier, savedCount: passport.savedCount }))
  }, [passport, setMrz, t])

  async function handleDevIssue() {
    setIssuing(true)
    try {
      const data = await issuePassport(brand.popupId, brand.originStore.storeId)
      setPassport(data)
      setNotFound(false)
    } finally {
      setIssuing(false)
    }
  }

  if (!sessionReady || (!passport && !notFound)) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  if (notFound || !passport) {
    return (
      <MobileFrame>
        <div className="flex flex-1 flex-col items-center justify-center gap-4 text-center">
          <p className="t-body">{t('p2.emptyTitle')}</p>
          <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
            {t('p2.emptyBody')}
          </p>
          <Button variant="secondary" onClick={() => void handleDevIssue()} disabled={issuing}>
            {t('p2.devIssue')}
          </Button>
        </div>
      </MobileFrame>
    )
  }

  const visitedCount = passport.zones.filter((z) => z.visited).length
  const remainingZones = passport.zones.filter((z) => !z.visited).length

  return (
    <MobileFrame>
      <PassportCover isOpen={coverOpen} onOpen={handleCoverOpen} passportNo={passport.passportNo} />
      {/* 패스포트 실물 문서 스코프. 다크 앱 크롬과 대비되는 밝은 "내지" 카드로 전체 화면 폭까지 번져 보이게 한다(피그마 P2 참조). */}
      <div
        aria-hidden={!coverOpen}
        className="theme-light -mx-[var(--gutter)] flex flex-1 flex-col gap-6 px-[var(--gutter)] pb-[64px] pt-6"
        style={{ background: 'var(--bone-050)' }}
      >
        <div className="relative border p-4" style={{ borderColor: 'var(--hairline)' }}>
          <GuillocheBg />
          <div className="relative">
            <FieldGrid
              fields={[
                { label: t('p2.issuedPlace'), value: passport.issuedPlace },
                { label: t('p2.issuedAt'), value: new Date(passport.issuedAt).toLocaleDateString() },
                { label: t('p2.visitedZones'), value: `${visitedCount} / ${passport.zones.length}` },
                { label: t('p2.savedCount'), value: passport.savedCount },
              ]}
            />
            <p className="t-mrz mt-4" style={{ color: 'var(--ink-700)' }}>
              {passport.passportNo}
            </p>
          </div>
        </div>

        <HairlineSection title={t('p2.entryRecord')}>
          <div className="grid grid-cols-2 gap-3">
            {passport.zones.map((zone) => (
              <ZoneCell key={zone.zoneId} zone={zone} />
            ))}
          </div>
        </HairlineSection>

        <HairlineSection title={t('p2.accessGrants')}>
          <div className="flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {t('p2.currentTier')}
              </span>
              <span className="t-body">{passport.accessTier} / 4</span>
            </div>
            <ProgressGauge progress={passport.accessTier / 4} />
            <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
              {passport.accessTier >= 4
                ? t('p2.maxTierReached')
                : passport.accessTier >= 3
                  ? t('p2.nextTierAllZones')
                  : t('p2.nextTierRemaining', { count: remainingZones })}
            </p>
            <ul className="flex flex-col gap-2">
              {passport.grants.map((grant) => (
                <li key={grant.code} className="t-body" style={{ color: grant.active ? 'var(--ink-700)' : 'var(--graphite)' }}>
                  {grant.label}
                </li>
              ))}
            </ul>
          </div>
        </HairlineSection>

        <HairlineSection title={t('p2.savedPreview')}>
          {previewItems.length === 0 ? (
            <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
              {t('empty.archive')}
            </p>
          ) : (
            <ul className="flex flex-col gap-2">
              {previewItems.map((item) => (
                <li key={item.productId} className="t-body">
                  {item.displayName}
                </li>
              ))}
            </ul>
          )}
          <Link to="/archive" className="t-label mt-3 inline-block underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('p2.viewAll')}
          </Link>
        </HairlineSection>

        <div className="flex flex-col gap-3">
          <Link to="/recap" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('nav.viewRecap')}
          </Link>
          <Link to="/transfer" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
            {t('nav.viewTransfer')}
          </Link>
        </div>
      </div>

      {toast && <Toast lines={toast} onDismiss={() => setToast(null)} />}
    </MobileFrame>
  )
}
