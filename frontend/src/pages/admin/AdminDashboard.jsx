import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import { apiFetch } from '../../api'

const karten = [
  {
    titel: 'Stellen',
    beschreibung: 'Ausschreibungen prüfen und verwalten.',
    link: '/admin/stellen',
    linkText: 'Stellen ansehen',
    icon: 'S',
  },
  {
    titel: 'Partnerfirmen',
    beschreibung: 'Firmenprofile und Kontaktdaten überblicken.',
    link: '/admin/partnerfirmen',
    linkText: 'Partnerfirmen ansehen',
    icon: 'P',
  },
  {
    titel: 'Anfragen',
    beschreibung: 'Hinweise zum aktuellen E-Mail-Ablauf ansehen.',
    link: '/admin/anfragen',
    linkText: 'Anfragen-Status ansehen',
    icon: 'A',
  },
]

async function fetchOverview(accessToken) {
  const [jobs, companies] = await Promise.all([
    apiFetch('/api/v1/admin/stellen', { accessToken }),
    apiFetch('/api/v1/firmen'),
  ])
  if (!Array.isArray(jobs) || !Array.isArray(companies?.data)) {
    throw new Error('Die API hat ungültige Verwaltungsdaten zurückgegeben.')
  }
  return { jobs, companies: companies.data }
}

export default function AdminDashboard() {
  const { accessToken } = useAuth()
  const [stellen, setStellen] = useState([])
  const [firmen, setFirmen] = useState([])
  const [fehler, setFehler] = useState('')
  const [laden, setLaden] = useState(true)

  useEffect(() => {
    let cancelled = false
    fetchOverview(accessToken)
      .then(({ jobs, companies }) => {
        if (!cancelled) {
          setStellen(jobs)
          setFirmen(companies)
        }
      })
      .catch((error) => {
        if (!cancelled) setFehler(error.message)
      })
      .finally(() => {
        if (!cancelled) setLaden(false)
      })
    return () => {
      cancelled = true
    }
  }, [accessToken])

  return (
    <div className="admin-page">
      <section className="admin-welcome">
        <div>
          <p className="helper-overline">Startseite</p>
          <h2>Guten Tag, Admin</h2>
          <p>Wähle einen Bereich, um mit der Verwaltung fortzufahren.</p>
        </div>
        <div className="admin-metrics">
          <div>
            <strong>{laden ? '…' : stellen.length}</strong>
            <span>Stellen in der Datenbank</span>
          </div>
          <div>
            <strong>{laden ? '…' : firmen.filter((firma) => firma.partner).length}</strong>
            <span>Partnerfirmen</span>
          </div>
          <div>
            <strong>—</strong>
            <span>Anfragen im Posteingang</span>
          </div>
        </div>
      </section>

      {fehler && <p className="login-error" role="alert">{fehler}</p>}

      <section className="admin-section" aria-labelledby="admin-areas-title">
        <div className="admin-section-heading">
          <div>
            <p className="helper-overline">Verwaltung</p>
            <h2 id="admin-areas-title">Deine Bereiche</h2>
          </div>
        </div>
        <div className="admin-cards">
          {karten.map((karte) => (
            <article className="admin-card" key={karte.titel}>
              <span className="admin-card-icon" aria-hidden="true">{karte.icon}</span>
              <div>
                <h3>{karte.titel}</h3>
                <p>{karte.beschreibung}</p>
              </div>
              <Link to={karte.link}>{karte.linkText} <span aria-hidden="true">→</span></Link>
            </article>
          ))}
        </div>
      </section>

      <section className="admin-recent detail-card">
        <div className="admin-section-heading">
          <div>
            <p className="helper-overline">Datenbank</p>
            <h2>Stellenvorschau</h2>
          </div>
          <Link to="/admin/stellen">Alle Stellen ansehen</Link>
        </div>
        {stellen.length ? (
          <ul className="admin-recent-list">
            {stellen.slice(-4).reverse().map((stelle) => (
              <li key={stelle.id}>
                <div>
                  <strong>{stelle.titel}</strong>
                  <span>{stelle.firma} · {stelle.standort}</span>
                </div>
                <span className="admin-status">{stelle.status}</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="empty">Es sind noch keine Stellen erfasst.</p>
        )}
        <p className="admin-data-note">
          Übersicht und Stellen stammen aus der Backend-Datenbank.
        </p>
      </section>
    </div>
  )
}
