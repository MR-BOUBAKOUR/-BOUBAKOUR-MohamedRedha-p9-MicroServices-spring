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
        default:
            return ''
    }
})
</script>

<template>
    <section :class="['assessment-card', resultClass]">
        <p><strong>NIVEAU:</strong> {{ assessment.level }}</p>
        <p><strong>CONTEXTE:</strong> {{ assessment.context }}</p>
        <p><strong>ANALYSE:</strong> {{ assessment.analysis }}</p>
        <p><strong>RECOMMANDATIONS:</strong> {{ assessment.recommendations }}</p>
        <p><strong>SOURCES:</strong> {{ assessment.sources }}</p>
        <p><strong>STATUS:</strong> {{ assessment.status }}</p>
    </section>
</template>

<style scoped>
.assessment-card {
    border: 1px solid #bbb;
    border-radius: 8px;
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
</style>
