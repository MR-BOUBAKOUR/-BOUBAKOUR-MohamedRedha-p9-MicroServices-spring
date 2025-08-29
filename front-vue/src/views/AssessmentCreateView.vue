<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AssessmentForm from '@/components/assessments/AssessmentForm.vue'
import {
    fetchAssessmentById,
    createAssessment,
    refuseAssessment,
} from '@/services/assessment-service.js'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()

const authStore = useAuthStore()

const patientId = Number(route.params.patientId)
const assessmentId = Number(route.params.assessmentId)

const assessment = ref(null)

onMounted(async () => {
    const base = await fetchAssessmentById(assessmentId)
    assessment.value = {
        context: [...(base.context || [])],
        analysis: '',
        level: '',
        recommendations: [''],
        sources: [authStore.user.username],
    }
})

async function handleAssessmentCreate(newAssessment) {
    try {
        // Create the new assessment produced by the doctor
        await createAssessment(patientId, newAssessment)

        // Update the status of the previous assessment created by the AI
        // from "REFUSED-PENDING" to "REFUSED"
        await refuseAssessment(assessmentId)

        // Can do better... FOR NOW *

        router.push({ name: 'patient', params: { patientId } })
    } catch (e) {
        console.warn("Erreur lors de la création de l'assessment.", e)
    }
}
</script>

<template>
    <main>
        <AssessmentForm
            v-if="assessment"
            :initialData="assessment"
            @submit="handleAssessmentCreate"
        />
    </main>
</template>
