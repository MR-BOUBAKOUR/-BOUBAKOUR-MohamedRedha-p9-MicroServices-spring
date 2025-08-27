<script setup>
import AssessmentCard from './AssessmentCard.vue'

defineProps({
    assessments: { type: Array, required: true },
    loading: { type: Boolean, default: false },
})
</script>

<template>
    <section class="assessments-section">
        <h2>Évaluations médicales</h2>

        <TransitionGroup
            tag="ul"
            name="list"
            class="assessments-list"
        >
            <!-- Generated assessment -->
            <li v-if="loading" key="pending" class="assessment-card">
                <p>Évaluation en cours…</p>
            </li>

            <!-- Existing assessments -->
            <li
                v-for="(assessment, index) in assessments"
                :key="assessment.id ?? index"
            >
                <AssessmentCard :assessment="assessment" />
            </li>
        </TransitionGroup>

        <p v-if="!loading && !assessments?.length">
            Ce patient n'a pas encore d'évaluations.
        </p>
    </section>
</template>

<style scoped>
.list-enter-active {
    transition: all 1s ease;
}

.list-enter-from {
    opacity: 0;
    transform: translateX(-30px);
}

.assessments-section {
    margin-top: 2rem;
}

.assessments-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.assessment-card {
    border: 1px solid #bbb;
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 2rem;
}
</style>
