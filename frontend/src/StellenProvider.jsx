import { useEffect, useState } from 'react'
import { apiFetch } from './api'
import { useAuth } from './AuthContext'
import { StellenContext } from './StellenContext'

function fromApi(stelle) {
  const startdatum = stelle.startOption === 'immediate'
    ? 'Ab sofort'
    : stelle.startOption === 'date'
      ? stelle.startDatum
      : 'Nach Vereinbarung'

  return { ...stelle, startdatum }
}

async function fetchStellen() {
  const result = await apiFetch('/api/v1/stellen?size=100')
  if (!Array.isArray(result?.data)) {
    throw new Error('Die API hat eine ungültige Stellenliste zurückgegeben.')
  }
  return result.data.map(fromApi)
}

export default function StellenProvider({ children }) {
  const { user, accessToken } = useAuth()
  const [stellen, setStellen] = useState([])
  const [stellenLaden, setStellenLaden] = useState(true)
  const [stellenFehler, setStellenFehler] = useState('')
  const [merkliste, setMerkliste] = useState([])
  const [merklisteOwner, setMerklisteOwner] = useState('')
  const [merklisteLoadError, setMerklisteLoadError] = useState({ email: '', message: '' })

  async function ladeStellen() {
    setStellenLaden(true)
    setStellenFehler('')
    try {
      setStellen(await fetchStellen())
    } catch (error) {
      setStellenFehler(error.message)
    } finally {
      setStellenLaden(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    fetchStellen()
      .then((result) => {
        if (!cancelled) setStellen(result)
      })
      .catch((error) => {
        if (!cancelled) setStellenFehler(error.message)
      })
      .finally(() => {
        if (!cancelled) setStellenLaden(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    if (!accessToken || user?.role !== 'student') {
      return () => {
        cancelled = true
      }
    }

    apiFetch('/api/v1/me/merkliste', { accessToken })
      .then((result) => {
        if (!Array.isArray(result)) {
          throw new Error('Die API hat eine ungültige Merkliste zurückgegeben.')
        }
        if (!cancelled) {
          setMerkliste(result)
          setMerklisteOwner(user.email)
          setMerklisteLoadError({ email: user.email, message: '' })
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setMerklisteOwner(user.email)
          setMerklisteLoadError({ email: user.email, message: error.message })
        }
      })

    return () => {
      cancelled = true
    }
  }, [accessToken, user?.email, user?.role])

  const eigeneMerkliste = merklisteOwner === user?.email ? merkliste : []
  const merklisteLaden = user?.role === 'student' && merklisteOwner !== user.email
  const merklisteFehler = merklisteLoadError.email === user?.email
    ? merklisteLoadError.message
    : ''
  const gemerkteIds = eigeneMerkliste.map((entry) => entry.stelleId)

  async function toggleMerken(stelleId) {
    if (!accessToken || user?.role !== 'student') {
      throw new Error('Melde dich mit einem Schülerkonto an, um Stellen zu merken.')
    }
    if (merklisteLaden) {
      throw new Error('Deine Merkliste wird noch geladen. Bitte versuche es gleich erneut.')
    }

    const existing = eigeneMerkliste.find((entry) => entry.stelleId === stelleId)
    try {
      if (existing) {
        await apiFetch(`/api/v1/me/merkliste/${existing.id}`, {
          method: 'DELETE',
          accessToken,
        })
        setMerkliste((current) => current.filter((entry) => entry.id !== existing.id))
      } else {
        const created = await apiFetch('/api/v1/me/merkliste', {
          method: 'POST',
          body: JSON.stringify({ stelleId }),
          accessToken,
        })
        setMerkliste((current) => [created, ...current])
      }
    } catch (error) {
      setMerklisteLoadError({ email: user.email, message: error.message })
      throw error
    }
  }

  async function createStelle(request) {
    if (!accessToken || user?.role !== 'admin') {
      throw new Error('Für diese Aktion ist ein Admin-Konto erforderlich.')
    }
    const created = await apiFetch('/api/v1/admin/stellen', {
      method: 'POST',
      body: JSON.stringify(request),
      accessToken,
    })
    setStellen((current) => [fromApi(created), ...current])
    return fromApi(created)
  }

  return (
    <StellenContext.Provider
      value={{
        stellen,
        stellenLaden,
        stellenFehler,
        ladeStellen,
        createStelle,
        gemerkteIds,
        merkliste: eigeneMerkliste,
        merklisteLaden,
        merklisteFehler,
        toggleMerken,
      }}
    >
      {children}
    </StellenContext.Provider>
  )
}
