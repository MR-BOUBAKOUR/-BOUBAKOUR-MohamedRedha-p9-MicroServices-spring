<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { setError } from '@/stores/error'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const credentials = ref({
    username: '',
    password: '',
})

onMounted(() => {
    if (route.query.error === 'oauth2_unknown_user') {
        setError('Google user not authorized, please contact the administrator.')
    }
})

const handleLogin = async () => {
    try {
        await authStore.login(credentials.value)
        router.push('/patients')
    } catch (e) {
        console.warn('Erreur lors de la connexion.', e)
    }
}

const handleGoogleLogin = () => {
    authStore.loginWithGoogle()
}
</script>

<template>
    <main>
        <form @submit.prevent="handleLogin" class="login-form">
            <h2>Connexion</h2>

            <div class="form-group">
                <label for="username">Nom d'utilisateur</label>
                <input id="username" v-model="credentials.username" type="text" required />
            </div>

            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input id="password" v-model="credentials.password" type="password" required />
            </div>

            <button type="submit">Se connecter</button>
        </form>

        <div class="oauth-separator">ou</div>

        <button type="button" @click="handleGoogleLogin" class="google-login-btn">
            Se connecter avec Google
        </button>
    </main>
</template>

<style>
.login-form {
    width: 300px;
    margin: 1.5rem auto;
    padding: 2rem;
    border: 1px solid #ccc;
}

.login-form button  {
    max-width: 300px;
}

.oauth-separator {
    text-align: center;
    margin: 1.5rem auto;
    color: #666;
}

.google-login-btn {
    max-width: 300px;
    display: block;
    margin: 1.5rem auto;
}
</style>
