<script setup>
import { RouterLink } from 'vue-router'

defineProps({
    patients: {
        type: Array,
        required: true,
    },
})
</script>

<template>
    <table class="patients-table">
        <thead>
            <tr>
                <th>Prénom</th>
                <th>Nom</th>
                <th>Date de naissance</th>
                <th>Genre</th>
                <th>Adresse</th>
                <th>Téléphone</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="patient in patients" :key="patient.id">
                <td>{{ patient.firstName }}</td>
                <td>{{ patient.lastName }}</td>
                <td>{{ patient.birthDate }}</td>
                <td>{{ patient.gender }}</td>
                <td>{{ patient.address }}</td>
                <td>{{ patient.phone }}</td>
                <td>
                    <RouterLink :to="{ name: 'patient', params: { id: patient.id } }">
                        Voir
                    </RouterLink>
                </td>
            </tr>
        </tbody>
    </table>
</template>

<style scoped>
.patients-table {
    width: 100%;
    margin-top: 1rem;
}
.patients-table th,
.patients-table td {
    border: 1px solid #ccc;
    padding: 0.5rem;
    text-align: left;
}
.patients-table th {
    background-color: #f5f5f5;
}
</style>
