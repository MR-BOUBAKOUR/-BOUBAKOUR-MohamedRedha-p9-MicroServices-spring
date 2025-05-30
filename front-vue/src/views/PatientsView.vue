<script setup>
import { onMounted, ref } from 'vue'
import PatientsList from '@/components/patients/PatientsList.vue'
import PatientForm from '@/components/patients/PatientForm.vue'
import { createPatient, fetchPatients } from '@/services/patient-service'

const patients = ref([])

onMounted(async () => {
    patients.value = await fetchPatients()
})

async function handlePatientCreate(newPatient) {
    const createdPatient = await createPatient(newPatient)
    patients.value.push(createdPatient)
}
</script>

<template>
    <main>
        <PatientsList :patients="patients" />
        <PatientForm @submit="handlePatientCreate" submitLabel="Ajouter un patient" />
    </main>
</template>
