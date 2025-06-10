<script setup>
import { globalError, clearError } from '@/stores/error'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { computed, watch } from 'vue'

const route = useRoute()
const router = useRouter()
const patientId = computed(() => route.params.id)

const showLayout = computed(() => route.name !== 'login')
const showReturnToPatientsLink = computed(() => route.name === 'patient')
const showReturnToPatientLink = computed(() => route.name === 'patient-edit')

watch(() => router.currentRoute.value.path, () => {
  clearError()
})
</script>

<template>
    <div class="conteneur">
        <header v-if="showLayout">
            <h1>MediLabo Solutions</h1>
            <RouterLink v-if="showReturnToPatientsLink" to="/patients">
                ◀ Retour à la liste des patients
            </RouterLink>
            <RouterLink
                v-if="showReturnToPatientLink"
                :to="{ name: 'patient', params: { id: patientId } }"
            >
                ◀ Retour à la fiche du patient
            </RouterLink>
        </header>

        <div v-if="globalError" class="error-banner">
            {{ globalError }}
            <button @click="clearError">×</button>
        </div>

        <RouterView />
    </div>
</template>

<style scoped>
.error-banner {
    background: #ff4444;
    color: white;
    padding: 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.error-banner button {
    background: none;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
}
</style>