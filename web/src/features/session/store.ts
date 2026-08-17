// 익명 세션 상태. CLAUDE.md 5장: sessionId는 localStorage["entry.sid"]에 저장하고
// 모든 요청에 X-Entry-Session 헤더로 첨부한다. PII는 여기 어디에도 두지 않는다.
import { create } from 'zustand'
import { createSession, updateMarket } from './api'
import type { Locale, Market } from '../../types/api'

const STORAGE_KEY = 'entry.sid'

interface SessionState {
  sessionId: string | null
  market: Market
  locale: Locale
  ready: boolean
  bootstrap: () => Promise<void>
  reissue: () => Promise<void>
  setMarket: (market: Market, locale: Locale) => Promise<void>
}

function detectEntryContext() {
  return {
    acceptLanguage: navigator.language ?? 'ko-KR',
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone ?? 'Asia/Seoul',
    entryPoint: 'STORE_TAG',
  }
}

export const useSessionStore = create<SessionState>((set, get) => ({
  sessionId: null,
  market: 'KR',
  locale: 'ko',
  ready: false,

  async bootstrap() {
    const existing = localStorage.getItem(STORAGE_KEY)
    if (existing) {
      set({ sessionId: existing, ready: true })
      return
    }
    const session = await createSession(detectEntryContext())
    localStorage.setItem(STORAGE_KEY, session.sessionId)
    set({ sessionId: session.sessionId, market: session.market, locale: session.locale, ready: true })
  },

  async reissue() {
    const session = await createSession(detectEntryContext())
    localStorage.setItem(STORAGE_KEY, session.sessionId)
    set({ sessionId: session.sessionId, market: session.market, locale: session.locale, ready: true })
  },

  async setMarket(market, locale) {
    if (!get().sessionId) return
    const session = await updateMarket(market, locale)
    set({ market: session.market, locale: session.locale })
  },
}))
