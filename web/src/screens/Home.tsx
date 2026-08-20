import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getHealth, type HealthData } from '../features/system/api'
import { useT } from '../i18n'
import MobileFrame from '../components/MobileFrame'
import HairlineSection from '../components/HairlineSection'

type Status = 'checking' | 'ok' | 'fail'

interface NavItem {
  label: string
  path: string
  caption?: string
}

// 팀 내부 이동/테스트용 메뉴 허브다. 실제 고객은 QR 딥링크(/s/:id, /z/:id 등)로 바로
// 들어오므로 이 화면은 제품 플로우가 아니다 — /dev/* 화면과 같은 성격이라 i18n을 강제하지 않는다.
const CUSTOMER_SCREENS: NavItem[] = [
  { label: 'P1 · 스캔 결과', path: '/s/SKY-STREAM-W260', caption: '/s/:productId' },
  { label: 'P2 · 팝업 패스포트', path: '/passport' },
  { label: 'P3 · 방문 리캡', path: '/recap' },
  { label: 'P4 · 국경 이전 (클라이맥스)', path: '/transfer?market=HK' },
  { label: 'P5 · 아카이브', path: '/archive' },
  { label: 'P8 · 사전 등록', path: '/register' },
]

// D1/D2, /dev/* 전부 이 메뉴에 올리지 않는다 — /entryadmin 비밀번호 진입 후 D1(Admin.tsx)에서만
// 접근한다. 이 화면은 게스트가 QR 없이 루트로 들어왔을 때 보는 순수 고객 화면 메뉴다.

function NavList({ items }: { items: NavItem[] }) {
  return (
    <ul className="flex flex-col">
      {items.map((item) => (
        <li key={item.path} style={{ borderBottom: '1px solid var(--hairline)' }}>
          <Link to={item.path} className="flex items-center justify-between py-3">
            <span className="t-body">{item.label}</span>
            {item.caption && (
              <span className="t-label" style={{ color: 'var(--graphite)' }}>
                {item.caption}
              </span>
            )}
          </Link>
        </li>
      ))}
    </ul>
  )
}

export default function Home() {
  const t = useT()
  const [status, setStatus] = useState<Status>('checking')
  const [health, setHealth] = useState<HealthData | null>(null)

  useEffect(() => {
    getHealth()
      .then((data) => {
        setHealth(data)
        setStatus('ok')
      })
      .catch(() => setStatus('fail'))
  }, [])

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-6 pb-[64px] pt-6">
        <div>
          <h1 className="t-display-l">{t('common.health.title')}</h1>
          <p className="t-label mt-1" style={{ color: 'var(--graphite)' }}>
            {status === 'checking' && t('common.health.checking')}
            {status === 'ok' && t('common.health.ok')}
            {status === 'fail' && t('common.health.fail')}
            {health && ` · ${health.profile}`}
          </p>
        </div>

        <HairlineSection title="고객 화면 (P1~P8)">
          <NavList items={CUSTOMER_SCREENS} />
        </HairlineSection>
      </div>
    </MobileFrame>
  )
}
