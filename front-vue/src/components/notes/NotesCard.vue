<script setup>
defineProps({
    notes: {
        type: Array,
        required: true,
    },
})
</script>

<template>
    <section class="notes-card">
        <h2>Notes</h2>
        <ul class="notes-list">
            <li v-for="(note, index) in notes" :key="index" class="note-item">
                {{ note.note }}
            </li>
        </ul>
    </section>
</template>

<style scoped>
.notes-card {
    margin-top: 2rem;
}

.notes-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.note-item {
    border: 1px solid #ccc;
    padding: 0.5rem;
    margin-bottom: 0.5rem;
    background-color: #f9f9f9;
}
</style>
