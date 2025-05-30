<script setup>
defineProps({
    patient: {
        type: Object,
        required: true,
    },
})
</script>

<template>
    <section class="patient-card">
        <h2>{{ patient.firstName }} | {{ patient.lastName }}</h2>
        <ul>
            <li><strong>Date de naissance :</strong> {{ patient.birthDate }}</li>
            <li><strong>Genre :</strong> {{ patient.gender }}</li>
            <li><strong>Adresse :</strong> {{ patient.address }}</li>
            <li><strong>Téléphone :</strong> {{ patient.phone }}</li>
        </ul>
        <RouterLink :to="{ name: 'patient-edit', params: { id: patient.id } }">
            Modifier le patient
        </RouterLink>
    </section>
</template>

<style scoped>
.patient-card {
    border: 1px solid #ccc;
    margin: 4rem auto;
    padding: 1rem;
    border-radius: 8px;
}
</style>
