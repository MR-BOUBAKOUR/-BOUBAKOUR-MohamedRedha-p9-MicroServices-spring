<script setup>
import { ref, onMounted } from 'vue'
import PatientsList from '@/components/patients/PatientsList.vue'
import PatientForm from '@/components/patients/PatientForm.vue'
import { fetchPatients, createPatient } from '@/services/patient-service'

const patients = ref([])
const totalPages = ref(0)
const currentPage = ref(0)
const isFirstPage = ref(true)
const isLastPage = ref(false)

async function loadPatients(page = 0) {
    const result = await fetchPatients(page, 10)
    patients.value = result.content || []
    totalPages.value = result.totalPages || 0
    currentPage.value = result.number || 0
    isFirstPage.value = result.first || false
    isLastPage.value = result.last || false
}

onMounted(() => loadPatients())

async function handlePatientCreate(newPatient) {
    try {
        await createPatient(newPatient)
        await loadPatients(0) // recharge la première page pour voir le nouveau patient
    } catch (e) {
        console.warn('Erreur lors de la création du patient.')
    }
}
</script>

<template>
    <main class="patients-page">
        <div class="patients-container">
            <!-- Liste + pagination -->
            <div class="patients-list-section">
                <div class="patients-list">
                    <PatientsList :patients="patients" />
                </div>
                <div class="pagination">
                    <button :disabled="isFirstPage" @click="loadPatients(currentPage - 1)">
                        Précédent
                    </button>
                    <span>{{ currentPage + 1 }} / {{ totalPages }}</span>
                    <button :disabled="isLastPage" @click="loadPatients(currentPage + 1)">
                        Suivant
                    </button>
                </div>
            </div>

            <!-- Formulaire -->
            <div class="patient-form-section">
                <PatientForm @submit="handlePatientCreate" submitLabel="Ajouter un patient" />
            </div>
        </div>
    </main>
</template>

<style scoped>
.patients-page {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    min-height: calc(100vh - 120px);
    box-sizing: border-box;
    overflow: hidden;
}

.patients-container {
    display: flex;
    gap: 5rem;
    width: 100%;
    align-items: flex-start;
}

/* Section Liste */
.patients-list-section {
    flex: 2;
    display: flex;
    flex-direction: column;
    height: 700px;
    justify-content: flex-start;
}

.patients-list {
    height: 500px; /* hauteur fixe pour la table seule */
    width: 100%;
}

/* Pagination */
.pagination {
    height: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    width: 100%;
}

/* Formulaire */
.patient-form-section {
    flex: 1;
    height: 700px; /* même hauteur que liste + pagination */
}
</style>
