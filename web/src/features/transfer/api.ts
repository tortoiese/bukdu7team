import { apiRequest } from '../client'
import type { Market, TransferData } from '../../types/api'

export function getTransfer(market: Market) {
  return apiRequest<TransferData>(`/transfer?market=${market}`)
}
