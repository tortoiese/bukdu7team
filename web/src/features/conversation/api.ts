import { apiRequest } from '../client'
import type { ConversationReply, ConversationStart } from '../../types/api'

export function startConversation(scanId: string) {
  return apiRequest<ConversationStart>('/conversations', {
    method: 'POST',
    body: { scanId },
  })
}

export function sendConversationMessage(conversationId: string, text: string) {
  return apiRequest<ConversationReply>(`/conversations/${conversationId}/messages`, {
    method: 'POST',
    body: { text },
  })
}
