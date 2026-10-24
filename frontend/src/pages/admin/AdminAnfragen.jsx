import { ADMIN_EMAIL } from '../../data/site'

export default function AdminAnfragen() {
  return (
    <section className="admin-page">
      <div className="admin-page-heading">
        <div>
          <p className="helper-overline">Posteingang</p>
          <h2>Kontaktanfragen</h2>
          <p>Firmen- und Schulkontakte an einem Ort im Blick behalten.</p>
        </div>
        <span className="admin-count">Noch nicht verbunden</span>
      </div>
      <div className="admin-inbox-empty detail-card">
        <span className="admin-inbox-icon" aria-hidden="true">✉</span>
        <h3>Es gibt noch keinen Anfrage-Posteingang.</h3>
        <p>
          Die Formulare für Firmen und Schulen öffnen aktuell das E-Mail-
          Programm der anfragenden Person. Anfragen werden nicht automatisch
          an diese Website übermittelt oder hier gespeichert.
        </p>
        <a className="btn-secondary" href={`mailto:${ADMIN_EMAIL}`}>
          Admin-E-Mail öffnen
        </a>
      </div>
      <p className="admin-data-note">
        Für eine zentrale Anfrageverwaltung braucht es später eine Backend-API,
        Speicherung, Benachrichtigungen und Status wie „Neu“ oder „Kontaktiert“.
      </p>
    </section>
  )
}
