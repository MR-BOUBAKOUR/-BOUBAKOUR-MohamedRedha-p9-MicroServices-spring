<script setup>
defineProps({
    assessment: {
        type: Object,
        required: true,
    },
})
</script>

<template>
    <section class="assessment-card">
        <h2>Assessment</h2>
        {{ assessment.assessmentResult }}
    </section>
</template>

<style scoped></style>
