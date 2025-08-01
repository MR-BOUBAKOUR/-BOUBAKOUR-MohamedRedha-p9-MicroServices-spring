import axios from 'axios'
import { setError, clearError } from '@/stores/error'
import { setupAxiosInterceptors } from './api'

const api = axios.create({
    baseURL: 'https://localhost:8071/v1',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
})

setupAxiosInterceptors(api)

export async function fetchPatients() {
    try {
        const response = await api.get('/patients')
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(
                    error.response.data.message || 'Erreur lors de la récupération des patients.',
                )
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}

export async function fetchPatientById(id) {
    try {
        const response = await api.get(`/patients/${id}`)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(
                    error.response.data.message || 'Erreur lors de la récupération du patient.',
                )
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}

export async function createPatient(newPatient) {
    try {
        clearError()
        const response = await api.post('/patients', newPatient)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Erreur lors de la création du patient.')
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}

export async function updatePatient(id, updatedPatient) {
    try {
        const response = await api.put(`/patients/${id}`, updatedPatient)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data.message || 'Erreur lors de la mise à jour du patient')
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}
