<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const credentials = ref({
    username: '',
    password: '',
})

const handleLogin = async () => {
    try {
        await authStore.login(credentials.value)
        router.push('/patients')
    } catch (e) {
        console.warn('Erreur lors de la connexion.')
    }
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
    </main>
</template>

<style>
.login-form {
    width: 300px;
    margin: 2rem auto;
    padding: 2rem;
    border: 1px solid #ccc;
}
</style>
