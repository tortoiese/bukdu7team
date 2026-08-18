// ?demo=1 또는 네트워크 실패 시 API 호출 없이 쓰는 고정 데이터(PROMPTS.md #11).
import type { TransferData } from '../../types/api'

export const DEMO_TRANSFER: TransferData = {
  originStore: 'SEOUL / SEONGSU',
  targetMarket: 'HK',
  currency: 'HKD',
  generatedAt: new Date().toISOString(),
  sendTiming: {
    recommendedAt: new Date(Date.now() + 72 * 3600 * 1000).toISOString(),
    rationale: '체류 마지막 날 이후로 판단해 72시간 뒤로 제안합니다.',
    aiUsed: false,
  },
  items: [
    {
      productId: 'SKY-STREAM-W260',
      displayName: '스카이 스트림 백팩 화이트 260',
      status: 'IN_STOCK',
      storeName: '코즈웨이베이',
      action: { type: 'RESERVE', label: '매장 방문 예약' },
    },
    {
      productId: 'PM-TOTE',
      displayName: 'M PUP 토트백',
      status: 'TRANSFERABLE',
      transferDays: 4,
      fromStore: 'KR-SEONGSU',
      action: { type: 'REQUEST_TRANSFER', label: '이동 요청' },
    },
  ],
  unresolvedAnswers: [
    {
      code: 'COLOR_CARE',
      question: '밝은 컬러 관리가 어렵지 않을까 고민이 있었습니다.',
      answer: '코팅 소재는 물티슈로 닦아내는 방식으로 관리할 수 있습니다.',
      aiUsed: false,
    },
  ],
  mrzTransition: { from: 'MKT<KR', to: 'MKT<HK' },
}
