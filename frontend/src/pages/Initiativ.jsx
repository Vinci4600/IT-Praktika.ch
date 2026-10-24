import { useEffect, useState } from 'react'
import { apiFetch } from '../api'

export default function Initiativ() {
  const [firmen, setFirmen] = useState([])
  const [laden, setLaden] = useState(true)
  const [fehler, setFehler] = useState('')

  useEffect(() => {
    let cancelled = false
    apiFetch('/api/v1/firmen')
      .then((result) => {
        if (!Array.isArray(result?.data)) {
          throw new Error('Die API hat ungültige Firmendaten zurückgegeben.')
        }
        if (!cancelled) {
          setFirmen(result.data.filter((firma) => firma.partner || firma.initiativeListed))
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
  }, [])

  return (
    <>
      <header className="hero hero-compact">
        <div className="hero-inner">
          <p className="eyebrow">Kein passendes Inserat?</p>
          <h1>Initiativ bewerben</h1>
          <p className="lede">
            Die Liste enthält Partnerfirmen und mögliche Praktikumsfirmen aus
            der Firmenliste des UZH-Instituts für Informatik. Nutze die
            veröffentlichten Kontaktdaten und prüfe vor dem Versand, ob sie für
            eine Bewerbung geeignet sind.
          </p>
        </div>
      </header>

      <main className="content">
        <p className="count">
          {firmen.length} {firmen.length === 1 ? 'Firma' : 'Firmen'}
        </p>
        {laden && <p className="empty" role="status">Firmen werden geladen …</p>}
        {fehler && <p className="login-error" role="alert">{fehler}</p>}
        {!laden && !fehler && !firmen.length && (
          <p className="empty">Aktuell sind keine Firmen für Initiativbewerbungen verfügbar.</p>
        )}
        <ul className="grid">
          {!laden && !fehler && firmen.map((firma) => {
            const betreff = encodeURIComponent(`Initiativbewerbung – ${firma.name}`)
            const nachricht = encodeURIComponent(
              `Guten Tag\n\nIch möchte mich gerne initiativ bei ${firma.name} bewerben. ` +
                'Im Anhang finden Sie meine Bewerbungsunterlagen. Ich freue mich, von Ihnen zu hören.\n\nFreundliche Grüsse\n',
            )

            return (
              <li key={firma.id}>
                <article className="card">
                  <div className="card-top">
                    <h2>{firma.name}</h2>
                    <p className="firma">{firma.standort}</p>
                  </div>
                  {firma.contactPerson && (
                    <p className="preview">Kontaktperson: {firma.contactPerson}</p>
                  )}
                  <dl className="meta">
                    <div>
                      <dt>IT-Schwerpunkte</dt>
                      <dd>
                        <div className="tags">
                          {firma.spezialitaeten.map((spezialitaet) => (
                            <span key={spezialitaet} className="tag">
                              {spezialitaet}
                            </span>
                          ))}
                        </div>
                      </dd>
                    </div>
                  </dl>
                  <p className="preview">{firma.beschreibung}</p>
                  {firma.internshipUrl && (
                    <p className="preview">
                      <a href={firma.internshipUrl} target="_blank" rel="noreferrer">
                        Veröffentlichte Praktikumsstellen ansehen
                      </a>
                    </p>
                  )}
                  <div className="card-footer">
                    <div className="contact-info">
                      <p>
                        <strong>E-Mail</strong>
                        {firma.email
                          ? <a href={`mailto:${firma.email}`}>{firma.email}</a>
                          : <span>Keine E-Mail-Adresse hinterlegt</span>}
                      </p>
                      <p>
                        <strong>Telefon</strong>
                        {firma.telefon
                          ? <a href={`tel:${firma.telefon.replace(/\s/g, '')}`}>{firma.telefon}</a>
                          : <span>Keine Telefonnummer hinterlegt</span>}
                      </p>
                      {firma.website && (
                        <p>
                          <strong>Website</strong>
                          <a href={firma.website} target="_blank" rel="noreferrer">
                            {firma.website.replace(/^https?:\/\//, '')}
                          </a>
                        </p>
                      )}
                    </div>
                    {firma.email && (
                      <a
                        className="btn-primary"
                        href={`mailto:${firma.email}?subject=${betreff}&body=${nachricht}`}
                      >
                        Per E-Mail bewerben
                      </a>
                    )}
                    {!firma.email && firma.website && (
                      <a className="btn-secondary" href={firma.website} target="_blank" rel="noreferrer">
                        Website und Kontakt ansehen
                      </a>
                    )}
                  </div>
                </article>
              </li>
            )
          })}
        </ul>
        <section className="helper-tip" aria-label="Hinweise zu den Firmenkontakten">
          <div className="helper-tip-icon" aria-hidden="true">i</div>
          <div>
            <p className="helper-overline">Quelle und Kontakt</p>
            <h2>Angaben vor einer Bewerbung prüfen</h2>
            <p>
              Die UZH-Firmenliste nennt mögliche Praktikumsfirmen. Ein Eintrag
              ist keine Zusage, dass eine Stelle frei ist oder dass die
              Kontaktadresse Bewerbungen entgegennimmt.
            </p>
            <p>
              <a href="https://www.ifi.uzh.ch/de/studies/bsc-info/firmenliste.html" target="_blank" rel="noreferrer">
                Originale Firmenliste des UZH-Instituts für Informatik ansehen
              </a>
            </p>
            <p>
              Ein E-Mail-Button erscheint nur, wenn die UZH-Liste eine
              E-Mail-Adresse veröffentlicht. Die Nachricht wird nicht
              automatisch versendet; prüfe sie und füge deine Unterlagen selbst
              hinzu.
            </p>
          </div>
        </section>
      </main>
    </>
  )
}
