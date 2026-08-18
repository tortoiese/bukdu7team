// 데모 폴백 모드. ?demo=1이면 처음부터, 네트워크 실패 시에는 자동으로 켜진다(PROMPTS.md #11).
// 전 화면 공용 MobileFrame이 이 값을 읽어 상단에 mono 캡션 한 줄로만 알린다 — 에러 모달을 띄우지 않는다.
import { create } from 'zustand'

function initialDemoFromQuery(): boolean {
  if (typeof window === 'undefined') return false
  return new URLSearchParams(window.location.search).get('demo') === '1'
}

interface DemoState {
  isDemo: boolean
  enableDemo: () => void
}

export const useDemoStore = create<DemoState>((set) => ({
  isDemo: initialDemoFromQuery(),
  enableDemo: () => set({ isDemo: true }),
}))
