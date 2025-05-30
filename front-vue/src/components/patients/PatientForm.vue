<script setup>
import { ref } from 'vue'

const props = defineProps({
    patient: Object,
    submitLabel: String,
})

const emit = defineEmits(['submit'])

const form = ref({
    firstName: props.patient?.firstName || '',
    lastName: props.patient?.lastName || '',
    birthDate: props.patient?.birthDate || '',
    gender: props.patient?.gender || '',
    address: props.patient?.address || '',
    phone: props.patient?.phone || '',
})

function handleSubmit() {
    emit('submit', form.value)

    form.value = {
        firstName: '',
        lastName: '',
        birthDate: '',
        gender: '',
        address: '',
        phone: '',
    }
}
</script>

<template>
    <section class="patient-card">
        <form @submit.prevent="handleSubmit" class="patient-form">
            <label>Prénom <input v-model="form.firstName" required /></label>
            <label>Nom <input v-model="form.lastName" required /></label>
            <label>Date de naissance <input type="date" v-model="form.birthDate" required /></label>
            <label>Genre <input v-model="form.gender" /></label>
            <label>Adresse <input v-model="form.address" /></label>
            <label>Téléphone <input v-model="form.phone" /></label>

            <button type="submit">{{ submitLabel }}</button>
        </form>
    </section>
</template>

<style scoped>
.patient-card {
    border: 1px solid #ccc;
    margin: 4rem auto;
    padding: 1rem;
    border-radius: 8px;
    max-width: 300px;
}

.patient-form {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}
</style>
