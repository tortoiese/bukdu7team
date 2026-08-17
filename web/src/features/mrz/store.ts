import { create } from 'zustand'

interface MrzState {
  lines: [string, string]
  accessibleLabel: string
  visible: boolean
  set: (lines: [string, string], accessibleLabel: string) => void
  hide: () => void
}

const DEFAULT_LINES: [string, string] = ['ENTRY<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<', 'SAVED00<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<']

// 화면마다 각자 밴드를 그리지 않고, 이 store를 통해 하나의 하단 고정 MrzBar를 공유한다.
export const useMrzStore = create<MrzState>((set) => ({
  lines: DEFAULT_LINES,
  accessibleLabel: '',
  visible: false,
  set: (lines, accessibleLabel) => set({ lines, accessibleLabel, visible: true }),
  hide: () => set({ visible: false }),
}))
