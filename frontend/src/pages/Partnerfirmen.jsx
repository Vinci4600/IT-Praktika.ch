import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../AuthContext'
import { apiFetch } from '../api'

const emptyForm = {
  name: '',
  standort: '',
  spezialitaeten: '',
  email: '',
  contactPerson: '',
  telefon: '',
  beschreibung: '',
  website: '',
  partner: true,
  initiativeListed: false,
  internshipUrl: '',
  logoUrl: '',
}

function initials(name) {
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((word) => word[0])
    .join('')
    .toUpperCase()
}

function fromApi(company) {
  return {
    ...company,
    spezialitaeten: company.spezialitaeten.join('\n'),
    email: company.email ?? '',
    contactPerson: company.contactPerson ?? '',
    telefon: company.telefon ?? '',
    beschreibung: company.beschreibung ?? '',
    website: company.website ?? '',
    internshipUrl: company.internshipUrl ?? '',
    logoUrl: company.logoUrl ?? '',
  }
}

function toRequest(form) {
  return {
    ...form,
    spezialitaeten: form.spezialitaeten
      .split('\n')
      .map((item) => item.trim())
      .filter(Boolean),
    email: form.email || null,
    contactPerson: form.contactPerson || null,
    telefon: form.telefon || null,
    beschreibung: form.beschreibung || null,
    website: form.website || null,
    internshipUrl: form.internshipUrl || null,
    logoUrl: form.logoUrl || null,
  }
}

async function fetchCompanies() {
  const result = await apiFetch('/api/v1/firmen')
  if (!Array.isArray(result?.data)) {
    throw new Error('Die API hat ungültige Firmendaten zurückgegeben.')
  }
  return result.data
}

export default function Partnerfirmen() {
  const { accessToken } = useAuth()
  const [firmen, setFirmen] = useState([])
  const [firmaId, setFirmaId] = useState('')
  const [form, setForm] = useState(null)
  const [fehler, setFehler] = useState('')
  const [laden, setLaden] = useState(true)
  const [speichern, setSpeichern] = useState(false)
  const firma = firmen.find((company) => company.id === firmaId)

  const ladeFirmen = useCallback(async (showLoading = true) => {
    if (showLoading) {
      setFehler('')
      setLaden(true)
    }
    try {
      const companies = await fetchCompanies()
      setFirmen(companies)
      setFirmaId((selectedId) => companies.some((company) => company.id === selectedId)
        ? selectedId
        : companies[0]?.id ?? '')
    } catch (error) {
      setFehler(error.message)
    } finally {
      setLaden(false)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    fetchCompanies()
      .then((companies) => {
        if (!cancelled) {
          setFirmen(companies)
          setFirmaId(companies[0]?.id ?? '')
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

  function bearbeiten(company) {
    setForm(fromApi(company))
    setFehler('')
  }

  async function speichernFirma(event) {
    event.preventDefault()
    setFehler('')
    setSpeichern(true)
    try {
      const editing = Boolean(form.id)
      const saved = await apiFetch(
        editing ? `/api/v1/firmen/${form.id}` : '/api/v1/firmen',
        {
          method: editing ? 'PUT' : 'POST',
          body: JSON.stringify(toRequest(form)),
          accessToken,
        },
      )
      setForm(null)
      await ladeFirmen()
      setFirmaId(saved.id)
    } catch (error) {
      setFehler(error.message)
    } finally {
      setSpeichern(false)
    }
  }

  async function loeschen(company) {
    if (!window.confirm(`Soll "${company.name}" wirklich gelöscht werden?`)) return
    setFehler('')
    try {
      await apiFetch(`/api/v1/firmen/delete/${company.id}`, {
        method: 'DELETE',
        accessToken,
      })
      await ladeFirmen()
    } catch (error) {
      setFehler(error.message)
    }
  }

  return (
    <section className="admin-page partner-admin-content">
      <div className="admin-page-heading">
        <div>
          <p className="helper-overline">Verwaltung</p>
          <h2>Firmenverwaltung</h2>
          <p>Firmenprofile, Partnerstatus und Initiativbewerbungen verwalten.</p>
        </div>
        <button className="btn-primary" type="button" onClick={() => setForm({ ...emptyForm })}>
          Firma hinzufügen
        </button>
      </div>

      {fehler && <p className="login-error" role="alert">{fehler}</p>}

      {form && (
        <form className="detail-card form" onSubmit={speichernFirma}>
          <h3>{form.id ? 'Firma bearbeiten' : 'Neue Firma'}</h3>
          <label>
            Firmenname
            <input required maxLength={200} value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
          </label>
          <div className="row">
            <label>
              Standort
              <input required maxLength={160} value={form.standort} onChange={(event) => setForm({ ...form, standort: event.target.value })} />
            </label>
            <label>
              Partnerfirma
              <select value={String(form.partner)} onChange={(event) => setForm({ ...form, partner: event.target.value === 'true' })}>
                <option value="true">Ja</option>
                <option value="false">Nein</option>
              </select>
            </label>
          </div>
          <label>
            In der Initiativbewerbung anzeigen
            <select value={String(form.initiativeListed)} onChange={(event) => setForm({ ...form, initiativeListed: event.target.value === 'true' })}>
              <option value="true">Ja</option>
              <option value="false">Nein</option>
            </select>
          </label>
          <label>
            IT-Schwerpunkte (ein Eintrag pro Zeile)
            <textarea required rows={3} value={form.spezialitaeten} onChange={(event) => setForm({ ...form, spezialitaeten: event.target.value })} />
          </label>
          <div className="row">
            <label>
              Kontakt-E-Mail (optional)
              <input type="email" maxLength={254} value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
            </label>
            <label>
              Kontaktperson (optional)
              <input maxLength={200} value={form.contactPerson} onChange={(event) => setForm({ ...form, contactPerson: event.target.value })} />
            </label>
            <label>
              Telefon (optional)
              <input maxLength={40} value={form.telefon} onChange={(event) => setForm({ ...form, telefon: event.target.value })} />
            </label>
          </div>
          <label>
            Beschreibung (optional)
            <textarea maxLength={10000} rows={3} value={form.beschreibung} onChange={(event) => setForm({ ...form, beschreibung: event.target.value })} />
          </label>
          <div className="row">
            <label>
              Website (optional)
              <input type="url" maxLength={2048} value={form.website} onChange={(event) => setForm({ ...form, website: event.target.value })} />
            </label>
            <label>
              Link zu Praktikumsstellen (optional)
              <input type="url" maxLength={2048} value={form.internshipUrl} onChange={(event) => setForm({ ...form, internshipUrl: event.target.value })} />
            </label>
            <label>
              Logo-URL (optional)
              <input type="url" maxLength={2048} value={form.logoUrl} onChange={(event) => setForm({ ...form, logoUrl: event.target.value })} />
            </label>
          </div>
          <p className="admin-data-note">
            Es wird nur eine HTTPS-/HTTP-Logo-Adresse gespeichert; Datei-Uploads sind nicht aktiviert.
          </p>
          <div className="row">
            <button className="btn-primary" type="submit" disabled={speichern}>
              {speichern ? 'Wird gespeichert …' : 'Speichern'}
            </button>
            <button className="btn-secondary" type="button" onClick={() => setForm(null)}>Abbrechen</button>
          </div>
        </form>
      )}

      {laden ? (
        <p className="empty" role="status">Firmen werden geladen …</p>
      ) : (
        <>
          <section className="partner-picker detail-card">
            <div>
              <p className="helper-overline">Firmenübersicht</p>
              <h2>{firmen.length} Firmen</h2>
            </div>
            <label>
              Firma auswählen
              <select value={firmaId} onChange={(event) => setFirmaId(event.target.value)}>
                {firmen.map((company) => (
                  <option key={company.id} value={company.id}>{company.name}</option>
                ))}
              </select>
            </label>
          </section>

          {firma ? (
            <article className="partner-profile detail-card">
              <div className="partner-profile-heading">
                <div className="partner-logo" aria-label={`Logo-Platzhalter ${initials(firma.name)}`}>
                  {initials(firma.name)}
                </div>
                <div>
                  <p className="helper-overline">
                    {firma.partner ? 'Partnerfirma' : firma.initiativeListed ? 'Initiativbewerbung' : 'Firma'}
                  </p>
                  <h2>{firma.name}</h2>
                  <p className="firma">{firma.standort}</p>
                </div>
              </div>
              <p className="detail-text">{firma.beschreibung}</p>
              <section className="partner-specialties" aria-labelledby="partner-specialties-title">
                <h3 id="partner-specialties-title">IT-Schwerpunkte</h3>
                <div className="tags">
                  {firma.spezialitaeten.map((specialty) => <span className="tag" key={specialty}>{specialty}</span>)}
                </div>
              </section>
              <dl className="meta partner-contact-meta">
                {firma.email && <div><dt>E-Mail</dt><dd><a href={`mailto:${firma.email}`}>{firma.email}</a></dd></div>}
                {firma.contactPerson && <div><dt>Kontaktperson</dt><dd>{firma.contactPerson}</dd></div>}
                {firma.telefon && <div><dt>Telefon</dt><dd><a href={`tel:${firma.telefon.replace(/\s/g, '')}`}>{firma.telefon}</a></dd></div>}
                {firma.website && <div><dt>Website</dt><dd><a href={firma.website} target="_blank" rel="noreferrer">{firma.website.replace(/^https?:\/\//, '')}</a></dd></div>}
                {firma.internshipUrl && <div><dt>Praktikumsstellen</dt><dd><a href={firma.internshipUrl} target="_blank" rel="noreferrer">Stellen ansehen</a></dd></div>}
                {firma.logoUrl && <div><dt>Logo-Adresse</dt><dd><a href={firma.logoUrl} target="_blank" rel="noreferrer">Logo ansehen</a></dd></div>}
              </dl>
              <p className="partner-logo-note">Das angezeigte Zeichen ist ein Platzhalter, kein offizielles Firmenlogo.</p>
              <div className="row">
                <button className="btn-secondary" type="button" onClick={() => bearbeiten(firma)}>Bearbeiten</button>
                <button className="btn-secondary" type="button" onClick={() => void loeschen(firma)}>Löschen</button>
              </div>
            </article>
          ) : (
            <p className="empty">Keine Firmen vorhanden.</p>
          )}
        </>
      )}
    </section>
  )
}
