import { apiRequest } from '../client'
import type { ArchiveList, Market } from '../../types/api'

export function saveToArchive(productId: string, scanId?: string) {
  return apiRequest<{ savedCount: number }>('/archive', { method: 'POST', body: { productId, scanId } })
}

export function removeFromArchive(productId: string) {
  return apiRequest<{ savedCount: number }>(`/archive/${productId}`, { method: 'DELETE' })
}

export function getArchive(market: Market) {
  return apiRequest<ArchiveList>(`/archive?market=${market}`)
}
