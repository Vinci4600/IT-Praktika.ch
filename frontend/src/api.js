const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function apiFetch(path, { accessToken, headers: requestHeaders, ...options } = {}) {
  const headers = new Headers(requestHeaders)

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  const text = await response.text()
  let result = null

  if (text) {
    try {
      result = JSON.parse(text)
    } catch (error) {
      if (response.ok) {
        throw new Error('Die API hat eine ungültige Antwort zurückgegeben.', { cause: error })
      }
    }
  }

  if (!response.ok) {
    throw new Error(
      result?.error?.message ?? `Die Anfrage ist fehlgeschlagen (HTTP ${response.status}).`,
    )
  }

  return result
}
