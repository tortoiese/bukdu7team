import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import CharacterBubble from '../components/CharacterBubble'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { sendConversationMessage, startConversation } from '../features/conversation/api'
import brand from '../brand/mcm.json'
import type { ConversationMessage } from '../types/api'

export default function Talk() {
  const { scanId } = useParams<{ scanId: string }>()
  const ready = useSessionStore((state) => state.ready)
  const t = useT()
  const [conversationId, setConversationId] = useState<string | null>(null)
  const [messages, setMessages] = useState<ConversationMessage[]>([])
  const [turnsRemaining, setTurnsRemaining] = useState(3)
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState(false)
  const [handoffSuggested, setHandoffSuggested] = useState(false)

  useEffect(() => {
    if (!ready || !scanId) return
    let cancelled = false
    startConversation(scanId)
      .then((conversation) => {
        if (cancelled) return
        setConversationId(conversation.conversationId)
        setMessages(conversation.messages)
        setTurnsRemaining(conversation.turnsRemaining)
        setHandoffSuggested(conversation.turnsRemaining === 0)
      })
      .catch(() => {
        if (!cancelled) setError(true)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [ready, scanId])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmed = text.trim()
    if (!conversationId || !trimmed || sending || turnsRemaining === 0) return

    setSending(true)
    setError(false)
    try {
      const response = await sendConversationMessage(conversationId, trimmed)
      setMessages((current) => [
        ...current,
        { role: 'USER', text: trimmed },
        { role: 'CHARACTER', character: response.reply.character, text: response.reply.message },
      ])
      setTurnsRemaining(response.turnsRemaining)
      setHandoffSuggested(response.handoffSuggested)
      setText('')
    } catch {
      setError(true)
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  if (!conversationId) {
    return (
      <MobileFrame>
        <div className="flex flex-1 flex-col items-center justify-center gap-4 text-center">
          <p className="t-body">{t('p6.loadFail')}</p>
          <Button variant="secondary" onClick={() => window.location.reload()}>
            {t('common.retry')}
          </Button>
          <Link to="/" className="t-label underline underline-offset-4">
            {t('p6.backHome')}
          </Link>
        </div>
      </MobileFrame>
    )
  }

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-5 pb-[64px] pt-8">
        <header className="border-b border-ink-700 pb-4">
          <p className="t-label" style={{ color: 'var(--graphite)' }}>
            {t('p6.eyebrow')}
          </p>
          <div className="mt-1 flex items-end justify-between gap-4">
            <h1 className="t-display-m">{t('p6.title')}</h1>
            <span className="t-label whitespace-nowrap">
              {t('p6.turnsRemaining', { count: turnsRemaining })}
            </span>
          </div>
        </header>

        <div className="flex flex-1 flex-col gap-4" aria-live="polite">
          {messages.map((message, index) => {
            if (message.role === 'USER') {
              return (
                <div key={`${message.role}-${index}`} className="flex justify-end pl-12">
                  <p className="t-body border border-ink-700 px-3 py-2">{message.text}</p>
                </div>
              )
            }
            const character = message.character ?? 'HARU'
            return (
              <CharacterBubble
                key={`${message.role}-${index}`}
                name={brand.characters[character].name}
                avatarUrl={brand.characters[character].avatar}
                message={message.text}
              />
            )
          })}
        </div>

        {handoffSuggested && (
          <div className="border border-ink-700 p-4">
            <p className="t-label">{t('p6.handoffTitle')}</p>
            <p className="t-body mt-2">{t('p6.handoffBody')}</p>
          </div>
        )}

        {error && <p className="t-body" role="alert">{t('p6.sendFail')}</p>}

        <form className="flex flex-col gap-3" onSubmit={(event) => void handleSubmit(event)}>
          <label className="t-label" htmlFor="conversation-message">
            {t('p6.inputLabel')}
          </label>
          <textarea
            id="conversation-message"
            value={text}
            maxLength={500}
            rows={3}
            disabled={sending || turnsRemaining === 0}
            placeholder={t('p6.inputPlaceholder')}
            onChange={(event) => setText(event.target.value)}
            className="w-full resize-none border border-ink-700 bg-transparent p-3 t-body outline-none disabled:opacity-40"
          />
          <Button type="submit" disabled={!text.trim() || sending || turnsRemaining === 0}>
            {sending ? t('p6.sending') : t('p6.send')}
          </Button>
        </form>
      </div>
    </MobileFrame>
  )
}
