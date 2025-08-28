<script setup>
import { globalError, clearError } from '@/stores/error'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const patientId = computed(() => route.params.patientId)

const showReturnToPatientsLink = computed(() => route.name === 'patient')
const showReturnToPatientLink = computed(() =>
    route.name === 'patient-edit' || route.name === 'assessment-edit'
)

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
        <div v-if="authStore.isInitializing">Loading authentication...</div>

        <template v-else>
            <header>
                <div class="header-content" v-if="route.name !== 'login'">
                    <!-- Logo -->
                    <div class="header-logo">
                        <h1>MediLabo Solutions</h1>
                    </div>

                    <!-- Erreurs -->
                    <div class="header-error-banner">
                        <Transition name="fade">
                            <div v-if="globalError" class="error-message">
                                <p>{{ globalError }}</p>
                                <button @click="clearError">×</button>
                            </div>
                        </Transition>
                    </div>

                    <!-- User info -->
                    <div v-if="authStore.user" class="header-user-banner">
                        <img
                            v-if="authStore.user.imageUrl"
                            :src="authStore.user.imageUrl"
                            alt="Profile"
                            class="profile-image"
                        />
                        <div class="user-meta">
                            <div>{{ authStore.user.username }}</div>
                            <div>{{ authStore.user.role }}</div>
                        </div>
                    </div>

                    <!-- Déconnexion -->
                    <div class="header-logout">
                        <button @click="handleLogout">Déconnexion</button>
                    </div>
                </div>

                <div v-else class="header-error-banner">
                    <Transition name="fade">
                        <div v-if="globalError" class="error-message">
                            <p>{{ globalError }}</p>
                            <button @click="clearError">×</button>
                        </div>
                    </Transition>
                </div>

                <nav>
                    <RouterLink v-if="showReturnToPatientsLink" to="/patients">
                        ◀ Retour à la liste des patients
                    </RouterLink>
                    <RouterLink
                        v-if="showReturnToPatientLink"
                        :to="{ name: 'patient', params: { patientId: patientId } }"
                    >
                        ◀ Retour à la fiche du patient
                    </RouterLink>
                </nav>
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
.header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 1rem;
    min-height: 80px;
}

/* Logo */
.header-logo h1 {
    margin: 0;
}

/* Bannière d’erreur */
.header-error-banner {
    flex: 1;
    display: flex;
    justify-content: center;
}
.error-message {
    font-size: 0.9rem;
    background: #ff4444;
    color: white;
    padding: 0 1rem;
    display: flex;
    max-width: 400px;
    min-height: 40px;
    border-radius: 4px;
    align-items: center;
}
.error-message button {
    max-width: 50px;
    background: none;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
}

/* User info */
.header-user-banner {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}
.profile-image {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
}
.user-meta {
    display: flex;
    flex-direction: column;
    font-size: 0.9rem;
    color: #555;
}

.header-logout {
    margin-left: 1rem;
}
</style>
