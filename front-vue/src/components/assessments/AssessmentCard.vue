<script setup>
import { computed } from 'vue'

const props = defineProps({
    assessment: {
        type: Object,
        required: true,
    },
})

const resultClass = computed(() => {
    if (!props.assessment?.level) return ''
    
    switch (props.assessment.level) {
        case 'VERY_LOW':
            return 'risk-very-low'
        case 'LOW':
            return 'risk-low'
        case 'MODERATE':
            return 'risk-moderate'
        case 'HIGH':
            return 'risk-high'
        case 'VERY_HIGH':
            return 'risk-very-high'
        default:
            return ''
    }
})
</script>

<template>
    <h2>Assessment</h2>
    <section :class="resultClass" class="assessment-card">
        <div v-if="!assessment">
            <p>Analyse en cours...</p>
        </div>
        <div v-else>
            <p>NIVEAU: {{ assessment.level }}</p>
            <p>CONTEXTE: {{ assessment.context }}</p>
            <p>ANALYSE: {{ assessment.analysis }}</p>
            <p>RECOMMANDATIONS: {{ assessment.recommendations }}</p>
        </div>
    </section>
</template>

<style scoped>
.assessment-card {
    max-width: 600px;
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

.risk-very-low {
    background-color: #d0e7ff;
    border-color: #8ab4f8;
    color: #1a3e72;
}

.risk-low {
    background-color: #fff9c4;
    border-color: #f0e68c;
    color: #6b5700;
}

.risk-moderate {
    background-color: #ffe0b2;
    border-color: #ffb74d;
    color: #7a4b00;
}

.risk-high {
    background-color: #ffcdd2;
    border-color: #f28b82;
    color: #7f1f1f;
}

.risk-very-high {
    background-color: #d32f2f;
    border-color: #b71c1c;
    color: #ffffff;
}
</style>
