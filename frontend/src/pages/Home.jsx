import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { BERUFE } from '../data/internships'
import { useAuth } from '../AuthContext'
import { useStellen } from '../StellenContext'

const emptyForm = {
  titel: '',
  firma: '',
  standort: '',
  dauer: '',
  startOption: 'Nach Vereinbarung',
  startDatum: '',
  beruf: BERUFE[0],
  ansprechperson: '',
  email: '',
  telefon: '',
  bewerbungsUrl: '',
  entlohnung: '',
  beschreibung: '',
  anforderungen: '',
}

export default function Home() {
  const { user } = useAuth()
  const {
    stellen,
    stellenLaden,
    stellenFehler,
    merklisteFehler,
    createStelle,
    gemerkteIds,
    toggleMerken,
  } = useStellen()
  const navigate = useNavigate()
  const [suche, setSuche] = useState('')
  const [standort, setStandort] = useState('alle')
  const [beruf, setBeruf] = useState('alle')
  const [nurGemerkte, setNurGemerkte] = useState(false)
  const [formOffen, setFormOffen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [formFehler, setFormFehler] = useState('')
  const [wirdGespeichert, setWirdGespeichert] = useState(false)
  const [merklisteAktionFehler, setMerklisteAktionFehler] = useState('')

  function filterZuruecksetzen() {
    setSuche('')
    setStandort('alle')
    setBeruf('alle')
    setNurGemerkte(false)
  }

  const standorte = useMemo(() => {
    return [...new Set(stellen.map((s) => s.standort))].sort()
  }, [stellen])

  const gefiltert = useMemo(() => {
    const q = suche.trim().toLowerCase()
    return stellen.filter((s) => {
      const matchStandort = standort === 'alle' || s.standort === standort
      const matchBeruf = beruf === 'alle' || s.beruf === beruf
      const hay = [
        s.titel,
        s.firma,
        s.standort,
        s.dauer,
        s.startdatum ?? 'Nach Vereinbarung',
        s.beruf,
        s.entlohnung,
        s.beschreibung,
        ...(s.anforderungen ?? []),
      ]
        .join(' ')
        .toLowerCase()
      const matchMerkliste = !nurGemerkte || gemerkteIds.includes(s.id)
      return matchStandort && matchBeruf && matchMerkliste && (!q || hay.includes(q))
    })
  }, [stellen, suche, standort, beruf, nurGemerkte, gemerkteIds])

  async function speichern(event) {
    event.preventDefault()
    setFormFehler('')
    setWirdGespeichert(true)
    const request = {
      titel: form.titel.trim(),
      firma: form.firma.trim(),
      standort: form.standort.trim(),
      dauer: form.dauer.trim(),
      startOption: form.startOption === 'Ab sofort'
        ? 'IMMEDIATE'
        : form.startOption === 'Datum'
          ? 'DATE'
          : 'BY_AGREEMENT',
      startDatum: form.startOption === 'Datum' ? form.startDatum : null,
      beruf: form.beruf,
      ansprechperson: form.ansprechperson.trim(),
      email: form.email.trim(),
      telefon: form.telefon.trim(),
      bewerbungsUrl: form.bewerbungsUrl.trim(),
      entlohnung: form.entlohnung.trim() || 'nicht angegeben',
      beschreibung: form.beschreibung.trim(),
      anforderungen: form.anforderungen
        .split('\n')
        .map((line) => line.replace(/^[-•]\s*/, '').trim())
        .filter(Boolean),
      status: 'PUBLISHED',
    }

    try {
      const created = await createStelle(request)
      setForm(emptyForm)
      setFormOffen(false)
      navigate(`/stelle/${created.id}`)
    } catch (error) {
      setFormFehler(error.message)
    } finally {
      setWirdGespeichert(false)
    }
  }

  async function merken(stelleId) {
    setMerklisteAktionFehler('')
    if (user?.role !== 'student') {
      navigate('/login')
      return
    }
    try {
      await toggleMerken(stelleId)
    } catch (error) {
      setMerklisteAktionFehler(error.message)
    }
  }

  return (
    <>
      <header className="hero">
        <div className="hero-inner">
          <p className="eyebrow">IT-Praktika Schweiz</p>
          <h1>Finde dein nächstes Informatik-Praktikum</h1>
          <p className="lede">
            Filtere nach ICT-Beruf, Standort und Dauer. Klicke eine Stelle an,
            um zur eigenen Seite mit Anforderungen und Kontakt zu gelangen.
          </p>
          <div className="toolbar">
            <input
              className="search"
              type="search"
              placeholder="Suche nach Firma, Skill, Stadt…"
              value={suche}
              onChange={(e) => setSuche(e.target.value)}
              aria-label="Stellen durchsuchen"
            />
            <select
              value={beruf}
              onChange={(e) => setBeruf(e.target.value)}
              aria-label="Beruf filtern"
            >
              <option value="alle">Alle Berufe</option>
              {BERUFE.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
            <select
              value={standort}
              onChange={(e) => setStandort(e.target.value)}
              aria-label="Standort filtern"
            >
              <option value="alle">Alle Standorte</option>
              {standorte.map((ort) => (
                <option key={ort} value={ort}>
                  {ort}
                </option>
              ))}
            </select>
            {user?.role === 'admin' && (
              <button type="button" className="btn-primary" onClick={() => setFormOffen(true)}>
                Stelle erfassen
              </button>
            )}
            <button
              type="button"
              className="btn-secondary"
              onClick={filterZuruecksetzen}
              disabled={
                !suche && standort === 'alle' && beruf === 'alle' && !nurGemerkte
              }
            >
              Filter zurücksetzen
            </button>
          </div>
          <button
            type="button"
            className={`saved-filter${nurGemerkte ? ' active' : ''}`}
            aria-pressed={nurGemerkte}
            onClick={() => setNurGemerkte((aktuell) => !aktuell)}
          >
            ★ Gemerkte Stellen ({gemerkteIds.length})
          </button>
          <p className="saved-note">
            {user?.role === 'student'
              ? 'Deine Merkliste wird mit deinem Konto gespeichert.'
              : 'Melde dich mit einem Schülerkonto an, um Stellen dauerhaft zu merken.'}
          </p>
          {(stellenFehler || merklisteFehler || merklisteAktionFehler) && (
            <p className="login-error" role="alert">
              {stellenFehler || merklisteFehler || merklisteAktionFehler}
            </p>
          )}
        </div>
      </header>

      <main className="content">
        {stellenLaden ? (
          <p className="empty" role="status">Stellen werden geladen …</p>
        ) : stellenFehler ? (
          <p className="empty" role="alert">Die Stellen konnten nicht geladen werden.</p>
        ) : <p className="count">
          {gefiltert.length} {gefiltert.length === 1 ? 'Stelle' : 'Stellen'}
        </p>}
        {!stellenLaden && !stellenFehler && gefiltert.length === 0 ? (
          <p className="empty">Keine Treffer. Passe Suche oder Filter an.</p>
        ) : !stellenLaden && !stellenFehler && (
          <ul className="grid">
            {gefiltert.map((stelle) => (
              <li key={stelle.id}>
                <article className="card">
                  <Link className="card-link-content" to={`/stelle/${stelle.id}`}>
                    <div className="card-top">
                      <p className="badge">{stelle.beruf}</p>
                      <h2>{stelle.titel}</h2>
                      <p className="firma">{stelle.firma}</p>
                    </div>
                    <dl className="meta">
                      <div>
                        <dt>Standort</dt>
                        <dd>{stelle.standort}</dd>
                      </div>
                      <div>
                        <dt>Dauer</dt>
                        <dd>{stelle.dauer}</dd>
                      </div>
                      <div>
                        <dt>Start</dt>
                        <dd>{stelle.startdatum || 'Nach Vereinbarung'}</dd>
                      </div>
                      <div>
                        <dt>Entlöhnung</dt>
                        <dd>{stelle.entlohnung}</dd>
                      </div>
                    </dl>
                    <p className="preview">{stelle.beschreibung}</p>
                    <span className="card-cta">Zur Stelle</span>
                  </Link>
                  <button
                    type="button"
                    className={`save-job${gemerkteIds.includes(stelle.id) ? ' saved' : ''}`}
                    aria-pressed={gemerkteIds.includes(stelle.id)}
                    aria-label={
                      gemerkteIds.includes(stelle.id)
                        ? `${stelle.titel} aus Merkliste entfernen`
                        : `${stelle.titel} merken`
                    }
                    onClick={() => void merken(stelle.id)}
                  >
                    {gemerkteIds.includes(stelle.id) ? '★ Gemerkt' : '☆ Stelle merken'}
                  </button>
                </article>
              </li>
            ))}
          </ul>
        )}
      </main>

      {formOffen && user?.role === 'admin' && (
        <div className="overlay" onClick={() => setFormOffen(false)}>
          <form
            className="dialog form"
            onClick={(e) => e.stopPropagation()}
            onSubmit={speichern}
          >
            <button className="close" type="button" onClick={() => setFormOffen(false)}>
              Schliessen
            </button>
            <h2>Neue Praktikumsstelle</h2>
            <label>
              Titel
              <input
                required
                value={form.titel}
                onChange={(e) => setForm({ ...form, titel: e.target.value })}
              />
            </label>
            <div className="row">
              <label>
                Firma
                <input
                  required
                  value={form.firma}
                  onChange={(e) => setForm({ ...form, firma: e.target.value })}
                />
              </label>
              <label>
                Standort
                <input
                  required
                  value={form.standort}
                  onChange={(e) => setForm({ ...form, standort: e.target.value })}
                />
              </label>
            </div>
            <div className="row">
              <label>
                Beruf
                <select
                  value={form.beruf}
                  onChange={(e) => setForm({ ...form, beruf: e.target.value })}
                >
                  {BERUFE.map((name) => (
                    <option key={name} value={name}>
                      {name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Dauer
                <input
                  required
                  placeholder="z. B. 6 Monate"
                  value={form.dauer}
                  onChange={(e) => setForm({ ...form, dauer: e.target.value })}
                />
              </label>
            </div>
            <div className="row">
              <label>
                Praktikumsstart
                <select
                  value={form.startOption}
                  onChange={(e) => setForm({ ...form, startOption: e.target.value })}
                >
                  <option>Ab sofort</option>
                  <option>Nach Vereinbarung</option>
                  <option>Datum</option>
                </select>
              </label>
              {form.startOption === 'Datum' && (
                <label>
                  Startdatum
                  <input
                    type="date"
                    required
                    value={form.startDatum}
                    onChange={(e) => setForm({ ...form, startDatum: e.target.value })}
                  />
                </label>
              )}
            </div>
            <label>
              Entlöhnung
              <input
                placeholder="z. B. CHF 1’800 / Monat"
                value={form.entlohnung}
                onChange={(e) => setForm({ ...form, entlohnung: e.target.value })}
              />
            </label>
            <div className="row">
              <label>
                E-Mail
                <input
                  required
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </label>
              <label>
                Telefon
                <input
                  required
                  value={form.telefon}
                  onChange={(e) => setForm({ ...form, telefon: e.target.value })}
                />
              </label>
            </div>
            <label>
              Ansprechperson <span className="optional-label">(optional)</span>
              <input
                value={form.ansprechperson}
                onChange={(e) => setForm({ ...form, ansprechperson: e.target.value })}
                placeholder="z. B. Frau Müller"
              />
            </label>
            <label>
              Bewerbungslink der Firma <span className="optional-label">(optional)</span>
              <input
                type="url"
                pattern="https?://.+"
                title="Bitte eine Webadresse beginnend mit https:// oder http:// eingeben."
                value={form.bewerbungsUrl}
                onChange={(e) => setForm({ ...form, bewerbungsUrl: e.target.value })}
                placeholder="https://firma.ch/jobs/praktikum"
              />
              <span className="optional-help">
                Wenn angegeben, können Bewerbende sich direkt über die
                Firmenwebseite bewerben.
              </span>
            </label>
            <label>
              Kurzbeschreibung
              <textarea
                required
                rows={3}
                value={form.beschreibung}
                onChange={(e) => setForm({ ...form, beschreibung: e.target.value })}
              />
            </label>
            <label>
              Anforderungen (eine pro Zeile)
              <textarea
                required
                rows={4}
                placeholder={'Grundkenntnisse JavaScript\nGit-Erfahrung'}
                value={form.anforderungen}
                onChange={(e) => setForm({ ...form, anforderungen: e.target.value })}
              />
            </label>
              {formFehler && <p className="login-error" role="alert">{formFehler}</p>}
              <button type="submit" className="btn-primary" disabled={wirdGespeichert}>
                {wirdGespeichert ? 'Wird gespeichert …' : 'Speichern'}
              </button>
          </form>
        </div>
      )}
    </>
  )
}
