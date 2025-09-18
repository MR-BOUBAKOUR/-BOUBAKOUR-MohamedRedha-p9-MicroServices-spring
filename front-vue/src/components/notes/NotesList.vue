<script setup>
defineProps({
    notes: {
        type: Array,
        required: true,
    },
})
</script>

<template>
    <TransitionGroup tag="ul" name="list" class="notes-list" v-if="notes.length > 0">
        <li v-for="note in notes" :key="note.id" class="note-card">
            <div class="note-content" :class="{ expanded: note.expanded }">
                {{ note.note }}
            </div>
            <button class="toggle-btn" @click="note.expanded = !note.expanded">
                {{ note.expanded ? 'Réduire' : 'Détails' }}
            </button>
        </li>
    </TransitionGroup>
    <p v-else>Ce patient n'a pas encore de notes.</p>
</template>

<style scoped>
.list-enter-active {
    transition: all 0.5s ease;
}

.list-enter-from {
    opacity: 0;
    transform: translateX(-30px);
}

.notes-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.note-card {
    border: 1px solid #ccc;
    padding: 0.5rem;
    margin-bottom: 1rem;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
}

.note-content {
    overflow: hidden;
    line-height: 1.4;
    max-height: calc(1.4em * 3);
    min-height: calc(1.4em * 3);
    transition: max-height 0.3s ease;
}

.note-content.expanded {
    max-height: 1000px;
}

.toggle-btn {
    align-self: flex-end;
    border: none;
    background-color: transparent;
    color: #007bff;
    margin-top: 0.5rem;
    width: auto;
    max-width: none;
}
</style>
