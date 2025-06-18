import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// Adds auth token to the requests and handles 401 errors globally by logging out and redirecting to login
export function setupAxiosInterceptors(apiInstance) {
    apiInstance.interceptors.request.use(
        // 'config' is the request object containing all HTTP request info
        // (URL, method, headers, data, etc.)
        // Here we add the auth token to the headers before the request is sent to the server
        (config) => {
            const authStore = useAuthStore()
            if (authStore.token) {
                config.headers.Authorization = `Bearer ${authStore.token}`
            }
            return config
        },
        (error) => {
            // Propagate the error to the caller by rejecting the Promise
            // If we don't do this, Axios will treat the response as successful,
            // and the error won't be caught in the calling code's catch block
            return Promise.reject(error)
        },
    )

    apiInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            if (error.response?.status === 401) {
                const authStore = useAuthStore()

                // Avoid mutliple logouts
                if (!authStore.isLoggingOut) {
                    await authStore.logout(true) // Sient logout
                    router.push('/login')
                }
            }
            return Promise.reject(error)
        },
    )
}
