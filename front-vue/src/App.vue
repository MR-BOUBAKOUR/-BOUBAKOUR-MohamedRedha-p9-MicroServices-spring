<script setup>
import { useRoute, RouterView } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const patientId = computed(() => route.params.id)

const showLayout = computed(() => route.name !== 'login')
const showReturnToPatientsLink = computed(() => route.name === 'patient')
const showReturnToPatientLink = computed(() => route.name === 'patient-edit')
</script>

<template>
    <div class="conteneur">
        <header v-if="showLayout">
            <h1>MediLabo Solutions</h1>
            <RouterLink v-if="showReturnToPatientsLink" to="/patients">
                Retour à la liste des patients
            </RouterLink>
            <RouterLink
                v-if="showReturnToPatientLink"
                :to="{ name: 'patient', params: { id: patientId } }"
            >
                Retour à la fiche du patient
            </RouterLink>
        </header>

        <RouterView />
    </div>
</template>
