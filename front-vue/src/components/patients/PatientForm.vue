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
    const dataToSend = {
        ...form.value,
        gender: form.value.gender ? form.value.gender.toUpperCase() : '',
    }

    emit('submit', dataToSend)

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
            <div class="form-group">
                <label>Prénom</label>
                <input v-model="form.firstName" required />
            </div>
            <div class="form-group">
                <label>Nom</label>
                <input v-model="form.lastName" required />
            </div>
            <div class="form-group">
                <label>Date de naissance</label>
                <input type="date" v-model="form.birthDate" required />
            </div>
            <div class="form-group">
                <label>Genre</label>
                <input v-model="form.gender" />
            </div>
            <div class="form-group">
                <label>Adresse</label>
                <input v-model="form.address" />
            </div>
            <div class="form-group">
                <label>Téléphone</label>
                <input v-model="form.phone" />
            </div>

            <button type="submit">{{ submitLabel }}</button>
        </form>
    </section>
</template>

<style scoped>
.patient-card {
    border: 1px solid #ccc;
    margin: 2rem auto;
    padding: 1rem;
    max-width: 400px;
}

 .patient-card button {
    max-width: 400px;
 }

.patient-form {
    flex-direction: column;
}
</style>
