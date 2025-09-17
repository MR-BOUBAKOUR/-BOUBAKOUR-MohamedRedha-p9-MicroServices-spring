<script setup>
import { ref } from 'vue'

const props = defineProps({
    initialData: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['submit'])

const form = ref({
    analysis: props.initialData.analysis || '',
    level: props.initialData.level || '',
    context: [...(props.initialData.context || [])],
    recommendations: [...(props.initialData.recommendations || [])],
    sources: [...(props.initialData.sources || [])],
})

function handleSubmit() {
    emit('submit', { ...form.value })
}
</script>

<template>
    <section class="assessment-card">
        <form @submit.prevent="handleSubmit" class="assessment-form">
            <div class="form-group">
                <label>Contexte</label>
                <div v-for="(item, index) in form.context" :key="index">
                    <input v-model="form.context[index]" />
                </div>
            </div>

            <div class="form-group">
                <label>Analyse</label>
                <textarea v-model="form.analysis" rows="15"></textarea>
            </div>

            <div class="form-group">
                <label>Niveau de risque</label>
                <select v-model="form.level">
                    <option value="VERY_LOW">Very Low</option>
                    <option value="LOW">Low</option>
                    <option value="MODERATE">Moderate</option>
                    <option value="HIGH">High</option>
                </select>
            </div>

            <div class="form-group">
                <label>Recommandations</label>
                <div v-for="(item, index) in form.recommendations" :key="index">
                    <input v-model="form.recommendations[index]" />
                </div>
            </div>

            <div class="form-group">
                <label>Sources</label>
                <div v-for="(item, index) in form.sources" :key="index">
                    <input v-model="form.sources[index]" />
                </div>
            </div>

            <button type="submit">Enregistrer l’évaluation</button>
        </form>
    </section>
</template>

<style scoped>
.assessment-card {
    border: 1px solid #ccc;
    margin: 2rem auto;
    padding: 1rem;
    border-radius: 8px;
    max-width: 600px;
}

.assessment-card button {
    max-width: 600px;
}

.assessment-form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

input,
textarea,
select {
    padding: 0.5rem;
    border-radius: 4px;
    border: 1px solid #aaa;
}
</style>
