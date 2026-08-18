// MRZ 밴드용 44자 고정폭 포맷터. docs/DESIGN_SYSTEM.md 5.1 예시 형식을 따른다.
// 세그먼트는 모두 실제 코드값(제품ID, 구역ID, 시장코드 등)만 넣는다 — 장식용 난수 금지.
const LINE_LENGTH = 44

function sanitizeSegment(value: string): string {
  return value
    .toString()
    .toUpperCase()
    .replace(/\s+/g, '')
    .replace(/[^A-Z0-9<]/g, '')
}

export function buildMrzLine(...segments: string[]): string {
  const joined = segments.filter(Boolean).map(sanitizeSegment).join('<<')
  return joined.padEnd(LINE_LENGTH, '<').slice(0, LINE_LENGTH)
}
