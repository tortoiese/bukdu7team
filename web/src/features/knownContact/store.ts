// "이메일로 돌아가기"(/lookup)로 들어왔을 때, 방금 입력한 이메일을 화면 상단에 계속 보여주기 위한
// 순수 클라이언트 상태다. 서버는 이메일 원문을 저장하지 않으므로(CLAUDE.md R8, 해시만 보관) 이 값은
// 이 브라우저에만 남고, 세션과 별개로 로그아웃(clear)하면 사라진다.
import { create } from 'zustand'

const STORAGE_KEY = 'entry.knownEmail'

interface KnownContactState {
  email: string | null
  setEmail: (email: string) => void
  clear: () => void
}

export const useKnownContactStore = create<KnownContactState>((set) => ({
  email: localStorage.getItem(STORAGE_KEY),

  setEmail(email) {
    localStorage.setItem(STORAGE_KEY, email)
    set({ email })
  },

  clear() {
    localStorage.removeItem(STORAGE_KEY)
    set({ email: null })
  },
}))
