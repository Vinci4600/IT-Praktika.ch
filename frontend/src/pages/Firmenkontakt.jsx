import { useState } from 'react'
import { ADMIN_EMAIL } from '../data/site'

const emptyForm = {
  firma: '',
  kontaktperson: '',
  email: '',
  telefon: '',
  anliegen: 'Praktikumsstelle ausschreiben',
  rueckrufzeit: 'Vormittags (08:00–12:00 Uhr)',
  nachricht: '',
}

export default function Firmenkontakt() {
  const [form, setForm] = useState(emptyForm)

  function vorbereiten(event) {
    event.preventDefault()
    const betreff = `Rückrufanfrage von ${form.firma.trim()}`
    const nachricht = [
      'Guten Tag',
      '',
      'ich möchte gerne wegen einer Praktikumsausschreibung kontaktiert werden.',
      '',
      `Firma: ${form.firma.trim()}`,
      `Kontaktperson: ${form.kontaktperson.trim()}`,
      `E-Mail: ${form.email.trim()}`,
      `Telefon: ${form.telefon.trim()}`,
      `Anliegen: ${form.anliegen}`,
      `Bevorzugte Rückrufzeit: ${form.rueckrufzeit}`,
      `Nachricht: ${form.nachricht.trim() || 'Keine zusätzliche Nachricht'}`,
      '',
      'Freundliche Grüsse',
      form.kontaktperson.trim(),
    ].join('\n')

    window.location.href =
      `mailto:${ADMIN_EMAIL}?subject=${encodeURIComponent(betreff)}&body=${encodeURIComponent(nachricht)}`
  }

  function aendern(event) {
    const { name, value } = event.target
    setForm((aktuell) => ({ ...aktuell, [name]: value }))
  }

  return (
    <>
      <header className="hero hero-compact">
        <div className="hero-inner">
          <p className="eyebrow">Zusammen Praktika ermöglichen</p>
          <h1>Für Firmen</h1>
          <p className="lede">
            Sie möchten ein IT-Berufspraktikum anbieten? Wir unterstützen Sie
            dabei, Ihre Stelle passenden Praktikantinnen und Praktikanten
            vorzustellen.
          </p>
        </div>
      </header>

      <main className="content company-contact-layout">
        <section className="company-contact-intro">
          <p className="helper-overline">Persönlich betreut</p>
          <h2>Wir kümmern uns um die Ausschreibung.</h2>
          <p>
            Teilen Sie uns mit, worum es geht und wann wir Sie am besten
            erreichen. Der Admin meldet sich, um die nächsten Schritte mit
            Ihnen zu besprechen.
          </p>
          <div className="company-contact-note">
            <span className="company-contact-note-icon" aria-hidden="true">✓</span>
            <p>
              Noch keine fertige Ausschreibung? Kein Problem – wir besprechen
              gemeinsam, welche Angaben benötigt werden.
            </p>
          </div>
          <div className="company-contact-email">
            <span>Kontakt per E-Mail</span>
            <a href={`mailto:${ADMIN_EMAIL}`}>{ADMIN_EMAIL}</a>
          </div>
        </section>

        <form className="detail-card form company-contact-form" onSubmit={vorbereiten}>
          <div>
            <p className="helper-overline">Rückruf anfragen</p>
            <h2>Wie erreichen wir Sie?</h2>
          </div>
          <div className="row">
            <label>
              Firma
              <input
                name="firma"
                autoComplete="organization"
                required
                value={form.firma}
                onChange={aendern}
              />
            </label>
            <label>
              Kontaktperson
              <input
                name="kontaktperson"
                autoComplete="name"
                required
                value={form.kontaktperson}
                onChange={aendern}
              />
            </label>
          </div>
          <div className="row">
            <label>
              E-Mail
              <input
                name="email"
                type="email"
                autoComplete="email"
                required
                value={form.email}
                onChange={aendern}
              />
            </label>
            <label>
              Telefonnummer
              <input
                name="telefon"
                type="tel"
                autoComplete="tel"
                required
                value={form.telefon}
                onChange={aendern}
              />
            </label>
          </div>
          <div className="row">
            <label>
              Anliegen
              <select name="anliegen" value={form.anliegen} onChange={aendern}>
                <option>Praktikumsstelle ausschreiben</option>
                <option>Mehr über die Plattform erfahren</option>
                <option>Bestehende Ausschreibung besprechen</option>
                <option>Anderes Anliegen</option>
              </select>
            </label>
            <label>
              Bevorzugte Rückrufzeit
              <select
                name="rueckrufzeit"
                value={form.rueckrufzeit}
                onChange={aendern}
              >
                <option>Vormittags (08:00–12:00 Uhr)</option>
                <option>Nachmittags (13:00–17:00 Uhr)</option>
                <option>Keine Präferenz</option>
              </select>
            </label>
          </div>
          <label>
            Nachricht <span className="optional-label">(optional)</span>
            <textarea
              name="nachricht"
              rows={4}
              value={form.nachricht}
              onChange={aendern}
              placeholder="Gibt es schon Details zur Praktikumsstelle?"
            />
          </label>
          <button type="submit" className="btn-primary">
            Rückrufanfrage per E-Mail vorbereiten
          </button>
          <p className="company-contact-disclaimer">
            Es öffnet sich Ihr E-Mail-Programm mit einer vorbereiteten Anfrage.
            Bitte prüfen und senden Sie die E-Mail dort ab.
          </p>
        </form>
      </main>
    </>
  )
}
