import axios from 'axios'
import { setError, clearError } from '@/stores/error'
import { setupAxiosInterceptors } from './api'

const api = axios.create({
    baseURL: `${import.meta.env.VITE_GATEWAY_URL}/v1`,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
})

setupAxiosInterceptors(api)

export async function fetchNotesByPatientId(noteId) {
    try {
        const response = await api.get(`/notes/${noteId}`)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Erreur lors de la récupération des notes.')
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}

export async function createNote(newNote) {
    try {
        clearError()
        const response = await api.post('/notes', newNote)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Erreur lors de la création de la note.')
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}
