import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import LoginPage from './LoginPage'
import RegistrationPage from './RegistrationPage'
import './styles.css'

const Page = window.location.pathname === '/login' ? LoginPage : RegistrationPage

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Page />
  </StrictMode>,
)