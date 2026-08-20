import { apiRequest, ApiError } from '../client'
import type { PassportData, StampResponse } from '../../types/api'

export function getPassport() {
  return apiRequest<PassportData>('/passport')
}

export function issuePassport(popupId: string, issuedAtStore: string) {
  return apiRequest<PassportData>('/passport', { method: 'POST', body: { popupId, issuedAtStore } })
}

export function stampZone(zoneId: string, storeId?: string) {
  return apiRequest<StampResponse>('/passport/stamps', { method: 'POST', body: { zoneId, storeId } })
}

export function isNotFound(error: unknown) {
  return error instanceof ApiError && error.code === 'PASSPORT_NOT_FOUND'
}
