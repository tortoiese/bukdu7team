import { apiRequest } from '../client'

export interface HealthData {
  status: string
  version: string
  profile: string
}

export function getHealth() {
  return apiRequest<HealthData>('/health', { skipSessionHeader: true })
}
