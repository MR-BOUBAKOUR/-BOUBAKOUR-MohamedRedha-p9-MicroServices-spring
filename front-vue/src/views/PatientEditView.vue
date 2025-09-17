<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PatientForm from '@/components/patients/PatientForm.vue'
import { fetchPatientById, updatePatient } from '@/services/patient-service'

const route = useRoute()
const router = useRouter()

const patientId = Number(route.params.patientId)

const patient = ref(null)

onMounted(async () => {
    patient.value = await fetchPatientById(patientId)
})

async function handlePatientUpdate(updatedPatient) {
    try {
        await updatePatient(patientId, updatedPatient)
        router.push({ name: 'patient', params: { patientId: patientId } })
    } catch (e) {
        console.warn('Erreur lors de la mise à jour du patient.')
    }
}
</script>

<template>
    <main>
        <PatientForm
            v-if="patient"
            :patient="patient"
            @submit="handlePatientUpdate"
            submitLabel="Mettre à jour la fiche du patient"
        />
    </main>
</template>
