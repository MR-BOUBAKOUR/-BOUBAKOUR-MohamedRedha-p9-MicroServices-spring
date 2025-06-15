<script setup>
import { computed } from 'vue'

const props = defineProps({
    assessment: {
        type: Object,
        required: true,
    },
})

const resultClass = computed(() => {
    switch (props.assessment.assessmentResult) {
        case 'None':
            return 'risk-none'
        case 'Borderline':
            return 'risk-borderline'
        case 'In Danger':
            return 'risk-danger'
        case 'Early onset':
            return 'risk-early-onset'
        default:
            return ''
    }
})
</script>

<template>
    <section class="assessment-card">
        <h2>Assessment</h2>
        <p :class="resultClass">{{ assessment.assessmentResult }}</p>
    </section>
</template>

<style scoped>
.assessment-card {
    max-width: 300px;
    width: 100%;
}

.assessment-card p {
    padding: 0.5rem;
    font-weight: bold;
    text-align: center;
}

.risk-none {
    background-color: #d0e7ff;
    border-color: #8ab4f8;
    color: #1a3e72;
}

.risk-borderline {
    background-color: #fff9c4;
    border-color: #f0e68c;
    color: #6b5700;
}

.risk-danger {
    background-color: #ffe0b2;
    border-color: #ffb74d;
    color: #7a4b00;
}

.risk-early-onset {
    background-color: #ffcdd2;
    border-color: #f28b82;
    color: #7f1f1f;
}
</style>
