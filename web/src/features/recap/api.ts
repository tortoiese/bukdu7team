import { apiRequest } from '../client'
import type { RecapData } from '../../types/api'

export function getRecap() {
  return apiRequest<RecapData>('/recap')
}

export function linkAccount(channel: 'EMAIL' | 'PHONE', value: string, consent: boolean) {
  return apiRequest<{ linked: boolean; emailSent: boolean }>('/recap/link', { method: 'POST', body: { channel, value, consent } })
}
