// ?demo=1 또는 네트워크 실패 시 API 호출 없이 쓰는 고정 데이터(PROMPTS.md #11).
import type { ProductData, ScanResponse } from '../../types/api'

export const DEMO_PRODUCT: ProductData = {
  productId: 'SKY-STREAM-W260',
  line: 'Sky Stream',
  displayName: '스카이 스트림 백팩 화이트 260',
  material: '코티드 캔버스',
  weightGram: 780,
  sizeLabel: { local: '26cm', origin: '260' },
  craftNotes: [
    { heading: '실루엣 의도', body: '이동 중 무게중심이 흔들리지 않도록 상단 폭을 좁히고 하단을 넓힌 사다리꼴 구조로 설계했습니다.' },
    { heading: '소재 선택', body: '표면에 코팅을 더해 오염에 강하고, 젖은 천으로 닦아내는 방식으로 관리할 수 있습니다.' },
  ],
  media: [],
  stock: {
    thisStore: 'IN_STOCK',
    domesticOther: [{ storeId: 'KR-HDS', storeName: '현대 무역센터', status: 'IN_STOCK' }],
    homeMarket: { market: 'HK', status: 'IN_STOCK', storeName: '코즈웨이베이' },
  },
  priceDisplay: null,
}

export const DEMO_SCAN: ScanResponse = {
  scanId: 'demo-scan-1',
  scanCountForProduct: 1,
  sessionScanCount: 1,
  intentSignal: {
    stage: 'BROWSING',
    comparisonAxis: 'NONE',
    unresolved: 'UNKNOWN',
    confidence: 0.5,
    rationale: '이 제품을 처음 살펴보고 있습니다.',
    aiUsed: false,
  },
  greeting: { character: 'HENRY', message: '스카이 스트림 백팩입니다. 어떤 점이 궁금하신가요?' },
}
