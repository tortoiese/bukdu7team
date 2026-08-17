import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import '@fontsource/archivo/400.css'
import '@fontsource/archivo/500.css'
import '@fontsource/archivo/600.css'
import '@fontsource/archivo/700.css'
import '@fontsource/courier-prime/400.css'
import '@fontsource/courier-prime/700.css'
import '@fontsource/noto-sans-kr/400.css'
import '@fontsource/noto-sans-kr/500.css'
import '@fontsource/noto-sans-kr/700.css'
import './styles/global.css'
import { router } from './app/router'
import SessionBoot from './app/SessionBoot'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SessionBoot>
      <RouterProvider router={router} />
    </SessionBoot>
  </StrictMode>,
)
