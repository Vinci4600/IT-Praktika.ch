import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function RequireRole({ role, children }) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (user.role !== role) {
    return (
      <main className="content access-denied">
        <section className="detail-card">
          <p className="helper-overline">Keine Berechtigung</p>
          <h1>Dein Konto hat keinen Zugriff auf diesen Bereich.</h1>
          <p>
            Für diesen Bereich brauchst du ein Admin-Konto. Melde dich ab und
            anschließend mit einem Admin-Konto an.
          </p>
          <button
            type="button"
            className="btn-primary"
            onClick={() => {
              logout()
              navigate('/login', { state: { from: location } })
            }}
          >
            Abmelden und erneut anmelden
          </button>
        </section>
      </main>
    )
  }

  return children
}
