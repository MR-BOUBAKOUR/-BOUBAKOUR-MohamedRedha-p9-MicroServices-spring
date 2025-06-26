<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { setError } from '@/stores/error'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
    const { token, expires, error } = route.query

    if (error) {
        setError(`OAuth2 error: ${error}`)
        router.push('/login')
        return
    }

    if (token && expires) {
        try {
            authStore.token = token
            authStore.tokenExpiry = parseInt(expires) + Date.now()

            // Extract user info from the token
            const payload = JSON.parse(atob(token.split('.')[1]))
            authStore.user = {
                username: payload.username,
                role: payload.role,
                imageUrl: payload.image_url || null, // OAuth2 specific
            }

            authStore.scheduleTokenRefresh(parseInt(expires))
            authStore.isInitialized = true
            authStore.wasLoggedOut = false

            router.push('/patients')
        } catch (e) {
            setError('Error processing OAuth2 callback', e)
            router.push('/login')
        }
    } else {
        setError('Invalid OAuth2 callback')
        router.push('/login')
    }
})
</script>

<template>
    <div>Processing OAuth2 login...</div>
</template>
