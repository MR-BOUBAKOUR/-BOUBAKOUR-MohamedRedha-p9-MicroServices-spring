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
        <h2>Assessment</h2>
    <section :class="resultClass" class="assessment-card">
        <div>
            <p>NIVEAU: {{ assessment.assessmentResult }}</p>
            <p>RESUME: {{ assessment.summary }}</p>
            <p>RECOMMANDATIONS: {{ assessment.recommendations }}</p>
        </div>
    </section>
</template>

<style scoped>
.assessment-card {
    max-width: 320px;
    padding: 1rem;
    border: 1px solid #bbb;
    border-radius: 8px;
    font-family: Arial, sans-serif;
    background-color: #f9f9f9;
    color: #222;
    margin: 1rem auto;
}

.assessment-card h2 {
    text-align: center;
    font-weight: 600;
    margin-bottom: 1rem;
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
