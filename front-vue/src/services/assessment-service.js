import axios from 'axios'
import { setError } from '@/stores/error'
import { setupAxiosInterceptors } from './api'

const api = axios.create({
    baseURL: `${import.meta.env.VITE_GATEWAY_URL}/v1`,
    timeout: 120000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
})

setupAxiosInterceptors(api)

export async function fetchAssessmentsByPatientId(id) {
    try {
        const response = await api.get(`/assessments/${id}`)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(
                    error.response.data.message ||
                    "Erreur lors de la récupération des évaluations.",
                )
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}

export async function generateAssessmentByPatientId(id) {
    try {
        const response = await api.get(`/assessments/${id}/generate`)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(
                    error.response.data.message ||
                    "Erreur lors de la génération de l'évaluation.",
                )
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}
