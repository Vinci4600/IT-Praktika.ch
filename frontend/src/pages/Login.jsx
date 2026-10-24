import { useState } from 'react'
import { Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../AuthContext'

function FieldIcon({ type }) {
  const commonProps = {
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: '1.7',
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    'aria-hidden': 'true',
  }

  if (type === 'lock') {
    return (
      <svg {...commonProps}>
        <rect x="5" y="10" width="14" height="10" rx="2" />
        <path d="M8 10V7.8A4 4 0 0 1 12 4a4 4 0 0 1 4 3.8V10" />
      </svg>
    )
  }

  return (
    <svg {...commonProps}>
      <path d="M4 7.2A2.2 2.2 0 0 1 6.2 5h11.6A2.2 2.2 0 0 1 20 7.2v9.6A2.2 2.2 0 0 1 17.8 19H6.2A2.2 2.2 0 0 1 4 16.8V7.2Z" />
      <path d="m4.8 7 7.2 5.4L19.2 7" />
    </svg>
  )
}

export default function Login() {
  const {
    user,
    login,
    register,
    requestPasswordReset,
    confirmPasswordReset,
  } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const resetToken = searchParams.get('resetToken') ?? ''
  const [email, setEmail] = useState('')
  const [passwort, setPasswort] = useState('')
  const [passwortBestaetigen, setPasswortBestaetigen] = useState('')
  const [modus, setModus] = useState(() => resetToken ? 'reset-confirm' : 'login')
  const [fehler, setFehler] = useState('')
  const [erfolg, setErfolg] = useState('')
  const [wirdAngemeldet, setWirdAngemeldet] = useState(false)
  const adminAnmeldung = location.state?.from?.pathname?.startsWith('/admin')
  const ziel = location.state?.from?.pathname || '/bewerbungshelfer'

  if (user) {
    return <Navigate to={ziel} replace />
  }

  async function anmelden(event) {
    event.preventDefault()
    setFehler('')
    setErfolg('')

    if (['register', 'reset-confirm'].includes(modus) && passwort !== passwortBestaetigen) {
      setFehler('Die Passwörter stimmen nicht überein.')
      return
    }

    setWirdAngemeldet(true)

    try {
      if (modus === 'register') {
        await register(email.trim(), passwort)
        setPasswort('')
        setPasswortBestaetigen('')
        setErfolg('Dein Schülerkonto wurde erstellt. Du kannst dich jetzt anmelden.')
        setModus('login')
      } else if (modus === 'reset-request') {
        await requestPasswordReset(email.trim())
        setErfolg('Wenn ein Konto zu dieser E-Mail-Adresse existiert, erhältst du eine Nachricht mit den nächsten Schritten.')
        setModus('login')
      } else if (modus === 'reset-confirm') {
        await confirmPasswordReset(resetToken, passwort)
        setPasswort('')
        setPasswortBestaetigen('')
        setSearchParams({})
        setErfolg('Dein Passwort wurde geändert. Du kannst dich jetzt anmelden.')
        setModus('login')
      } else {
        await login(email.trim(), passwort)
        navigate(ziel, { replace: true })
      }
    } catch (error) {
      setFehler(
        error instanceof TypeError
          ? 'Der Server ist momentan nicht erreichbar. Bitte versuche es später erneut.'
          : error.message,
      )
    } finally {
      setWirdAngemeldet(false)
    }
  }

  return (
    <>
      <header className="hero hero-compact">
        <div className="hero-inner">
          <p className="eyebrow">Willkommen zurück</p>
          <h1>
            {modus === 'register'
              ? 'Konto erstellen'
              : modus === 'reset-request'
                ? 'Passwort zurücksetzen'
                : modus === 'reset-confirm'
                  ? 'Neues Passwort'
                  : 'Anmelden'}
          </h1>
          <p className="lede">
            {modus === 'register'
              ? 'Erstelle ein Schülerkonto für den Bewerbungshelfer.'
              : modus === 'reset-request'
                ? 'Wir senden dir einen Link, falls ein Konto mit dieser E-Mail-Adresse besteht.'
                : modus === 'reset-confirm'
                  ? 'Der Link ist zeitlich begrenzt. Wähle ein neues sicheres Passwort.'
              : adminAnmeldung
                ? 'Melde dich als Admin an, um die Verwaltung zu öffnen.'
                : 'Melde dich an, um den Bewerbungshelfer zu öffnen.'}
          </p>
        </div>
      </header>

      <main className="content login-content">
        <form className="detail-card form login-card" onSubmit={anmelden}>
          {!adminAnmeldung && ['login', 'register'].includes(modus) && (
            <div className="login-mode-switch" aria-label="Anmeldung oder Registrierung">
              <button
                className={`login-mode-button${modus === 'login' ? ' selected' : ''}`}
                type="button"
                aria-pressed={modus === 'login'}
                onClick={() => {
                  setModus('login')
                  setFehler('')
                  setErfolg('')
                }}
              >
                Anmelden
              </button>
              <button
                className={`login-mode-button${modus === 'register' ? ' selected' : ''}`}
                type="button"
                aria-pressed={modus === 'register'}
                onClick={() => {
                  setModus('register')
                  setFehler('')
                  setErfolg('')
                }}
              >
                Registrieren
              </button>
            </div>
          )}

          <div className="login-card-head">
            <span className="login-lock-badge" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <rect x="5" y="10" width="14" height="10" rx="2" />
                <path d="M8 10V7.8A4 4 0 0 1 12 4a4 4 0 0 1 4 3.8V10" />
              </svg>
            </span>
            <div>
              <p className="helper-overline">
                {modus === 'register'
                  ? 'Neues Schülerkonto'
                  : modus.startsWith('reset')
                    ? 'Passwort ändern'
                    : 'Sicher anmelden'}
              </p>
              <h2>
                {modus === 'register'
                  ? 'Dein Konto für IT-Praktika'
                  : modus.startsWith('reset')
                    ? 'Passwort sicher zurücksetzen'
                    : 'Mit deinem Konto anmelden'}
              </h2>
            </div>
          </div>

          {modus !== 'reset-confirm' && <label className="login-field">
            <span className="login-field-label">
              <span className="login-field-icon">
                <FieldIcon type="mail" />
              </span>
              E-Mail-Adresse
            </span>
            <input
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="name@beispiel.ch"
            />
          </label>}

          {modus !== 'reset-request' && <label className="login-field">
            <span className="login-field-label">
              <span className="login-field-icon">
                <FieldIcon type="lock" />
              </span>
              Passwort
            </span>
            <input
              type="password"
              value={passwort}
              onChange={(event) => setPasswort(event.target.value)}
              required={modus !== 'reset-request'}
              minLength={['register', 'reset-confirm'].includes(modus) ? 12 : undefined}
              maxLength={['register', 'reset-confirm'].includes(modus) ? 72 : undefined}
              autoComplete={['register', 'reset-confirm'].includes(modus) ? 'new-password' : 'current-password'}
              placeholder={['register', 'reset-confirm'].includes(modus) ? 'Mindestens 12 Zeichen' : 'Dein Passwort'}
            />
          </label>}

          {['register', 'reset-confirm'].includes(modus) && (
            <label className="login-field">
              <span className="login-field-label">
                <span className="login-field-icon">
                  <FieldIcon type="lock" />
                </span>
                Passwort wiederholen
              </span>
              <input
                type="password"
                autoComplete="new-password"
                required
                minLength={12}
                maxLength={72}
                value={passwortBestaetigen}
                onChange={(event) => setPasswortBestaetigen(event.target.value)}
                placeholder="Passwort erneut eingeben"
              />
            </label>
          )}

          {fehler && (
            <p className="login-error" role="alert">
              {fehler}
            </p>
          )}

          {erfolg && (
            <p className="login-success" role="status">
              {erfolg}
            </p>
          )}

          <button className="btn-primary login-submit" type="submit" disabled={wirdAngemeldet}>
            <span aria-hidden="true">→</span>
            <span>
              {wirdAngemeldet
                ? modus === 'register'
                  ? 'Konto wird erstellt …'
                  : modus.startsWith('reset')
                    ? 'Wird verarbeitet …'
                    : 'Anmeldung läuft …'
                : modus === 'register'
                  ? 'Konto erstellen'
                  : modus === 'reset-request'
                    ? 'Reset-Link anfordern'
                    : modus === 'reset-confirm'
                      ? 'Passwort speichern'
                      : 'Anmelden'}
            </span>
          </button>

          <p className="login-note">
            {modus === 'register'
              ? 'Hier kannst du ein Schülerkonto erstellen. Admin-Konten können nicht öffentlich registriert werden.'
              : modus.startsWith('reset')
                ? 'Aus Sicherheitsgründen verrät die Anfrage nicht, ob ein Konto mit dieser E-Mail-Adresse besteht.'
                : 'Dein Zugang und deine Rolle werden vom Server geprüft. Für den Adminbereich brauchst du ein Admin-Konto.'}
          </p>

          {modus === 'login' && !adminAnmeldung && (
            <button
              className="btn-secondary"
              type="button"
              onClick={() => {
                setModus('reset-request')
                setFehler('')
                setErfolg('')
              }}
            >
              Passwort vergessen?
            </button>
          )}
          {modus.startsWith('reset') && (
            <button
              className="btn-secondary"
              type="button"
              onClick={() => {
                setModus('login')
                setSearchParams({})
                setFehler('')
                setErfolg('')
              }}
            >
              Zurück zur Anmeldung
            </button>
          )}
        </form>
      </main>
    </>
  )
}
