import { apiRequest } from '../client'
import type { Locale, Market, SessionData } from '../../types/api'

export interface CreateSessionRequest {
  acceptLanguage: string
  timezone: string
  entryPoint: string
}

export function createSession(req: CreateSessionRequest) {
  return apiRequest<SessionData>('/sessions', { method: 'POST', body: req, skipSessionHeader: true })
}

export function updateMarket(market: Market, locale: Locale) {
  return apiRequest<SessionData>('/sessions/market', { method: 'PATCH', body: { market, locale } })
}
