import { useAuthStore } from '@/stores/auth'
import { setError } from '@/stores/error'
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
        (error) => {
            // Axios automatically parses the HTTP response and exposes
            // the status code in error.response.status
            if (error.response?.status === 401) {
                const authStore = useAuthStore()
                authStore.logout()
                router.push('/login')
                setError('Session expirée, veuillez vous reconnecter')
            }
            return Promise.reject(error)
        },
    )
}
