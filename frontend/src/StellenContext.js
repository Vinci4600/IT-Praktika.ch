import { createContext, useContext } from 'react'

export const StellenContext = createContext(null)

export function useStellen() {
  const value = useContext(StellenContext)
  if (!value) {
    throw new Error('useStellen muss innerhalb von StellenProvider verwendet werden')
  }
  return value
}
