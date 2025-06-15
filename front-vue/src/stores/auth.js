import axios from 'axios'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { setError } from './error'

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null)
    const user = ref(null)

    const isAuthenticated = computed(() => !!token.value)

    const login = async (credentials) => {
        try {
            const authApi = axios.create({
                baseURL: 'http://localhost:8071',
                timeout: 10000,
                headers: {
                    'Content-Type': 'application/json',
                },
            })

            const response = await authApi.post('/login', {
                username: credentials.username,
                password: credentials.password,
            })
            const newToken = response.data.token

            // Store the token in reactive state and localStorage
            token.value = newToken
            localStorage.setItem('token', newToken)

            // Extract the user info from the token and set the user state
            const payload = JSON.parse(atob(newToken.split('.')[1]))
            user.value = {
                username: payload.username,
                role: payload.role,
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Invalid credentials')
            } else {
                setError('Connection error')
            }
        }
    }

    const logout = () => {
        token.value = null
        user.value = null
        localStorage.removeItem('token')
    }

    const initAuth = () => {
        const storedToken = localStorage.getItem('token')

        if (storedToken) {
            token.value = storedToken

            try {
                const payload = JSON.parse(atob(storedToken.split('.')[1]))

                if (payload.exp * 1000 > Date.now()) {
                    user.value = {
                        username: payload.username,
                        role: payload.role,
                    }
                } else {
                    logout()
                }
                // eslint-disable-next-line no-unused-vars
            } catch (error) {
                logout()
            }
        }
    }

    return {
        token,
        user,
        isAuthenticated,
        login,
        logout,
        initAuth,
    }
})
