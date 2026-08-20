import { createBrowserRouter } from 'react-router-dom'
import RootLayout from './RootLayout'
import Home from '../screens/Home'
import KitchenSink from '../screens/dev/KitchenSink'
import ScanResult from '../screens/ScanResult'
import Passport from '../screens/Passport'
import ZoneStamp from '../screens/ZoneStamp'
import Recap from '../screens/Recap'
import Transfer from '../screens/Transfer'
import Archive from '../screens/Archive'
import Talk from '../screens/Talk'
import Register from '../screens/Register'
import Advisor from '../screens/Advisor'
import Admin from '../screens/Admin'
import PersonaConsole from '../screens/PersonaConsole'
import AdminLogin from '../screens/AdminLogin'
import AdminAuthGuard from '../components/AdminAuthGuard'
import QrSheet from '../screens/dev/QrSheet'
import AdminQr from '../screens/dev/AdminQr'
import DevReset from '../screens/dev/DevReset'

// 화면이 늘어날 때마다 이 라우터에 등록한다. 라우트 매핑은 CLAUDE.md 4장 참고.
export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/s/:productId', element: <ScanResult /> },
      { path: '/passport', element: <Passport /> },
      { path: '/z/:zoneId', element: <ZoneStamp /> },
      { path: '/recap', element: <Recap /> },
      { path: '/transfer', element: <Transfer /> },
      { path: '/archive', element: <Archive /> },
      { path: '/talk/:scanId', element: <Talk /> },
      { path: '/register', element: <Register /> },
      { path: '/advisor/:grantToken', element: <Advisor /> },
      { path: '/entryadmin', element: <AdminLogin /> },
      { path: '/admin', element: <AdminAuthGuard><Admin /></AdminAuthGuard> },
      { path: '/admin/personas', element: <AdminAuthGuard><PersonaConsole /></AdminAuthGuard> },
      // /dev/* 전부 관리자 인증 필요 — 게스트는 QR로 /s/:productId, /z/:zoneId에만 진입하므로
      // 이 경로들을 만날 일이 없어야 한다.
      { path: '/dev/kitchen-sink', element: <AdminAuthGuard><KitchenSink /></AdminAuthGuard> },
      { path: '/dev/qr', element: <AdminAuthGuard><QrSheet /></AdminAuthGuard> },
      { path: '/dev/admin-qr', element: <AdminAuthGuard><AdminQr /></AdminAuthGuard> },
      { path: '/dev/reset', element: <AdminAuthGuard><DevReset /></AdminAuthGuard> },
    ],
  },
])
