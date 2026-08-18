import { create } from 'zustand'

interface MrzState {
  lines: [string, string]
  accessibleLabel: string
  visible: boolean
  scrambleDurationMs?: number
  set: (lines: [string, string], accessibleLabel: string, scrambleDurationMs?: number) => void
  hide: () => void
}

const DEFAULT_LINES: [string, string] = ['ENTRY<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<', 'SAVED00<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<']

// 화면마다 각자 밴드를 그리지 않고, 이 store를 통해 하나의 하단 고정 MrzBar를 공유한다.
// scrambleDurationMs는 다음 값 전환 1회에만 적용되는 속도 힌트다(기본은 MrzBar의 240ms).
export const useMrzStore = create<MrzState>((set) => ({
  lines: DEFAULT_LINES,
  accessibleLabel: '',
  visible: false,
  scrambleDurationMs: undefined,
  set: (lines, accessibleLabel, scrambleDurationMs) => set({ lines, accessibleLabel, visible: true, scrambleDurationMs }),
  hide: () => set({ visible: false }),
}))
