<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AssessmentForm from '@/components/assessments/AssessmentForm.vue'
import { fetchAssessmentById, updateAssessment } from '@/services/assessment-service.js'

const route = useRoute()
const router = useRouter()

const patientId = Number(route.params.patientId)
const assessmentId = Number(route.params.assessmentId)

const assessment = ref(null)

onMounted(async () => {
    assessment.value = await fetchAssessmentById(assessmentId)
})

async function handleAssessmentUpdate(updatedAssessment) {
    try {
        await updateAssessment(assessmentId, updatedAssessment)
        router.push({ name: 'patient', params: { patientId: patientId } })
    } catch (e) {
        console.warn('Erreur lors de la mise à jour de l\'assessment.', e)
    }
}
</script>

<template>
    <main>
        <AssessmentForm
            v-if="assessment"
            :assessment="assessment"
            @submit="handleAssessmentUpdate"
        />
    </main>
</template>
