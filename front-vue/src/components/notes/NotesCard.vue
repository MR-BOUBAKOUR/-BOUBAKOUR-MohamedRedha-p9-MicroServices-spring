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
        <TransitionGroup tag="ul" name="list" class="notes-list" v-if="notes.length > 0">
            <li v-for="note in notes" :key="note.id" class="note-item">
                {{ note.note }}
            </li>
        </TransitionGroup>
        <p v-else>Ce patient n'a pas encore de notes.</p>
    </section>
</template>

<style scoped>
.list-enter-active {
    transition: all 1s ease;
}

.list-enter-from {
    opacity: 0;
    transform: translateX(-30px);
}

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
    background-color: #f5f5f5;
}
</style>
