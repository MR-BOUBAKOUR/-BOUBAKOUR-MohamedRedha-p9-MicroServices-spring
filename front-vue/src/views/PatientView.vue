<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import PatientCard from '@/components/patients/PatientCard.vue'
import NotesCard from '@/components/notes/NotesCard.vue'
import NoteForm from '@/components/notes/NoteForm.vue'
import AssessmentCard from '@/components/assessments/AssessmentCard.vue'
import { fetchPatientById } from '@/services/patient-service'
import { fetchNotesByPatientId, createNote } from '@/services/note-service'
import { fetchAssessmentByPatientId } from '@/services/assessment-service'

const route = useRoute()
const patientId = Number(route.params.id)

const patient = ref()
const notes = ref([])
const assessment = ref()

onMounted(async () => {
    patient.value = await fetchPatientById(patientId)
    notes.value = await fetchNotesByPatientId(patientId)
    assessment.value = await fetchAssessmentByPatientId(patientId)
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

        assessment.value = await fetchAssessmentByPatientId(patientId)
    } catch (e) {
        console.warn('Erreur lors de la création de la note.')
    }
}
</script>

<template>
    <main>
        <PatientCard v-if="patient" :patient="patient" />
        <NotesCard :notes="notes" />
        <NoteForm @submit="handleNoteCreate" />
        <AssessmentCard v-if="assessment" :assessment="assessment" />
    </main>
</template>
