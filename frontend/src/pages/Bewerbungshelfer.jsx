const schritte = [
  {
    nummer: '01',
    titel: 'Lebenslauf',
    text: 'Zeig deine Ausbildung, Projekte und IT-Kenntnisse auf einen Blick.',
    status: 'Zum Vorbereiten',
  },
  {
    nummer: '02',
    titel: 'Motivationsschreiben',
    text: 'Erkläre, warum du dich für genau dieses Praktikum interessierst.',
    status: 'Zum Vorbereiten',
  },
  {
    nummer: '03',
    titel: 'Unterlagen prüfen',
    text: 'Kontrolliere Kontaktdaten, Rechtschreibung und Dateinamen vor dem Versand.',
    status: 'Zum Vorbereiten',
  },
]

const tipps = [
  'Nenne konkrete Projekte, an denen du gearbeitet hast.',
  'Passe dein Schreiben an die Firma und das Praktikum an.',
  'Fasse deine Unterlagen am besten als übersichtliche PDF-Dateien zusammen.',
]

export default function Bewerbungshelfer() {
  return (
    <>
      <header className="hero hero-compact">
        <div className="hero-inner">
          <p className="eyebrow">Dein nächster Schritt</p>
          <h1>Bewerbungshelfer</h1>
          <p className="lede">
            Mit einer guten Vorbereitung kommst du deinem IT-Praktikum näher.
            Hier findest du die wichtigsten Bausteine für deine Bewerbung.
          </p>
        </div>
      </header>

      <main className="content helper-content">
        <section className="helper-welcome">
          <div>
            <p className="helper-overline">Bewerbungs-Checkliste</p>
            <h2>Gut vorbereitet macht den Unterschied.</h2>
            <p>
              Eine vollständige Bewerbung zeigt, was dich ausmacht und woran
              du Freude hast. Starte mit diesen drei Schritten.
            </p>
          </div>
          <div className="helper-progress" aria-label="0 von 3 Schritten vorbereitet">
            <span className="helper-progress-number">0<span> / 3</span></span>
            <span className="helper-progress-label">Schritte vorbereitet</span>
            <div className="helper-progress-track">
              <span />
            </div>
          </div>
        </section>

        <section className="helper-section" aria-labelledby="helper-steps-title">
          <div className="helper-section-heading">
            <div>
              <p className="helper-overline">Schritt für Schritt</p>
              <h2 id="helper-steps-title">Deine Unterlagen</h2>
            </div>
            <span className="helper-pill">3 wichtige Bausteine</span>
          </div>
          <div className="helper-steps">
            {schritte.map((schritt) => (
              <article className="helper-step" key={schritt.nummer}>
                <span className="helper-step-number">{schritt.nummer}</span>
                <div className="helper-step-copy">
                  <h3>{schritt.titel}</h3>
                  <p>{schritt.text}</p>
                </div>
                <span className="helper-step-status">{schritt.status}</span>
              </article>
            ))}
          </div>
        </section>

        <aside className="helper-tip">
          <div className="helper-tip-icon" aria-hidden="true">i</div>
          <div>
            <p className="helper-overline">Gut zu wissen</p>
            <h2>Auch kleine Projekte zählen.</h2>
            <p>
              Schulprojekte, eigene Websites oder technische Hobbys zeigen
              deine Neugier und gehören in deinen Lebenslauf.
            </p>
          </div>
        </aside>

        <section className="helper-section helper-tips" aria-labelledby="helper-tips-title">
          <p className="helper-overline">Kurz &amp; hilfreich</p>
          <h2 id="helper-tips-title">Drei Tipps für den letzten Check</h2>
          <ul>
            {tipps.map((tipp, index) => (
              <li key={tipp}>
                <span>{String(index + 1).padStart(2, '0')}</span>
                {tipp}
              </li>
            ))}
          </ul>
        </section>
      </main>
    </>
  )
}
