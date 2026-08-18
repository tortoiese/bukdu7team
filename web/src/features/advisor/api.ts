import { apiRequest } from '../client'
import type { AdvisorBriefing, AdvisorConsentResponse } from '../../types/api'

export function issueAdvisorConsent(scope: string[], ttlSeconds: number) {
  return apiRequest<AdvisorConsentResponse>('/consents/advisor', { method: 'POST', body: { scope, ttlSeconds } })
}

export function getAdvisorBriefing(grantToken: string) {
  return apiRequest<AdvisorBriefing>(`/advisor/${grantToken}`)
}

export function addAdvisorNote(grantToken: string, note: string) {
  return apiRequest<{ saved: boolean }>(`/advisor/${grantToken}/notes`, { method: 'POST', body: { note } })
}
