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
                <th>Genre</th>
                <th>Action</th>
            </tr>
        </thead>
        <TransitionGroup tag="tbody" name="list" v-if="patients.length > 0">
            <tr v-for="patient in patients" :key="patient.id">
                <td>{{ patient.firstName }}</td>
                <td>{{ patient.lastName }}</td>
                <td>{{ patient.gender }}</td>
                <td>
                    <RouterLink :to="{ name: 'patient', params: { patientId: patient.id } }">
                        Voir
                    </RouterLink>
                </td>
            </tr>
        </TransitionGroup>
    </table>
</template>

<style scoped>
.list-enter-active {
    transition: all 1s ease-out;
}

.list-enter-from {
    opacity: 0;
    transform: translateX(-20px);
}

.patients-table {
    width: 100%;
    margin: 2rem auto;
    border-collapse: collapse;
}

.patients-table th,
.patients-table td {
    border: 1px solid #ccc;
    padding: 0.5rem 1rem;
    text-align: left;
}

/* Largeurs fixes */
.patients-table th:nth-child(1),
.patients-table td:nth-child(1),
.patients-table th:nth-child(2),
.patients-table td:nth-child(2) {
    width: 40%;
}

.patients-table th:nth-child(3),
.patients-table td:nth-child(3),
.patients-table th:nth-child(4),
.patients-table td:nth-child(4) {
    text-align: center;
    vertical-align: middle;
    width: 10%;
}

.patients-table th {
    background-color: #f5f5f5;
}
</style>
