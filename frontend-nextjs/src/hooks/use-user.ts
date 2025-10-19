"use client"

import { useState, useEffect } from "react"

const STORAGE_KEY = "household-user"

export function useUser() {
  const [username, setUsername] = useState<string>("")
  const [isLoaded, setIsLoaded] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      setUsername(stored)
    }
    setIsLoaded(true)
  }, [])

  const saveUsername = (name: string) => {
    setUsername(name)
    localStorage.setItem(STORAGE_KEY, name)
  }

  const logout = () => {
    setUsername("")
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    username,
    saveUsername,
    logout,
    isLoaded,
  }
}
