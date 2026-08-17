import { useEffect, useRef, useState } from 'react'

const SCRAMBLE_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<'
const DURATION_MS = 240

function prefersReducedMotion() {
  return typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function randomChar() {
  return SCRAMBLE_CHARS[Math.floor(Math.random() * SCRAMBLE_CHARS.length)]
}

// 값이 바뀔 때 달라진 글자만 무작위로 순환하다가 왼쪽부터 순서대로 확정된다(240ms).
// prefers-reduced-motion에서는 애니메이션 없이 target을 그대로 반환한다.
export function useScramble(target: string): string {
  const reduceMotion = prefersReducedMotion()
  const [display, setDisplay] = useState(target)
  const prevTarget = useRef(target)
  const frameRef = useRef<number | undefined>(undefined)

  useEffect(() => {
    if (reduceMotion) {
      prevTarget.current = target
      return
    }
    if (target === prevTarget.current) return

    const from = prevTarget.current
    const maxLen = Math.max(from.length, target.length)
    const diffIndexes: number[] = []
    for (let i = 0; i < maxLen; i++) {
      if (from[i] !== target[i]) diffIndexes.push(i)
    }

    const start = performance.now()

    const tick = (now: number) => {
      const elapsed = now - start
      const progress = Math.min(1, elapsed / DURATION_MS)
      const lockedCount = Math.floor(diffIndexes.length * progress)

      const chars = target.split('').map((ch, i) => {
        if (from[i] === ch) return ch
        const diffPos = diffIndexes.indexOf(i)
        return diffPos < lockedCount ? ch : randomChar()
      })
      setDisplay(chars.join(''))

      if (progress < 1) {
        frameRef.current = requestAnimationFrame(tick)
      } else {
        setDisplay(target)
        prevTarget.current = target
      }
    }

    frameRef.current = requestAnimationFrame(tick)
    return () => {
      if (frameRef.current) cancelAnimationFrame(frameRef.current)
    }
  }, [target, reduceMotion])

  return reduceMotion ? target : display
}
