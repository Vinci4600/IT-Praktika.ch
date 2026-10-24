import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

export default function Layout() {
  const { user, logout } = useAuth()
  const [logoutError, setLogoutError] = useState('')

  async function abmelden() {
    setLogoutError('')
    try {
      await logout()
    } catch (error) {
      setLogoutError(`Lokal abgemeldet; der Server konnte die Sitzung nicht widerrufen: ${error.message}`)
    }
  }

  return (
    <div className="page">
      <nav className="topnav">
        <NavLink to="/" className="brand" end>
          IT-Praktika
        </NavLink>
        <div className="nav-links">
          <NavLink to="/" end>
            Stellen
          </NavLink>
          <NavLink to="/initiativ">Initiativ bewerben</NavLink>
          <NavLink to="/bewerbungshelfer">Bewerbungshelfer</NavLink>
          <NavLink to="/firmenkontakt">Für Firmen</NavLink>
          <NavLink to="/schulen">Für Schulen</NavLink>
          {user ? (
            <>
              {user.role === 'admin' && (
                <NavLink to="/admin">Adminbereich</NavLink>
              )}
              <NavLink to="/konto">Konto</NavLink>
              <span className="nav-user">
                {user.role === 'admin' ? 'Admin' : 'Schüler/in'} · {user.email}
              </span>
              <button className="nav-logout" type="button" onClick={() => void abmelden()}>
                Abmelden
              </button>
            </>
          ) : (
            <NavLink to="/login">Login</NavLink>
          )}
        </div>
      </nav>
      {logoutError && <p className="login-error" role="alert">{logoutError}</p>}
      <Outlet />
    </div>
  )
}
