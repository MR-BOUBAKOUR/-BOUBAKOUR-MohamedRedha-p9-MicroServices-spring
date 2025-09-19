<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import PatientCard from '@/components/patients/PatientCard.vue'
import NotesList from '@/components/notes/NotesList.vue'
import NoteForm from '@/components/notes/NoteForm.vue'
import AssessmentsList from '@/components/assessments/AssessmentsList.vue'
import { fetchPatientById } from '@/services/patient-service'
import { fetchNotesByPatientId, createNote } from '@/services/note-service'
import {
    downloadAssessmentPdf,
    fetchAssessmentsByPatientId,
    queueAssessmentByPatientId,
    refuseAssessment,
} from '@/services/assessment-service'

const route = useRoute()
const patientId = Number(route.params.patientId)

const patient = ref()

// --- Notes pagination ---
const notes = ref([])
const notesTotalPages = ref(0)
const notesCurrentPage = ref(0)
const notesIsFirstPage = ref(true)
const notesIsLastPage = ref(false)

// --- Assessments pagination ---
const assessments = ref([])
const assessmentsTotalPages = ref(0)
const assessmentsCurrentPage = ref(0)
const assessmentsIsFirstPage = ref(true)
const assessmentsIsLastPage = ref(false)

let eventSource = null
let progressInterval = null

const sseStates = {}

// --- Dernier assessment confirmé ---
const lastConfirmedAssessment = computed(() => {
    return (
        [...assessments.value]
            .filter((a) => ['ACCEPTED', 'UPDATED', 'MANUAL'].includes(a.status))
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0] || null
    )
})

// --- Y a-t-il une évaluation en attente ? ---
const hasWaitingAssessment = computed(() =>
    assessments.value.some((a) =>
        ['QUEUED', 'PROCESSING', 'PENDING', 'REFUSED_PENDING'].includes(a.status),
    ),
)

const lastLevelIconAssessment = computed(() => {
    if (!lastConfirmedAssessment.value) return null
    switch (lastConfirmedAssessment.value.level) {
        case 'VERY_LOW':
            return '/icons/risk_very_low.svg'
        case 'LOW':
            return '/icons/risk_low.svg'
        case 'MODERATE':
            return '/icons/risk_moderate.svg'
        case 'HIGH':
            return '/icons/risk_high.svg'
        default:
            return null
    }
})

// --- Charger une page de notes ---
async function loadNotes(page = 0, size = 3) {
    const result = await fetchNotesByPatientId(patientId, page, size)
    notes.value = result.content || []
    notesTotalPages.value = result.totalPages || 0
    notesCurrentPage.value = result.number || 0
    notesIsFirstPage.value = result.first || false
    notesIsLastPage.value = result.last || false
}

// --- Charger une page d'assessments ---
async function loadAssessments(page = 0, size = 3) {
    const result = await fetchAssessmentsByPatientId(patientId, page, size)
    assessments.value = normalizeAssessments(result.content || [])
    assessmentsTotalPages.value = result.totalPages || 0
    assessmentsCurrentPage.value = result.number || 0
    assessmentsIsFirstPage.value = result.first || false
    assessmentsIsLastPage.value = result.last || false
}

// --- Helper pour injecter "en attente" ou "en cours" ---
function normalizeAssessments(list) {
    return list.map((a) => {
        if (a.status === 'QUEUED') {
            return {
                ...a,
                progress: 0,
                progressMessage: 'Évaluation en attente',
            }
        }
        if (a.status === 'PROCESSING') {
            if (sseStates[a.id]) {
                // On a déjà l'état SSE → pas besoin de simulation
                return {
                    ...a,
                    progress: sseStates[a.id].progress,
                    progressMessage: sseStates[a.id].progressMessage,
                    _simulateProgress: false,
                }
            } else {
                // Aucun état SSE → on simule la progression
                return {
                    ...a,
                    progress: 0,
                    progressMessage: 'Traitement en cours',
                    _simulateProgress: true,
                }
            }
        }
        return a
    })
}

onMounted(async () => {
    // --- Chargement initial ---
    patient.value = await fetchPatientById(patientId)
    await loadNotes()
    await loadAssessments()

    // --- SSE subscription ---
    eventSource = new EventSource(
        `${import.meta.env.VITE_GATEWAY_URL}/v1/assessments/sse/${patientId}`,
        { withCredentials: true },
    )

    eventSource.addEventListener('assessment-progress', (e) => {
        const data = JSON.parse(e.data)
        console.log('Progress update:', data)

        sseStates[data.assessmentId] = {
            progress: data.progress,
            progressMessage: data.message,
        }

        const index = assessments.value.findIndex((a) => a.id === data.assessmentId)
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

        const index = assessments.value.findIndex((a) => a.id === dto.id)
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
        assessments.value.forEach((a) => {
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

const handleDownloadLast = async () => {
    if (lastConfirmedAssessment.value) {
        await downloadAssessmentPdf(lastConfirmedAssessment.value.id)
    }
}

// --- Notes / Assessment actions ---

async function handleNoteCreate(note) {
    const newNote = { patId: patient.value.id, patient: patient.value.firstName, ...note }

    await createNote(newNote)

    await queueAssessmentByPatientId(patientId)
    await loadNotes()
    await loadAssessments()
}

async function handleAssessmentAccepted() {
    await loadAssessments()
}

async function handleAssessmentReload(assessment) {
    await refuseAssessment(assessment.id)
    await queueAssessmentByPatientId(assessment.patId)
    await loadAssessments()
}
</script>

<template>
    <main>
        <section class="infos-section">
            <!-- Toujours présent -->
            <div class="infos-block patient-block">
                <PatientCard v-if="patient" :patient="patient" />
            </div>

            <!-- Dernier risque confirmé -->
            <div v-if="lastConfirmedAssessment" class="infos-block">
                <h3>Dernier risque confirmé</h3>
                <div class="last-risk">
                    <img :src="lastLevelIconAssessment" alt="level" width="120" height="120" />
                </div>
            </div>

            <!-- Télécharger -->
            <div v-if="lastConfirmedAssessment" class="infos-block">
                <h3>Dernière évaluation validée</h3>
                <div>
                    <button class="button-download" @click="handleDownloadLast">Télécharger</button>
                </div>
            </div>

            <!-- Évaluation en attente -->
            <div v-if="hasWaitingAssessment" class="infos-block">
                <h3>Évaluation en attente</h3>
                <div class="evaluation-icon">
                    <img
                        src="/icons/evaluation_todo.svg"
                        alt="Évaluation à faire"
                        width="120"
                        height="120"
                    />
                </div>
            </div>
        </section>

        <section class="notes-section">
            <h2>Notes médicales</h2>

            <div class="notes-container">
                <!-- Formulaire à gauche -->
                <div class="note-form">
                    <NoteForm @submit="handleNoteCreate" />
                </div>

                <!-- Liste des notes à droite -->
                <div class="notes-list-wrapper">
                    <NotesList class="notes-list" :notes="notes" />

                    <div class="pagination" v-if="notesTotalPages > 1">
                        <button
                            :disabled="notesIsFirstPage"
                            @click="loadNotes(notesCurrentPage - 1)"
                        >
                            Précédent
                        </button>
                        <span>{{ notesCurrentPage + 1 }} / {{ notesTotalPages }}</span>
                        <button
                            :disabled="notesIsLastPage"
                            @click="loadNotes(notesCurrentPage + 1)"
                        >
                            Suivant
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <section class="assessments-section">
            <h2>Évaluations médicales</h2>

            <AssessmentsList
                @accepted="handleAssessmentAccepted"
                @reload="handleAssessmentReload"
                :assessments="assessments"
            />
            <div class="pagination" v-if="assessmentsTotalPages > 1">
                <button
                    :disabled="assessmentsIsFirstPage"
                    @click="loadAssessments(assessmentsCurrentPage - 1)"
                >
                    Précédent
                </button>
                <span>{{ assessmentsCurrentPage + 1 }} / {{ assessmentsTotalPages }}</span>
                <button
                    :disabled="assessmentsIsLastPage"
                    @click="loadAssessments(assessmentsCurrentPage + 1)"
                >
                    Suivant
                </button>
            </div>
        </section>
    </main>
</template>

<style scoped>
.infos-section,
.notes-section,
.assessments-section {
    margin-top: 2rem;
}

.notes-section h2,
.assessments-section h2 {
    margin-bottom: 1rem;
    font-size: 1.5rem;
}

.notes-container {
    display: flex;
    gap: 2rem;
    align-items: flex-start;
}

.note-form {
    flex: 1 1 0;
}

.notes-list-wrapper {
    flex: 1 1 0;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    margin: 1rem 0;
}

.assessments-section {
    display: flex;
    flex-direction: column;
}

/* Infos section horizontale homogène */
.infos-section {
    display: flex;
    gap: 2rem;
    align-items: stretch;
}

/* Chaque bloc */
.infos-block {
    flex: 1;
    border: 1px solid #ccc;
    padding: 1rem;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
}

/* PatientCard = plus large (≈40%) */
.patient-block {
    flex: 2;
}

/* Titres des blocs (sauf PatientCard) */
.infos-block h3 {
    margin: 0;
    font-size: 0.9rem;
    font-weight: bold;
    text-align: center;
    border-bottom: 1px solid #ddd;
    width: 100%;
    align-self: flex-start;
}

.infos-block:not(.patient-block) > *:not(h3) {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.button-download {
    border-radius: 4px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
    color: white;
    border: none;
    transition: background-color 0.2s;
    background-color: #4caf50;
    width: 120px;
    height: 60px;
}

.button-download:hover {
    opacity: 0.9;
}
</style>
