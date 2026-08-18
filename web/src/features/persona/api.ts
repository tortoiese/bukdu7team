import { apiRequest } from '../client'
import type { Persona, PersonaSimulationResult } from '../../types/api'

export function getPersonas() {
  return apiRequest<Persona[]>('/personas')
}

export interface SimulateBody {
  hypothesis: string
  variantA: string
  variantB: string
  productId: string
}

export function simulatePersona(personaId: string, body: SimulateBody) {
  return apiRequest<PersonaSimulationResult>(`/personas/${personaId}/simulate`, { method: 'POST', body })
}
