import { useState } from 'react'
import { ADMIN_EMAIL } from '../data/site'

const emptyForm = {
  typ: 'Schule',
  name: '',
  kontaktperson: '',
  email: '',
  telefon: '',
  anliegen: 'Plattform für Schüler/innen empfehlen',
  nachricht: '',
}

export default function Schulkontakt() {
  const [form, setForm] = useState(emptyForm)

  function aendern(event) {
    const { name, value } = event.target
    setForm((aktuell) => ({ ...aktuell, [name]: value }))
  }

  function vorbereiten(event) {
    event.preventDefault()
    const name = form.name.trim()
    const kontaktperson = form.kontaktperson.trim()
    const betreff = `Anfrage ${form.typ.toLowerCase()}: ${name}`
    const nachricht = [
      'Guten Tag',
      '',
      'ich möchte gerne Kontakt zur IT-Praktika-Plattform aufnehmen.',
      '',
      `Einrichtungstyp: ${form.typ}`,
      `Name der Einrichtung: ${name}`,
      `Kontaktperson: ${kontaktperson}`,
      `E-Mail: ${form.email.trim()}`,
      `Telefon: ${form.telefon.trim() || 'Nicht angegeben'}`,
      `Anliegen: ${form.anliegen}`,
      `Nachricht: ${form.nachricht.trim() || 'Keine zusätzliche Nachricht'}`,
      '',
      'Freundliche Grüsse',
      kontaktperson,
    ].join('\n')

    window.location.href =
      `mailto:${ADMIN_EMAIL}?subject=${encodeURIComponent(betreff)}&body=${encodeURIComponent(nachricht)}`
  }

  return (
    <>
      <header className="hero hero-compact">
        <div className="hero-inner">
          <p className="eyebrow">Gemeinsam den Berufseinstieg unterstützen</p>
          <h1>Für Schulen</h1>
          <p className="lede">
            Unterstützen Sie Ihre Schüler/innen bei der Suche nach einem
            IT-Berufspraktikum oder melden Sie einen schulischen Betrieb, der
            Praktikumsplätze anbieten möchte.
          </p>
        </div>
      </header>

      <main className="content company-contact-layout">
        <section className="company-contact-intro">
          <p className="helper-overline">Zusammenarbeit mit Schulen</p>
          <h2>Praktikumsmöglichkeiten sichtbar machen.</h2>
          <p>
            Schulen können die Plattform an interessierte Schüler/innen
            weiterempfehlen. Schulbetriebe können sich melden, wenn sie ein
            IT-Berufspraktikum anbieten möchten. Der Admin bespricht mit Ihnen
            die nächsten Schritte.
          </p>
          <div className="company-contact-note">
            <span className="company-contact-note-icon" aria-hidden="true">✓</span>
            <p>
              Ob Sie die Plattform vorstellen oder einen Praktikumsplatz
              anbieten möchten – wählen Sie Ihr Anliegen im Formular aus.
            </p>
          </div>
          <div className="company-contact-email">
            <span>Kontakt per E-Mail</span>
            <a href={`mailto:${ADMIN_EMAIL}`}>{ADMIN_EMAIL}</a>
          </div>
        </section>

        <form className="detail-card form company-contact-form" onSubmit={vorbereiten}>
          <div>
            <p className="helper-overline">Kontakt aufnehmen</p>
            <h2>Wie können wir zusammenarbeiten?</h2>
          </div>
          <div className="row">
            <label>
              Sie melden sich als
              <select name="typ" value={form.typ} onChange={aendern}>
                <option>Schule</option>
                <option>Schulbetrieb</option>
              </select>
            </label>
            <label>
              {form.typ === 'Schule' ? 'Name der Schule' : 'Name des Schulbetriebs'}
              <input
                name="name"
                autoComplete="organization"
                required
                value={form.name}
                onChange={aendern}
              />
            </label>
          </div>
          <div className="row">
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
          </div>
          <div className="row">
            <label>
              Telefonnummer <span className="optional-label">(optional)</span>
              <input
                name="telefon"
                type="tel"
                autoComplete="tel"
                value={form.telefon}
                onChange={aendern}
              />
            </label>
            <label>
              Anliegen
              <select name="anliegen" value={form.anliegen} onChange={aendern}>
                <option>Plattform für Schüler/innen empfehlen</option>
                <option>IT-Praktikumsplätze im Schulbetrieb anbieten</option>
                <option>Zusammenarbeit besprechen</option>
                <option>Anderes Anliegen</option>
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
              placeholder="Teilen Sie uns kurz mit, worum es geht."
            />
          </label>
          <button type="submit" className="btn-primary">
            Anfrage per E-Mail vorbereiten
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
