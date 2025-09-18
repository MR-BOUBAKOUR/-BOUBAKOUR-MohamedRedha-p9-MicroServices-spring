<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit'])

const form = ref({
    note: '',
})

function handleSubmit() {
    emit('submit', form.value)
    form.value = { note: '' }
}
</script>

<template>
    <form @submit.prevent="handleSubmit" class="patient-form">
        <textarea v-model="form.note" required />
        <button type="submit">Ajouter une note</button>
    </form>
</template>

<style scoped>
.patient-form {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.patient-form textarea {
    flex: 1;
    min-height: 403px;
    resize: none;
    padding: 0;
    font-size: 1rem;
    line-height: 1.4;
    border: 1px solid #ccc;
}

.patient-form button {
    margin-top: 1rem;
    align-self: center;
    padding: 0.5rem 1rem;
}
</style>
