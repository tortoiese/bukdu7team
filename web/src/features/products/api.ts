import { apiRequest } from '../client'
import type { Market, ProductData, ProductSummary } from '../../types/api'

export function getProduct(productId: string, market: Market) {
  return apiRequest<ProductData>(`/products/${productId}?market=${market}`)
}

// /dev/qr QR 시트 전용 가벼운 목록.
export function getProductList() {
  return apiRequest<ProductSummary[]>('/products')
}
