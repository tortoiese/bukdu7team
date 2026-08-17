interface CharacterBubbleProps {
  name: string
  avatarUrl?: string
  message: string
}

// 캐릭터 말풍선. 좌측 40px 원형 아바타 + 우측 텍스트, 꼬리 없음.
export default function CharacterBubble({ name, avatarUrl, message }: CharacterBubbleProps) {
  return (
    <div className="flex items-start gap-3">
      <div
        aria-hidden="true"
        className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-full"
        style={{ background: 'var(--bone-100)' }}
      >
        {avatarUrl ? (
          <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
        ) : (
          <span className="t-label" style={{ color: 'var(--graphite)' }}>
            {name.slice(0, 1)}
          </span>
        )}
      </div>
      <div className="flex-1 px-3 py-2" style={{ background: 'var(--bone-100)' }}>
        <p className="t-body" style={{ color: 'var(--ink-700)' }}>
          {message}
        </p>
      </div>
    </div>
  )
}
