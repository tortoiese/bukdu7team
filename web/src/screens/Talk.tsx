import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import MobileFrame from '../components/MobileFrame'
import CharacterBubble from '../components/CharacterBubble'
import HairlineSection from '../components/HairlineSection'
import Button from '../components/Button'
import Loading from '../components/Loading'
import { useT } from '../i18n'
import { useSessionStore } from '../features/session/store'
import { startConversation, sendMessage } from '../features/conversation/api'
import brand from '../brand/mcm.json'
import type { ConversationMessage } from '../types/api'

export default function Talk() {
  const { scanId } = useParams<{ scanId: string }>()
  const t = useT()
  const sessionReady = useSessionStore((s) => s.ready)

  const [conversationId, setConversationId] = useState<string | null>(null)
  const [messages, setMessages] = useState<ConversationMessage[]>([])
  const [turnsRemaining, setTurnsRemaining] = useState(3)
  const [criteria, setCriteria] = useState<string[]>([])
  const [handoff, setHandoff] = useState(false)
  const [loadError, setLoadError] = useState(false)
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const listEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!sessionReady || !scanId) return
    startConversation(scanId)
      .then((res) => {
        setConversationId(res.conversationId)
        setMessages(res.messages)
        setTurnsRemaining(res.turnsRemaining)
      })
      .catch(() => setLoadError(true))
  }, [sessionReady, scanId])

  useEffect(() => {
    listEndRef.current?.scrollIntoView({ block: 'end' })
  }, [messages])

  async function handleSend() {
    if (!conversationId || !draft.trim() || sending) return
    const text = draft.trim()
    setDraft('')
    setMessages((prev) => [...prev, { role: 'USER', text }])
    setSending(true)
    try {
      const res = await sendMessage(conversationId, text)
      const reply = res.reply
      if (reply) {
        setMessages((prev) => [...prev, { role: 'CHARACTER', character: reply.character, text: reply.message }])
      }
      setTurnsRemaining(res.turnsRemaining)
      if (res.extracted?.criteria?.length) {
        setCriteria((prev) => Array.from(new Set([...prev, ...res.extracted.criteria])))
      }
      if (res.handoffSuggested) setHandoff(true)
    } catch {
      setLoadError(true)
    } finally {
      setSending(false)
    }
  }

  if (loadError) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center text-center">
          <p className="t-body">{t('p6.loadFail')}</p>
        </div>
      </MobileFrame>
    )
  }

  if (!conversationId) {
    return (
      <MobileFrame>
        <div className="flex flex-1 items-center justify-center">
          <Loading label={t('common.loading')} />
        </div>
      </MobileFrame>
    )
  }

  const inputDisabled = handoff || turnsRemaining <= 0

  return (
    <MobileFrame>
      <div className="flex flex-1 flex-col gap-4 pb-[64px] pt-6">
        <p className="t-label" style={{ color: 'var(--graphite)' }}>
          {t('p6.turnsLeft', { count: turnsRemaining })}
        </p>

        <div className="flex flex-1 flex-col gap-3">
          {messages.map((m, i) =>
            m.role === 'CHARACTER' ? (
              <CharacterBubble key={i} name={m.character ? brand.characters[m.character].name : ''} message={m.text} />
            ) : (
              <div key={i} className="ml-auto max-w-[80%] px-3 py-2" style={{ background: 'var(--cognac)' }}>
                <p className="t-body" style={{ color: 'var(--ink-900)' }}>
                  {m.text}
                </p>
              </div>
            ),
          )}
          <div ref={listEndRef} />
        </div>

        {criteria.length > 0 && (
          <HairlineSection title={t('p6.criteriaTitle')}>
            <ul className="flex flex-col gap-1">
              {criteria.map((c) => (
                <li key={c} className="t-body-s">
                  {c}
                </li>
              ))}
            </ul>
          </HairlineSection>
        )}

        {inputDisabled ? (
          <HairlineSection>
            <div className="flex flex-col gap-3">
              <p className="t-body">{t('p6.handoffTitle')}</p>
              <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
                {t('p6.handoffBody')}
              </p>
              <Button variant="secondary">{t('p1.callStaff')}</Button>
              <Link to="/passport" className="t-label text-center underline underline-offset-4" style={{ color: 'var(--ink-700)' }}>
                {t('nav.viewPassport')}
              </Link>
            </div>
          </HairlineSection>
        ) : (
          <div className="flex gap-3">
            <input
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && void handleSend()}
              placeholder={t('p6.inputPlaceholder')}
              className="t-body h-[44px] flex-1 bg-transparent px-1"
              style={{ border: 'none', borderBottom: '1px solid var(--hairline)', color: 'var(--ink-700)' }}
            />
            <Button variant="primary" className="w-auto px-4" onClick={() => void handleSend()} disabled={sending || !draft.trim()}>
              {t('p6.send')}
            </Button>
          </div>
        )}
      </div>
    </MobileFrame>
  )
}
