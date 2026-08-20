// 화면 문구는 전부 여기서 읽는다. JSX에 한국어를 직접 박지 않는다(CLAUDE.md 6장).
// locale은 세션 store에서 온다. ko/en/zh-Hant/ja 4개 로케일을 모두 지원한다(CLAUDE.md 5장 locale enum).
import ko from './ko.json'
import en from './en.json'
import zhHant from './zh-Hant.json'
import ja from './ja.json'
import { useSessionStore } from '../features/session/store'

type Dict = Record<string, string>

const dicts: Record<string, Dict> = { ko, en, 'zh-Hant': zhHant, ja }

function interpolate(template: string, params?: Record<string, string | number>): string {
  if (!params) return template
  return template.replace(/\{(\w+)\}/g, (match, name) => (name in params ? String(params[name]) : match))
}

export function translate(locale: string, key: string, params?: Record<string, string | number>): string {
  const dict = dicts[locale] ?? dicts.en
  const template = dict[key] ?? dicts.ko[key] ?? key
  return interpolate(template, params)
}

export function useT() {
  const locale = useSessionStore((s) => s.locale)
  return (key: string, params?: Record<string, string | number>) => translate(locale, key, params)
}
