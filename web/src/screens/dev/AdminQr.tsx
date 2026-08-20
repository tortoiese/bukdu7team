import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import QRCode from 'qrcode'

// 운영자 전용 QR. 게스트용 QR 인쇄 시트(/dev/qr)와 분리하고, Home.tsx 메뉴 어디에도 링크하지 않는다.
// /entryadmin(비밀번호 진입)으로만 연결한다 — ORIGIN은 배포 URL로 자동 반영된다.
export default function AdminQr() {
  const [dataUrl, setDataUrl] = useState<string | null>(null)
  const url = `${window.location.origin}/entryadmin`

  useEffect(() => {
    void QRCode.toDataURL(url, { margin: 1, width: 320 }).then(setDataUrl)
  }, [url])

  return (
    <div className="theme-light min-h-screen bg-bone-050 px-8 py-8">
      <div className="mx-auto flex max-w-[420px] flex-col items-center gap-4 text-center">
        <Link to="/" className="t-label self-start underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
          메뉴로
        </Link>
        <h1 className="t-display-m">관리자 전용 QR</h1>
        <p className="t-body-s" style={{ color: 'var(--graphite)' }}>
          게스트 QR 시트(/dev/qr)와 별개다. 직원 단말에서만 스캔한다.
        </p>
        {dataUrl && <img src={dataUrl} alt="admin QR" className="h-[320px] w-[320px]" />}
        <p className="t-mrz" style={{ color: 'var(--graphite)' }}>
          {url}
        </p>
      </div>
    </div>
  )
}
