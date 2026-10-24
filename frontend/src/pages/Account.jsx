import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function Account() {
  const { user, deleteAccount } = useAuth()
  const navigate = useNavigate()
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event) {
    event.preventDefault()
    if (!window.confirm('Konto und zugehörige Merkliste endgültig löschen?')) return
    setError('')
    setBusy(true)
    try {
      await deleteAccount(password)
      navigate('/', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="content login-content">
      <section className="detail-card login-card">
        <p className="helper-overline">Konto</p>
        <h1>{user?.email}</h1>
        <p>
          Beim Löschen werden dein Konto und deine persönlichen Merkliste-Einträge
          entfernt. Dieser Vorgang kann nicht rückgängig gemacht werden.
        </p>
        <form className="form" onSubmit={submit}>
          <label>
            Passwort zur Bestätigung
            <input
              type="password"
              required
              maxLength={72}
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {error && <p className="login-error" role="alert">{error}</p>}
          <button className="btn-secondary" type="submit" disabled={busy}>
            {busy ? 'Konto wird gelöscht …' : 'Konto endgültig löschen'}
          </button>
        </form>
      </section>
    </main>
  )
}
