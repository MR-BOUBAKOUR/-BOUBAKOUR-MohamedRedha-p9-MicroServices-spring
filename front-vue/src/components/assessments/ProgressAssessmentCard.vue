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
        <div class="progress-container">
            <p><strong>{{ assessment.progressMessage }}</strong></p>
            <progress :value="assessment.progress" max="100"></progress>
            <p>{{ assessment.progress }} %</p>
        </div>
    </section>
</template>

<style scoped>
.assessment-card {
    border: 1px solid #bbb;
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 2rem;
    text-align: center;
}

.progress-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
}

progress {
    width: 100%;
    height: 20px;
}
</style>
