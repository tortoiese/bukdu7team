import { motion, useReducedMotion } from 'motion/react'

interface StampProps {
  label: string
  rotationSeed: number
  size?: number
}

// 구역 방문·저장 완료 시 찍히는 검인 도장. rotationSeed로 각도를 고정해 리렌더마다 바뀌지 않게 한다.
export default function Stamp({ label, rotationSeed, size = 88 }: StampProps) {
  const rotation = (rotationSeed % 100) / 100 * 14 - 7 // -7deg ~ +7deg, 세션 내 고정
  const reduceMotion = useReducedMotion()

  return (
    <motion.div
      initial={reduceMotion ? false : { scale: 1.6, opacity: 0, filter: 'blur(2px)' }}
      animate={{ scale: 1, opacity: 0.85, filter: 'blur(0px)' }}
      transition={reduceMotion ? { duration: 0 } : { duration: 0.18, ease: [0.2, 0.9, 0.2, 1] }}
      style={{ rotate: rotation, mixBlendMode: 'multiply', width: size, height: size }}
      className="flex items-center justify-center"
    >
      <svg width={size} height={size} viewBox="0 0 88 88" role="img" aria-label={label}>
        <circle cx="44" cy="44" r="40" fill="none" stroke="var(--stamp)" strokeWidth="2.5" />
        <circle cx="44" cy="44" r="33" fill="none" stroke="var(--stamp)" strokeWidth="1" />
        <text
          x="44"
          y="49"
          textAnchor="middle"
          fill="var(--stamp)"
          fontFamily="var(--font-mono)"
          fontSize="11"
          letterSpacing="0.08em"
        >
          {label}
        </text>
      </svg>
    </motion.div>
  )
}
