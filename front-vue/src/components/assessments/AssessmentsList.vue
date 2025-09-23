<script setup>
import AssessmentCard from './AssessmentCard.vue'
import ProgressAssessmentCard from './ProgressAssessmentCard.vue'

defineProps({
    assessments: { type: Array, required: true },
})

defineEmits(['accepted', 'reload'])
</script>

<template>
    <TransitionGroup tag="ul" name="list" class="assessments-list">
        <!-- Existing assessments -->
        <li v-for="(assessment, index) in assessments" :key="assessment.id ?? index">
            <ProgressAssessmentCard
                v-if="assessment.progress !== undefined"
                :assessment="assessment"
            />
            <AssessmentCard
                v-else
                :assessment="assessment"
                @accepted="$emit('accepted', $event)"
                @reload="$emit('reload', $event)"
            />
        </li>
    </TransitionGroup>

    <p v-if="!assessments?.length">Ce patient n'a pas encore d'évaluations.</p>
</template>

<style scoped>
.list-enter-active {
    transition: all 1s ease;
}

.list-enter-from {
    opacity: 0;
    transform: translateX(-30px);
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
