// 화면 문구는 전부 여기서 읽는다. JSX에 한국어를 직접 박지 않는다(CLAUDE.md 6장).
// locale은 세션 store에서 온다. ko 외 로케일은 아직 en으로 폴백한다(zh-Hant/ja는 후속 프롬프트에서 채운다).
import ko from './ko.json'
import en from './en.json'
import { useSessionStore } from '../features/session/store'

type Dict = Record<string, string>

const dicts: Record<string, Dict> = { ko, en }

export function translate(locale: string, key: string): string {
  const dict = dicts[locale] ?? dicts.en
  return dict[key] ?? dicts.ko[key] ?? key
}

export function useT() {
  const locale = useSessionStore((s) => s.locale)
  const dictLocale = locale === 'ko' ? 'ko' : 'en'
  return (key: string) => translate(dictLocale, key)
}
