import { useEffect, useState } from 'react'
import { AuthContext } from './AuthContext'
import { apiFetch } from './api'

const TOKEN_KEY = 'it-praktika-access-token'
const EMAIL_KEY = 'it-praktika-user-email'
const ROLE_KEY = 'it-praktika-user-role'
const EXPIRY_KEY = 'it-praktika-token-expiry'
const LEGACY_DEMO_KEYS = ['it-praktika-demo-role', 'it-praktika-demo-email']
const ROLES = ['student', 'admin']

function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(EMAIL_KEY)
  sessionStorage.removeItem(ROLE_KEY)
  sessionStorage.removeItem(EXPIRY_KEY)
  LEGACY_DEMO_KEYS.forEach((key) => sessionStorage.removeItem(key))
}

function loadSession() {
  const token = sessionStorage.getItem(TOKEN_KEY)
  const email = sessionStorage.getItem(EMAIL_KEY)
  const role = sessionStorage.getItem(ROLE_KEY)
  const expiresAt = Number(sessionStorage.getItem(EXPIRY_KEY))

  if (!token || !email || !ROLES.includes(role) || !Number.isFinite(expiresAt)) {
    clearSession()
    return null
  }

  if (expiresAt <= Date.now()) {
    clearSession()
    return null
  }

  return { token, user: { email, role }, expiresAt }
}

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession)

  useEffect(() => {
    if (!session) {
      return undefined
    }

    const timeout = window.setTimeout(() => {
      clearSession()
      setSession(null)
    }, Math.max(session.expiresAt - Date.now(), 0))

    return () => window.clearTimeout(timeout)
  }, [session])

  async function login(email, password) {
    const result = await apiFetch('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })

    if (
      typeof result.accessToken !== 'string' ||
      !result.accessToken ||
      result.tokenType !== 'Bearer' ||
      !ROLES.includes(result.role) ||
      typeof result.email !== 'string' ||
      !result.email ||
      !Number.isFinite(result.expiresIn) ||
      result.expiresIn <= 0
    ) {
      throw new Error('Die API hat eine ungültige Login-Antwort zurückgegeben.')
    }

    const expiresAt = Date.now() + result.expiresIn * 1000
    const nextSession = {
      token: result.accessToken,
      user: { email: result.email, role: result.role },
      expiresAt,
    }

    sessionStorage.setItem(TOKEN_KEY, nextSession.token)
    sessionStorage.setItem(EMAIL_KEY, nextSession.user.email)
    sessionStorage.setItem(ROLE_KEY, nextSession.user.role)
    sessionStorage.setItem(EXPIRY_KEY, String(nextSession.expiresAt))
    setSession(nextSession)
  }

  async function register(email, password) {
    const result = await apiFetch('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })

    if (
      typeof result?.email !== 'string' ||
      !result.email ||
      result.role !== 'student'
    ) {
      throw new Error('Die API hat eine ungültige Registrierungsantwort zurückgegeben.')
    }

    return result
  }

  async function requestPasswordReset(email) {
    return apiFetch('/api/v1/auth/password-reset', {
      method: 'POST',
      body: JSON.stringify({ email }),
    })
  }

  async function confirmPasswordReset(token, password) {
    return apiFetch('/api/v1/auth/password-reset/confirm', {
      method: 'POST',
      body: JSON.stringify({ token, password }),
    })
  }

  async function deleteAccount(password) {
    if (!session?.token) {
      throw new Error('Für diese Aktion musst du angemeldet sein.')
    }
    await apiFetch('/api/v1/users/me', {
      method: 'DELETE',
      body: JSON.stringify({ password }),
      accessToken: session.token,
    })
    clearSession()
    setSession(null)
  }

  function apiRequest(path, options = {}) {
    if (!session?.token) {
      throw new Error('Für diese Anfrage musst du angemeldet sein.')
    }

    return apiFetch(path, { ...options, accessToken: session.token })
  }

  async function logout() {
    try {
      if (session?.token) {
        await apiFetch('/api/v1/auth/logout', {
          method: 'POST',
          accessToken: session.token,
        })
      }
    } finally {
      clearSession()
      setSession(null)
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user: session?.user ?? null,
        accessToken: session?.token ?? null,
        login,
        register,
        requestPasswordReset,
        confirmPasswordReset,
        deleteAccount,
        logout,
        apiRequest,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
