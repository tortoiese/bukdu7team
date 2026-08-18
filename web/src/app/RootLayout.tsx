import { Outlet } from 'react-router-dom'
import MrzBar from '../components/MrzBar'
import { useMrzStore } from '../features/mrz/store'

// 화면 전환에도 하나의 MrzBar 인스턴스만 유지해 스크램블 상태가 자연스럽게 이어지게 한다.
export default function RootLayout() {
  const { lines, accessibleLabel, visible, scrambleDurationMs } = useMrzStore()

  return (
    <>
      <Outlet />
      {visible && <MrzBar lines={lines} accessibleLabel={accessibleLabel} scrambleDurationMs={scrambleDurationMs} />}
    </>
  )
}
