<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import PatientCard from '@/components/patients/PatientCard.vue'
import NotesList from '@/components/notes/NotesList.vue'
import NoteForm from '@/components/notes/NoteForm.vue'
import AssessmentsList from '@/components/assessments/AssessmentsList.vue'
import { fetchPatientById } from '@/services/patient-service'
import { fetchNotesByPatientId, createNote } from '@/services/note-service'
import {
  fetchAssessmentsByPatientId,
  queueAssessmentByPatientId,
  refuseAssessment,
} from '@/services/assessment-service'

const route = useRoute()
const patientId = Number(route.params.patientId)

const patient = ref()
const notes = ref([])
const assessments = ref([])

let eventSource = null
let progressInterval = null

const sseStates = {}

// --- Helper pour injecter "en attente" ou "en cours" ---
function normalizeAssessments(list) {
  return list.map(a => {
    if (a.status === 'QUEUED') {
      return {
        ...a,
        progress: 0,
        progressMessage: 'Évaluation en attente'
      }
    }
    if (a.status === 'PROCESSING') {
      if (sseStates[a.id]) {
        // On a déjà l'état SSE → pas besoin de simulation
        return {
          ...a,
          progress: sseStates[a.id].progress,
          progressMessage: sseStates[a.id].progressMessage,
          _simulateProgress: false
        }
      } else {
        // Aucun état SSE → on simule la progression
        return {
          ...a,
          progress: 0,
          progressMessage: 'Traitement en cours',
          _simulateProgress: true
        }
      }
    }
    return a
  })
}

onMounted(async () => {
  // --- Chargement initial ---
  patient.value = await fetchPatientById(patientId)
  notes.value = await fetchNotesByPatientId(patientId)
  assessments.value = normalizeAssessments(await fetchAssessmentsByPatientId(patientId))

  // --- SSE subscription ---
  eventSource = new EventSource(
      `${import.meta.env.VITE_GATEWAY_URL}/v1/assessments/sse/${patientId}`,
      { withCredentials: true }
  )

  eventSource.addEventListener('assessment-progress', (e) => {
    const data = JSON.parse(e.data)
    console.log('Progress update:', data)

    sseStates[data.assessmentId] = {
      progress: data.progress,
      progressMessage: data.message
    }

    const index = assessments.value.findIndex(a => a.id === data.assessmentId)
    if (index !== -1) {
      assessments.value[index].progress = data.progress
      assessments.value[index].progressMessage = data.message
      assessments.value[index]._simulateProgress = false // stop simulation
    } else {
      assessments.value.push({
        id: data.assessmentId,
        patId: data.patId,
        progress: data.progress,
        progressMessage: data.message,
      })
    }
  })

  eventSource.addEventListener('assessment-generated', (e) => {
    const dto = JSON.parse(e.data)
    console.log('Assessment final reçu:', dto)

    const index = assessments.value.findIndex(a => a.id === dto.id)
    if (index !== -1) {
      assessments.value[index] = dto
    } else {
      assessments.value.push(dto)
    }
  })

  eventSource.onerror = (err) => {
    console.warn('SSE error:', err)
    eventSource.close()
  }

  // --- Interval pour simuler la progression infinie ---
  progressInterval = setInterval(() => {
    assessments.value.forEach(a => {
      if (a._simulateProgress) {
        a.progress += 5
        if (a.progress > 100) a.progress = 0
      }
    })
  }, 200)
})

onBeforeUnmount(() => {
  if (eventSource) {
    console.warn('SSE closed:')
    eventSource.close()
  }
  if (progressInterval) clearInterval(progressInterval)
})

// --- Notes / Assessment actions ---

async function handleNoteCreate(note) {
  const newNote = { patId: patient.value.id, patient: patient.value.firstName, ...note }
  const createdNote = await createNote(newNote)
  notes.value.push(createdNote)

  await queueAssessmentByPatientId(patientId)
  assessments.value = normalizeAssessments(await fetchAssessmentsByPatientId(patientId))
}

async function handleAssessmentReload(assessment) {
  await refuseAssessment(assessment.id)
  await queueAssessmentByPatientId(assessment.patId)

  assessments.value = normalizeAssessments(await fetchAssessmentsByPatientId(patientId))
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
    />
  </main>
</template>
