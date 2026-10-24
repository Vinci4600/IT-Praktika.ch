import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import { apiFetch } from '../../api'

const emptyForm = {
  firmaId: '',
  firma: '',
  titel: '',
  standort: '',
  dauer: '',
  startOption: 'BY_AGREEMENT',
  startDatum: '',
  beruf: 'ICT Fachmann',
  ansprechperson: '',
  email: '',
  telefon: '',
  bewerbungsUrl: '',
  entlohnung: 'nicht angegeben',
  beschreibung: '',
  anforderungen: '',
  status: 'DRAFT',
}

function fromApi(stelle) {
  return {
    ...stelle,
    startOption: stelle.startOption.toUpperCase(),
    startDatum: stelle.startDatum ?? '',
    ansprechperson: stelle.ansprechperson ?? '',
    telefon: stelle.telefon ?? '',
    bewerbungsUrl: stelle.bewerbungsUrl ?? '',
    status: stelle.status.toUpperCase(),
    firmaId: stelle.firmaId ?? '',
    anforderungen: stelle.anforderungen.join('\n'),
  }
}

function toRequest(form) {
  return {
    ...form,
    firmaId: form.firmaId || null,
    startDatum: form.startOption === 'DATE' ? form.startDatum : null,
    ansprechperson: form.ansprechperson || null,
    telefon: form.telefon || null,
    bewerbungsUrl: form.bewerbungsUrl || null,
    anforderungen: form.anforderungen
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean),
  }
}

async function fetchAdminData(accessToken) {
  const [stellenResult, firmenResult] = await Promise.all([
    apiFetch('/api/v1/admin/stellen', { accessToken }),
    apiFetch('/api/v1/firmen'),
  ])
  if (!Array.isArray(stellenResult) || !Array.isArray(firmenResult?.data)) {
    throw new Error('Die API hat ungültige Verwaltungsdaten zurückgegeben.')
  }
  return { stellen: stellenResult, firmen: firmenResult.data }
}

export default function AdminStellen() {
  const { accessToken } = useAuth()
  const [stellen, setStellen] = useState([])
  const [firmen, setFirmen] = useState([])
  const [form, setForm] = useState(null)
  const [fehler, setFehler] = useState('')
  const [laden, setLaden] = useState(true)
  const [wirdGespeichert, setWirdGespeichert] = useState(false)

  const ladeDaten = useCallback(async (showLoading = true) => {
    if (showLoading) {
      setFehler('')
      setLaden(true)
    }
    try {
      const result = await fetchAdminData(accessToken)
      setStellen(result.stellen)
      setFirmen(result.firmen)
    } catch (error) {
      setFehler(error.message)
    } finally {
      setLaden(false)
    }
  }, [accessToken])

  useEffect(() => {
    let cancelled = false
    fetchAdminData(accessToken)
      .then((result) => {
        if (!cancelled) {
          setStellen(result.stellen)
          setFirmen(result.firmen)
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

  function aendern(name, value) {
    setForm((current) => ({ ...current, [name]: value }))
  }

  function firmaWaehlen(firmaId) {
    const firma = firmen.find((item) => item.id === firmaId)
    setForm((current) => ({
      ...current,
      firmaId,
      firma: firma?.name ?? current.firma,
      standort: firma?.standort ?? current.standort,
      email: firma?.email ?? current.email,
      telefon: firma?.telefon ?? current.telefon,
    }))
  }

  async function speichern(event) {
    event.preventDefault()
    setFehler('')
    setWirdGespeichert(true)
    try {
      const request = toRequest(form)
      const editing = Boolean(form.id)
      await apiFetch(
        editing ? `/api/v1/admin/stellen/${form.id}` : '/api/v1/admin/stellen',
        {
          method: editing ? 'PUT' : 'POST',
          body: JSON.stringify(request),
          accessToken,
        },
      )
      setForm(null)
      await ladeDaten()
    } catch (error) {
      setFehler(error.message)
    } finally {
      setWirdGespeichert(false)
    }
  }

  async function loeschen(stelle) {
    if (!window.confirm(`Soll "${stelle.titel}" wirklich gelöscht werden?`)) return
    setFehler('')
    try {
      await apiFetch(`/api/v1/admin/stellen/${stelle.id}`, {
        method: 'DELETE',
        accessToken,
      })
      await ladeDaten()
    } catch (error) {
      setFehler(error.message)
    }
  }

  function bearbeiten(stelle) {
    setForm(fromApi(stelle))
    setFehler('')
  }

  return (
    <section className="admin-page">
      <div className="admin-page-heading">
        <div>
          <p className="helper-overline">Verwaltung</p>
          <h2>Stellen</h2>
          <p>Praktikumsstellen erstellen, bearbeiten und veröffentlichen.</p>
        </div>
        <button className="btn-primary" type="button" onClick={() => setForm({ ...emptyForm })}>
          Neue Stelle
        </button>
      </div>

      {fehler && <p className="login-error" role="alert">{fehler}</p>}

      {form && (
        <form className="detail-card form" onSubmit={speichern}>
          <h3>{form.id ? 'Stelle bearbeiten' : 'Neue Stelle'}</h3>
          <label>
            Partnerfirma (optional)
            <select value={form.firmaId} onChange={(event) => firmaWaehlen(event.target.value)}>
              <option value="">Keine verknüpfte Firma</option>
              {firmen.map((firma) => <option key={firma.id} value={firma.id}>{firma.name}</option>)}
            </select>
          </label>
          <label>
            Firmenname
            <input required maxLength={200} value={form.firma} onChange={(event) => aendern('firma', event.target.value)} />
          </label>
          <div className="row">
            <label>
              Titel
              <input required maxLength={200} value={form.titel} onChange={(event) => aendern('titel', event.target.value)} />
            </label>
            <label>
              Standort
              <input required maxLength={160} value={form.standort} onChange={(event) => aendern('standort', event.target.value)} />
            </label>
          </div>
          <div className="row">
            <label>
              Dauer
              <input required maxLength={100} value={form.dauer} onChange={(event) => aendern('dauer', event.target.value)} />
            </label>
            <label>
              Beruf
              <input required maxLength={160} value={form.beruf} onChange={(event) => aendern('beruf', event.target.value)} />
            </label>
          </div>
          <div className="row">
            <label>
              Start
              <select value={form.startOption} onChange={(event) => aendern('startOption', event.target.value)}>
                <option value="IMMEDIATE">Ab sofort</option>
                <option value="BY_AGREEMENT">Nach Vereinbarung</option>
                <option value="DATE">Konkretes Datum</option>
              </select>
            </label>
            {form.startOption === 'DATE' && (
              <label>
                Startdatum
                <input type="date" required value={form.startDatum} onChange={(event) => aendern('startDatum', event.target.value)} />
              </label>
            )}
          </div>
          <div className="row">
            <label>
              Kontakt-E-Mail
              <input type="email" required maxLength={254} value={form.email} onChange={(event) => aendern('email', event.target.value)} />
            </label>
            <label>
              Telefon (optional)
              <input maxLength={40} value={form.telefon} onChange={(event) => aendern('telefon', event.target.value)} />
            </label>
          </div>
          <div className="row">
            <label>
              Ansprechperson (optional)
              <input maxLength={200} value={form.ansprechperson} onChange={(event) => aendern('ansprechperson', event.target.value)} />
            </label>
            <label>
              Bewerbungslink (optional)
              <input type="url" maxLength={2048} value={form.bewerbungsUrl} onChange={(event) => aendern('bewerbungsUrl', event.target.value)} />
            </label>
          </div>
          <label>
            Entlöhnung
            <input required maxLength={160} value={form.entlohnung} onChange={(event) => aendern('entlohnung', event.target.value)} />
          </label>
          <label>
            Beschreibung
            <textarea required maxLength={20000} rows={4} value={form.beschreibung} onChange={(event) => aendern('beschreibung', event.target.value)} />
          </label>
          <label>
            Anforderungen (eine pro Zeile)
            <textarea required rows={4} value={form.anforderungen} onChange={(event) => aendern('anforderungen', event.target.value)} />
          </label>
          <label>
            Status
            <select value={form.status} onChange={(event) => aendern('status', event.target.value)}>
              <option value="DRAFT">Entwurf</option>
              <option value="PUBLISHED">Veröffentlicht</option>
              <option value="ARCHIVED">Archiviert</option>
            </select>
          </label>
          <div className="row">
            <button className="btn-primary" type="submit" disabled={wirdGespeichert}>
              {wirdGespeichert ? 'Wird gespeichert …' : 'Speichern'}
            </button>
            <button className="btn-secondary" type="button" onClick={() => setForm(null)}>
              Abbrechen
            </button>
          </div>
        </form>
      )}

      {laden ? (
        <p className="empty" role="status">Stellen werden geladen …</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th scope="col">Stelle</th>
                <th scope="col">Firma</th>
                <th scope="col">Beruf</th>
                <th scope="col">Status</th>
                <th scope="col">Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {stellen.map((stelle) => (
                <tr key={stelle.id}>
                  <td data-label="Stelle"><strong>{stelle.titel}</strong></td>
                  <td data-label="Firma">{stelle.firma}</td>
                  <td data-label="Beruf">{stelle.beruf}</td>
                  <td data-label="Status">{stelle.status}</td>
                  <td data-label="Aktionen">
                    {stelle.status === 'published' && <Link to={`/stelle/${stelle.id}`}>Öffnen</Link>}{' '}
                    <button className="btn-secondary" type="button" onClick={() => bearbeiten(stelle)}>Bearbeiten</button>{' '}
                    <button className="btn-secondary" type="button" onClick={() => void loeschen(stelle)}>Löschen</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!stellen.length && <p className="empty">Es sind noch keine Stellen vorhanden.</p>}
        </div>
      )}
    </section>
  )
}
