import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { stampZone } from '../features/passport/api'
import { ApiError } from '../features/client'

// 구역 QR 진입점(/z/:zoneId). 검인 후 /passport로 이동해 스탬프 모션을 재생한다.
export default function ZoneStamp() {
  const { zoneId } = useParams<{ zoneId: string }>()
  const navigate = useNavigate()
  const sessionReady = useSessionStore((s) => s.ready)
  const t = useT()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!sessionReady || !zoneId) return
    stampZone(zoneId)
      .then((res) => {
        navigate('/passport', { replace: true, state: { tierUnlocked: res.tierUnlocked } })
      })
      .catch((err: unknown) => {
        if (err instanceof ApiError) {
          if (err.code === 'ZONE_ALREADY_STAMPED') {
            navigate('/passport', { replace: true, state: { toastKey: 'p2.stampAlready' } })
            return
          }
          if (err.code === 'STAMP_TOO_SOON') {
            navigate('/passport', { replace: true, state: { toastKey: 'p2.stampTooSoon' } })
            return
          }
          if (err.code === 'PASSPORT_NOT_FOUND') {
            navigate('/passport', { replace: true })
            return
          }
        }
        setError(t('p2.stampFailTitle'))
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionReady, zoneId])

  if (error) {
    return (
      <MobileFrame>
        <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
          <p className="t-body">{error}</p>
        </div>
      </MobileFrame>
    )
  }

  return (
    <MobileFrame>
      <div className="flex flex-1 items-center justify-center">
        <Loading label={t('p2.stamping')} />
      </div>
    </MobileFrame>
  )
}
