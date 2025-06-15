<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PatientForm from '@/components/patients/PatientForm.vue'
import { fetchPatientById, updatePatient } from '@/services/patient-service'

const route = useRoute()
const patientId = Number(route.params.id)
const router = useRouter()

const patient = ref(null)

onMounted(async () => {
    patient.value = await fetchPatientById(patientId)
})

async function handlePatientUpdate(updatedPatient) {
    await updatePatient(patientId, updatedPatient)
    router.push({ name: 'patient', params: { id: patientId } })
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
