import { apiRequest } from '../client'
import type { IntentDashboardData } from '../../types/api'

export function getIntentDashboard() {
  return apiRequest<IntentDashboardData>('/admin/intent-dashboard')
}
