// 실제 브랜드 제품 사진을 쓰지 않는다(CLAUDE.md 8장) — 대신 여권 내지 도해체 라인아트로
// "이건 실물 사진이 아니라 목업"임을 분명히 하면서도 제품 카드에 시각적 무게를 준다.
// 모든 라인(Sky Stream/HHK/Denim/M PUP/Lifestyle)이 백팩·토트류라 하나의 추상 실루엣으로 통일한다.
interface ProductSilhouetteProps {
  label: string
}

export default function ProductSilhouette({ label }: ProductSilhouetteProps) {
  return (
    <div className="flex flex-col items-center gap-2">
      <svg width="140" height="140" viewBox="0 0 140 140" role="img" aria-label={label}>
        {/* 손잡이 */}
        <path d="M52 34 Q52 16 70 16 Q88 16 88 34" fill="none" stroke="var(--ink-900)" strokeWidth="1.5" />
        {/* 몸통 */}
        <rect x="30" y="34" width="80" height="86" rx="6" fill="none" stroke="var(--ink-900)" strokeWidth="1.5" />
        {/* 지퍼 라인 */}
        <path d="M30 58 H110" stroke="var(--ink-900)" strokeWidth="0.75" strokeDasharray="2 3" />
        {/* 앞주머니 */}
        <rect x="44" y="70" width="52" height="34" rx="4" fill="none" stroke="var(--stamp)" strokeWidth="1" />
        <path d="M44 84 H96" stroke="var(--stamp)" strokeWidth="0.75" />
      </svg>
      <p className="t-label" style={{ color: 'var(--graphite)' }}>
        MOCKUP
      </p>
    </div>
  )
}
