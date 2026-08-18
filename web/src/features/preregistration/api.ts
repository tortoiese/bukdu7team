import { apiRequest } from '../client'
import type { Market, PreregistrationResponse } from '../../types/api'

export interface PreregistrationBody {
  channel: 'EMAIL' | 'PHONE'
  value: string
  interestedLines: string[]
  market: Market
  consent: boolean
}

export function register(body: PreregistrationBody) {
  return apiRequest<PreregistrationResponse>('/preregistrations', { method: 'POST', body })
}
