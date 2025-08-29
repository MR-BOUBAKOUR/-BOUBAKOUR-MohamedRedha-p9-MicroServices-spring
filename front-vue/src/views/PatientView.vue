<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import PatientCard from '@/components/patients/PatientCard.vue'
import NotesList from '@/components/notes/NotesList.vue'
import NoteForm from '@/components/notes/NoteForm.vue'
import AssessmentsList from '@/components/assessments/AssessmentsList.vue'
import { fetchPatientById } from '@/services/patient-service'
import { fetchNotesByPatientId, createNote } from '@/services/note-service'
import {
    fetchAssessmentsByPatientId,
    generateAssessmentByPatientId,
    refuseAssessment,
} from '@/services/assessment-service'

const route = useRoute()
const patientId = Number(route.params.patientId)

const patient = ref()
const notes = ref([])
const assessments = ref([])

const isAssessmentLoading = ref(false)

onMounted(async () => {
    patient.value = await fetchPatientById(patientId)
    notes.value = await fetchNotesByPatientId(patientId)
    assessments.value = await fetchAssessmentsByPatientId(patientId)
})

async function handleNoteCreate(note) {
    try {
        const newNote = {
            patId: patient.value.id,
            patient: patient.value.firstName,
            ...note,
        }

        const createdNote = await createNote(newNote)
        notes.value.push(createdNote)

        isAssessmentLoading.value = true
        await generateAssessmentByPatientId(patientId)

        assessments.value = await fetchAssessmentsByPatientId(patientId)
    } catch (e) {
        console.warn('Erreur lors de la création de la note.')
    } finally {
        isAssessmentLoading.value = false
    }
}

async function handleAssessmentReload(assessment) {
    try {
        isAssessmentLoading.value = true
        // update the final status of the previous assessment to "REFUSED"
        await refuseAssessment(assessment.id)
        // generate a new assessment
        await generateAssessmentByPatientId(assessment.patId)

        assessments.value = await fetchAssessmentsByPatientId(patientId)
    } finally {
        isAssessmentLoading.value = false
    }
}
</script>

<template>
    <main>
        <PatientCard v-if="patient" :patient="patient" />
        <NotesList :notes="notes" />
        <NoteForm @submit="handleNoteCreate" />
        <AssessmentsList
            @reload="handleAssessmentReload"
            :assessments="assessments"
            :loading="isAssessmentLoading"
        />
    </main>
</template>
