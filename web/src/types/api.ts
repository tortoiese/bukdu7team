// docs/API_CONTRACT.md 와 1:1로 맞춘 타입. 서버 enum과 문자열 리터럴 유니온을 일치시킨다.

export type Market = 'KR' | 'HK' | 'JP' | 'US'
export type Locale = 'ko' | 'en' | 'zh-Hant' | 'ja'

export interface ApiMeta {
  requestId: string
  aiUsed: boolean
  fallback: boolean
  sessionRotated?: boolean
}

export interface ApiResponse<T> {
  data: T
  meta: ApiMeta
}

export interface ApiErrorBody {
  error: {
    code: string
    message: string
  }
}

// ── 세션 ──────────────────────────────────────────
export interface SessionData {
  sessionId: string
  market: Market
  locale: Locale
  marketInferred: boolean
  createdAt: string
}

// ── 운영자 인증 (D1/D2, /entryadmin) ────────────────
export interface AdminLoginResponse {
  adminToken: string
  expiresAt: string
}

// ── 제품 · 스캔 (P1) ──────────────────────────────
export type StockStatusCode = 'IN_STOCK' | 'TRANSFERABLE' | 'ONLINE_ONLY' | 'OUT_OF_STOCK'

export interface CraftNote {
  heading: string
  body: string
}

export interface StoreStock {
  storeId: string
  storeName: string
  status: StockStatusCode
}

export interface ProductStock {
  thisStore: StockStatusCode
  domesticOther: StoreStock[]
  homeMarket: { market: Market; status: StockStatusCode; storeName: string }
}

export interface ProductSummary {
  productId: string
  displayName: string
  line: string
}

export interface ProductData {
  productId: string
  line: string
  displayName: string
  material: string
  weightGram: number
  sizeLabel: { local: string; origin: string }
  craftNotes: CraftNote[]
  media: string[]
  stock: ProductStock
  priceDisplay: null
}

export type IntentStage = 'BROWSING' | 'CATEGORY_COMPARE' | 'LINE_COMPARE' | 'SIZE_DECIDED' | 'READY'
export type UnresolvedCode = 'SIZE' | 'COLOR_CARE' | 'PORTABILITY' | 'CAPACITY' | 'GIFT_FIT' | 'UNKNOWN'
export type CharacterId = 'HARU' | 'HENRY' | 'KAISER'

export interface IntentSignal {
  stage: IntentStage
  comparisonAxis: string
  unresolved: UnresolvedCode
  confidence: number
  rationale: string
  aiUsed: boolean
}

export interface ScanResponse {
  scanId: string
  scanCountForProduct: number
  sessionScanCount: number
  intentSignal: IntentSignal
  greeting: { character: CharacterId; message: string }
}

// ── 아카이브 (P5) ─────────────────────────────────
export interface ArchiveItem {
  productId: string
  displayName: string
  savedAt: string
  savedAtStoreId: string
  zoneId: string
  thumbnail: string
  homeMarketStatus: StockStatusCode
}

export interface ArchiveList {
  items: ArchiveItem[]
  intentSummary: { text: string; aiUsed: boolean }
}

// ── 패스포트 (P2) ─────────────────────────────────
export interface PassportZone {
  zoneId: string
  name: string
  visited: boolean
  stampedAt?: string
  rotationSeed?: number
}

export interface PassportGrant {
  code: string
  label: string
  active: boolean
}

export interface PassportData {
  passportNo: string
  issuedAt: string
  issuedPlace: string
  popupId: string
  zones: PassportZone[]
  savedCount: number
  accessTier: number
  grants: PassportGrant[]
  mrz: [string, string]
}

export interface StampResponse {
  zoneId: string
  stampedAt: string
  rotationSeed: number
  accessTier: number
  tierUnlocked: boolean
  nextTier: { tier: number; requirement: string; remainingZones: string[] } | null
}

// ── 방문 리캡 (P3) ────────────────────────────────
export interface RecapData {
  visitDate: string
  storeName: string
  viewedProducts: { productId: string; displayName: string; scanCount: number; order: number }[]
  interestSummary: { text: string; aiUsed: boolean }
  unresolvedFactors: { code: UnresolvedCode; label: string; productId: string }[]
  accountLink: { required: boolean; reason: string }
}

// ── 국경 이전 (P4) ────────────────────────────────
export type TransferActionType = 'RESERVE' | 'REQUEST_TRANSFER' | 'ONLINE' | 'NOTIFY_RESTOCK'

export interface TransferAction {
  type: TransferActionType
  label: string
  url?: string
}

export interface TransferItem {
  productId: string
  displayName: string
  status: StockStatusCode
  storeName?: string
  transferDays?: number
  fromStore?: string
  action: TransferAction
}

export interface TransferData {
  originStore: string
  targetMarket: Market
  currency: string
  generatedAt: string
  sendTiming: { recommendedAt: string; rationale: string; aiUsed: boolean }
  items: TransferItem[]
  unresolvedAnswers: { code: UnresolvedCode; question: string; answer: string; aiUsed: boolean }[]
  mrzTransition: { from: string; to: string }
}

// ── 대화 (P6) ─────────────────────────────────────
export interface ConversationMessage {
  role: 'CHARACTER' | 'USER'
  character?: CharacterId
  text: string
}

export interface ConversationStart {
  conversationId: string
  turnsRemaining: number
  messages: ConversationMessage[]
}

export interface ConversationReply {
  reply: { character: CharacterId; message: string } | null
  turnsRemaining: number
  extracted: { criteria: string[]; unresolved: UnresolvedCode | null }
  handoffSuggested: boolean
}

// ── 어드바이저 (P7) ───────────────────────────────
export interface AdvisorConsentResponse {
  grantToken: string
  expiresAt: string
  qrPayload: string
}

export interface AdvisorBriefing {
  briefing: { text: string; aiUsed: boolean }
  savedItems: ArchiveItem[]
  unresolved: { code: UnresolvedCode; label: string; productId: string }[]
  locale: Locale
  keyPhrases: { ko: string; target: string }[]
  expiresAt: string
}

// ── 사전 등록 (P8) ────────────────────────────────
export interface PreregistrationResponse {
  slot: string
  timeWindow: string
  code: string
}

// ── 대시보드 (D1) ─────────────────────────────────
export interface IntentDashboardData {
  products: { productId: string; scans: number; rescanRate: number; saveRate: number; conversionRate: number }[]
  unresolvedDistribution: { code: UnresolvedCode; count: number }[]
  marketDistribution: { market: Market; sessions: number }[]
  transferRecovery: { sent: number; converted: number; rate: number }
  zonePerformance: { zoneId: string; avgDwellSeconds: number; saves: number }[]
  actionHints: string[]
}

// ── 페르소나봇 (D2) ───────────────────────────────
export interface Persona {
  id: string
  name: string
  description: string
}

export interface PersonaSimulationResult {
  runId: string
  results: { variant: string; saved: boolean; reason: string; unresolved: UnresolvedCode }[]
  disclaimer: string
}
