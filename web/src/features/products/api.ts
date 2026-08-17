import { apiRequest } from '../client'
import type { Market, ProductData } from '../../types/api'

export function getProduct(productId: string, market: Market) {
  return apiRequest<ProductData>(`/products/${productId}?market=${market}`)
}
