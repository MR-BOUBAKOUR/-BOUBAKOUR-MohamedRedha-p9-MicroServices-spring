<script setup>
import { globalError, clearError } from '@/stores/error'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const patientId = computed(() => route.params.id)

const isLoginPage = computed(() => route.name !== 'login')
const showReturnToPatientsLink = computed(() => route.name === 'patient')
const showReturnToPatientLink = computed(() => route.name === 'patient-edit')

const handleLogout = async () => {
    await authStore.logout()
    router.push('/login')
}

watch(
    () => router.currentRoute.value.path,
    () => {
        clearError()
    },
)
</script>

<template>
    <div class="conteneur">
        <div v-if="authStore.isInitializing" class="loader">Loading authentication...</div>

        <template v-else>
            <header :class="{ 'header-empty': !isLoginPage }">
                <template v-if="isLoginPage">
                    <div class="header-content">
                        <h1>MediLabo Solutions</h1>
                        <Transition name="fade">
                            <div v-if="globalError" class="header-error-banner">
                                <p>{{ globalError }}</p>
                                <button @click="clearError">×</button>
                            </div>
                        </Transition>
                        <div class="header-info-logout">
                            <span v-if="authStore.user" class="user-info">
                                {{ authStore.user.username }}
                                {{ authStore.user.role }}
                            </span>
                            <button @click="handleLogout">Déconnexion</button>
                        </div>
                    </div>
                    <nav>
                        <RouterLink v-if="showReturnToPatientsLink" to="/patients">
                        ◀ Retour à la liste des patients
                        </RouterLink>
                        <RouterLink
                            v-if="showReturnToPatientLink"
                            :to="{ name: 'patient', params: { id: patientId } }"
                            >
                            ◀ Retour à la fiche du patient
                        </RouterLink>
                    </nav>
                </template>
            </header>
            
            <RouterView v-slot="{ Component }">
                <Transition name="fade" mode="out-in">
                    <component :is="Component" :key="$route.fullPath" />
                </Transition>
            </RouterView>
        </template>
    </div>
</template>


<style scoped>
.loader {
    text-align: center;
    margin: 2rem;
    font-size: 1.2rem;
    color: #666;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-info-logout {
    display: flex;
    align-items: center;
    gap: 2rem;
}

.user-info {
    font-size: 0.9rem;
    color: #666;
}

.header-error-banner {
    background: #ff4444;
    color: white;
    padding: 0 1rem;
    display: flex;
    max-width: 400px;
    min-height: 50px;
    border-radius: 4px;
}

.header-error-banner button {
    max-width: 50px;
    background: none;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
}
</style>
