import { apiRequest } from '../client'
import type { ScanResponse } from '../../types/api'

export interface ScanRequestBody {
  productId: string
  storeId: string
  zoneId: string
  scannedAt: string
}

export function postScan(body: ScanRequestBody) {
  return apiRequest<ScanResponse>('/scans', { method: 'POST', body })
}
