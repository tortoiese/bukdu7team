import { createBrowserRouter } from 'react-router-dom'
import RootLayout from './RootLayout'
import Home from '../screens/Home'
import KitchenSink from '../screens/dev/KitchenSink'

// 화면이 늘어날 때마다 이 라우터에 등록한다. 라우트 매핑은 CLAUDE.md 4장 참고.
export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/dev/kitchen-sink', element: <KitchenSink /> },
    ],
  },
])
