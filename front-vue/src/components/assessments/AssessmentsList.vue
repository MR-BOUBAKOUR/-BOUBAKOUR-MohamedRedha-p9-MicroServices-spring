<script setup>
import AssessmentCard from './AssessmentCard.vue'

defineProps({
    assessments: {
        type: Array,
        required: true,
    },
})
</script>

<template>
    <section class="assessments-section">
        <h2>Assessments</h2>
        <TransitionGroup tag="ul" name="list" class="assessments-list" v-if="assessments?.length">
            <li
                v-for="(assessment, index) in assessments"
                :key="assessment.id ?? index"
                class="assessment-card"
            >
                <AssessmentCard :assessment="assessment" />
            </li>
        </TransitionGroup>
        <p v-else>Ce patient n'a pas encore d'évaluations.</p>
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
    border: 1px solid #ccc;
    padding: 0.5rem;
}
</style>
