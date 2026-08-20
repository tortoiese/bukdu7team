import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Button from '../../components/Button'
import { useSessionStore } from '../../features/session/store'
import { postScan } from '../../features/scan/api'
import { issuePassport, stampZone } from '../../features/passport/api'
import brand from '../../brand/mcm.json'

const HK_SCAN_SEQUENCE = ['SKY-STREAM-W260', 'SKY-STREAM-B260', 'SKY-STREAM-W300']

// 발표 리허설용. 세션을 초기화하거나 미리 정한 시나리오 상태로 빠르게 되돌린다(PROMPTS.md #11).
export default function DevReset() {
  const navigate = useNavigate()
  const reissue = useSessionStore((s) => s.reissue)
  const setMarket = useSessionStore((s) => s.setMarket)
  const [busy, setBusy] = useState(false)
  const [log, setLog] = useState<string[]>([])

  function append(line: string) {
    setLog((prev) => [...prev, line])
  }

  async function resetOnly() {
    localStorage.removeItem('entry.sid')
    setLog([])
    setBusy(true)
    await reissue()
    append('세션을 초기화했습니다.')
    setBusy(false)
  }

  async function hkThreeScans() {
    setBusy(true)
    setLog([])
    localStorage.removeItem('entry.sid')
    await reissue()
    await setMarket('HK', 'zh-Hant')
    append('시장을 HK로 전환했습니다.')
    for (const productId of HK_SCAN_SEQUENCE) {
      await postScan({ productId, storeId: brand.originStore.storeId, zoneId: 'ZONE04', scannedAt: new Date().toISOString() })
      append(`${productId} 스캔 완료`)
    }
    setBusy(false)
    navigate(`/s/${HK_SCAN_SEQUENCE[HK_SCAN_SEQUENCE.length - 1]}`)
  }

  async function allZonesVisited() {
    setBusy(true)
    setLog([])
    localStorage.removeItem('entry.sid')
    await reissue()
    await issuePassport(brand.popupId, brand.originStore.storeId)
    append('패스포트를 발급했습니다.')
    for (const zoneId of Object.keys(brand.zones)) {
      try {
        await stampZone(zoneId)
        append(`${zoneId} 검인 완료`)
      } catch {
        append(`${zoneId} 검인 실패 — 어뷰징 방지 규칙(60초 간격)으로 이번 리허설에서는 건너뜁니다.`)
      }
    }
    setBusy(false)
    navigate('/passport')
  }

  async function emptyState() {
    setBusy(true)
    setLog([])
    localStorage.removeItem('entry.sid')
    await reissue()
    append('빈 상태 세션을 발급했습니다.')
    setBusy(false)
    navigate('/')
  }

  return (
    <div className="theme-light min-h-screen bg-bone-050 px-8 py-8">
      <div className="mx-auto flex max-w-[600px] flex-col gap-6">
        <Link to="/" className="t-label underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
          메뉴로
        </Link>
        <h1 className="t-display-m">DEV RESET</h1>

        <Button variant="secondary" onClick={() => void resetOnly()} disabled={busy}>
          세션 초기화만
        </Button>

        <div className="flex flex-col gap-3 border-t pt-4" style={{ borderColor: 'var(--hairline)' }}>
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            시나리오 프리셋
          </p>
          <Button variant="primary" onClick={() => void hkThreeScans()} disabled={busy}>
            홍콩 고객 3스캔 상태
          </Button>
          <Button variant="primary" onClick={() => void allZonesVisited()} disabled={busy}>
            전 구역 방문 상태
          </Button>
          <Button variant="primary" onClick={() => void emptyState()} disabled={busy}>
            빈 상태
          </Button>
        </div>

        {log.length > 0 && (
          <div className="border-t pt-4" style={{ borderColor: 'var(--hairline)' }}>
            <ul className="flex flex-col gap-1">
              {log.map((line, i) => (
                <li key={i} className="t-mrz" style={{ color: 'var(--graphite)' }}>
                  {line}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}
