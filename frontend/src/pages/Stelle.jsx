import { Link, useParams } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../AuthContext'
import { useStellen } from '../StellenContext'

function startAnzeige(startdatum) {
  if (!startdatum) return 'Nach Vereinbarung'
  if (!/^\d{4}-\d{2}-\d{2}$/.test(startdatum)) return startdatum

  return new Date(`${startdatum}T00:00:00`).toLocaleDateString('de-CH', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

export default function Stelle() {
  const { id } = useParams()
  const { user } = useAuth()
  const { stellen, stellenLaden, stellenFehler, gemerkteIds, toggleMerken } = useStellen()
  const [merklisteFehler, setMerklisteFehler] = useState('')
  const stelle = stellen.find((item) => item.id === id)

  async function merken() {
    setMerklisteFehler('')
    try {
      await toggleMerken(stelle.id)
    } catch (error) {
      setMerklisteFehler(error.message)
    }
  }

  if (!stelle) {
    return (
      <main className="content">
        <p className="empty">
          {stellenLaden
            ? 'Die Stelle wird geladen …'
            : stellenFehler
              ? 'Die Stelle konnte nicht geladen werden.'
              : 'Diese Stelle wurde nicht gefunden.'}{' '}
          <Link to="/">Zurück zur Übersicht</Link>
        </p>
      </main>
    )
  }

  return (
    <main className="content detail-page">
      <Link className="back" to="/">
        ← Alle Stellen
      </Link>
      <article className="detail-card">
        <p className="badge">{stelle.beruf}</p>
        <p className="eyebrow">{stelle.firma}</p>
        <h1>{stelle.titel}</h1>
        <dl className="meta detail-meta">
          <div>
            <dt>Standort</dt>
            <dd>{stelle.standort}</dd>
          </div>
          <div>
            <dt>Dauer</dt>
            <dd>{stelle.dauer}</dd>
          </div>
          <div>
            <dt>Praktikumsstart</dt>
            <dd>{startAnzeige(stelle.startdatum)}</dd>
          </div>
          <div>
            <dt>Beruf</dt>
            <dd>{stelle.beruf}</dd>
          </div>
          <div>
            <dt>Entlöhnung</dt>
            <dd>{stelle.entlohnung}</dd>
          </div>
        </dl>
        <p className="detail-text">{stelle.beschreibung}</p>
        <h2>Anforderungen</h2>
        <ul className="reqs">
          {(stelle.anforderungen ?? []).map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
        <h2>Kontakt</h2>
        <p>
          {stelle.ansprechperson && (
            <>
              Ansprechperson: {stelle.ansprechperson}
              <br />
            </>
          )}
          E-Mail: <a href={`mailto:${stelle.email}`}>{stelle.email}</a>
          <br />
          Telefon:{' '}
          <a href={`tel:${stelle.telefon.replace(/\s/g, '')}`}>{stelle.telefon}</a>
        </p>
        <div className="detail-actions">
          {user?.role === 'student' ? (
            <button
              type="button"
              className="btn-secondary"
              aria-pressed={gemerkteIds.includes(stelle.id)}
              onClick={() => void merken()}
            >
              {gemerkteIds.includes(stelle.id) ? '★ Aus Merkliste entfernen' : '☆ Stelle merken'}
            </button>
          ) : user?.role !== 'admin' && (
            <Link className="btn-secondary" to="/login">
              Anmelden, um Stelle zu merken
            </Link>
          )}
          <a className="btn-primary" href={`mailto:${stelle.email}?subject=${encodeURIComponent('Bewerbung: ' + stelle.titel)}`}>
            Per E-Mail bewerben
          </a>
          {stelle.bewerbungsUrl && (
            <a
              className="btn-secondary"
              href={stelle.bewerbungsUrl}
              target="_blank"
              rel="noreferrer"
            >
              Über Firmenwebseite bewerben
            </a>
          )}
          <Link className="btn-secondary" to="/initiativ">
            Initiativ bewerben
          </Link>
        </div>
        {merklisteFehler && <p className="login-error" role="alert">{merklisteFehler}</p>}
      </article>
    </main>
  )
}
