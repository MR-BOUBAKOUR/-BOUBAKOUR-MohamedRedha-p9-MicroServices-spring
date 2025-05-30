<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit'])

const form = ref({
    note: '',
})

function handleSubmit() {
    emit('submit', form.value)

    form.value = {
        note: '',
    }
}
</script>

<template>
    <form @submit.prevent="handleSubmit" class="patient-form">
        <input v-model="form.note" required />

        <button type="submit">Rajouter une note</button>
    </form>
</template>

<style scoped>
.patient-form {
    display: flex;
    flex-direction: row;
    gap: 0.5rem;
}
</style>
