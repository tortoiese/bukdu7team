// 공통 fetch 래퍼. 세션 요청/응답 헤더 동기화, 에러 → ApiError 변환.
// 컴포넌트에서 fetch를 직접 호출하지 않는다. 반드시 이 클라이언트를 통과한다.
import type { ApiErrorBody, ApiResponse } from '../types/api'
import { useSessionStore } from './session/store'

const API_BASE = import.meta.env.VITE_API_BASE as string

export class ApiError extends Error {
  code: string
  status: number

  constructor(code: string, message: string, status: number) {
    super(message)
    this.code = code
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'DELETE'
  body?: unknown
  skipSessionHeader?: boolean
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, skipSessionHeader = false } = options

  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (!skipSessionHeader) {
    const sessionId = useSessionStore.getState().sessionId
    if (sessionId) headers['X-Entry-Session'] = sessionId
  }

  let res: Response
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError('NETWORK_ERROR', '네트워크에 연결할 수 없습니다.', 0)
  }

  const resolvedSessionId = res.headers.get('X-Entry-Session')
  if (resolvedSessionId) useSessionStore.getState().replaceSession(resolvedSessionId)

  let json: ApiResponse<T> | ApiErrorBody
  try {
    json = await res.json()
  } catch {
    throw new ApiError('PARSE_ERROR', '응답을 해석할 수 없습니다.', res.status)
  }

  if (!res.ok || 'error' in json) {
    const err = 'error' in json ? json.error : { code: 'UNKNOWN', message: '알 수 없는 오류' }
    throw new ApiError(err.code, err.message, res.status)
  }

  return json.data
}
