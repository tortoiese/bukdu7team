import type { ButtonHTMLAttributes, ReactNode } from 'react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'text'
  children: ReactNode
}

// docs/DESIGN_SYSTEM.md 6장: 높이 52px, 전폭, radius 0, 라벨은 mono 대문자.
export default function Button({ variant = 'primary', className = '', children, ...rest }: ButtonProps) {
  const base = 'h-[52px] w-full px-4 text-[13px] tracking-[0.05em] uppercase font-mono transition-opacity'
  const styles: Record<string, string> = {
    primary: 'bg-cognac text-ink-900 disabled:opacity-40',
    secondary: 'bg-transparent border border-ink-700 text-ink-700 disabled:opacity-40',
    text: 'h-auto w-auto bg-transparent text-ink-700 underline underline-offset-4 disabled:opacity-40',
  }

  return (
    <button className={`${variant === 'text' ? '' : base} ${styles[variant]} ${className}`.trim()} {...rest}>
      {children}
    </button>
  )
}
