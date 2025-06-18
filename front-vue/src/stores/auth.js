import axios from 'axios'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { setError } from './error'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(null)
    const tokenExpiry = ref(null)
    const refreshTimer = ref(null)

    const isInitializing = ref(false)
    const isInitialized = ref(false)
    const wasLoggedOut = ref(false)

    const isAuthenticated = computed(() => !!token.value)

    const timeToExpiry = computed(() => {
        if (!tokenExpiry.value) return 0
        return Math.max(0, (tokenExpiry.value - Date.now()) / 1000 / 60)
    })

    const initAuth = async () => {
        // Avoid multiple concurrent calls, skip if already initialized or after a logout
        if (isInitializing.value || isInitialized.value || wasLoggedOut.value) return false

        isInitializing.value = true

        try {
            // Try to refresh the access token using the refresh token stored in the HttpOnly cookie
            const success = await refreshToken()

            if (success) {
                console.log('Session restored via refresh token')
                return true
            } else {
                console.log('No active session to restore')
                return false
            }
        } catch (error) {
            setError('Error restoring session: ' + (error.message || error))
            return false
        } finally {
            isInitializing.value = false
            isInitialized.value = true
        }
    }

    const login = async (credentials) => {
        try {
            const authApi = axios.create({
                baseURL: 'http://localhost:8071',
                timeout: 10000,
                headers: {
                    'Content-Type': 'application/json',
                },
                // Enable the sending of the cookies to the backend domain
                withCredentials: true,
            })

            const response = await authApi.post('/login', credentials)
            const { accessToken, expiresIn } = response.data

            // Store the token and his expiry in reactive states
            token.value = accessToken
            tokenExpiry.value = expiresIn + Date.now()

            // Extract the user info from the token and set the user state
            const payload = JSON.parse(atob(accessToken.split('.')[1]))
            user.value = {
                username: payload.username,
                role: payload.role,
            }

            scheduleTokenRefresh(expiresIn)

            // update the states
            isInitialized.value = true
            wasLoggedOut.value = false
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Invalid credentials')
            } else {
                setError('Connection error')
            }
        }
    }

    const logout = async (silent = false) => {
        try {
            if (refreshTimer.value) {
                clearTimeout(refreshTimer.value)
                refreshTimer.value = null
            }

            const authApi = axios.create({
                baseURL: 'http://localhost:8071',
                withCredentials: true,
            })
            await authApi.post('/logout')
        } catch (error) {
            if (!silent && error.response?.status !== 401) {
                setError('Error during logout')
            }
        } finally {
            user.value = null
            token.value = null
            tokenExpiry.value = null
            isInitialized.value = false
            wasLoggedOut.value = true
        }
    }

    const refreshToken = async () => {
        try {
            const authApi = axios.create({
                baseURL: 'http://localhost:8071',
                timeout: 10000,
                // No Authorization header needed: refresh is handled via HttpOnly cookie
                // Enable the sending of the refreshToken cookie to the backend domain
                withCredentials: true,
            })

            const response = await authApi.post('/refresh')
            const { accessToken, expiresIn } = response.data

            token.value = accessToken
            tokenExpiry.value = expiresIn + Date.now()

            const payload = JSON.parse(atob(accessToken.split('.')[1]))
            user.value = {
                username: payload.username,
                role: payload.role,
            }

            // We trigger a new refreshTimer (if it reachs under 2 minutes, we come back here)
            scheduleTokenRefresh(expiresIn)
            return true

            // eslint-disable-next-line no-unused-vars
        } catch (error) {
            // If refresh fails, logout silently to clean state
            logout(true)
            return false
        }
    }

    const scheduleTokenRefresh = (expiresInMs) => {
        // Clear any existing refresh timer to avoid multiple triggers
        if (refreshTimer.value) {
            clearTimeout(refreshTimer.value)
            refreshTimer.value = null
        }

        // Schedule the refresh to occur 2 minutes before token expiry
        // (AFTER 13 MINUTES = 15 minutes - 2 minutes)
        const delayBeforeRefresh = expiresInMs - 120000

        refreshTimer.value = setTimeout(() => {
            refreshToken()
        }, delayBeforeRefresh)
    }

    return {
        token,
        user,
        isAuthenticated,
        isInitializing,
        isInitialized,
        wasLoggedOut,
        timeToExpiry,
        initAuth,
        login,
        logout,
    }
})
