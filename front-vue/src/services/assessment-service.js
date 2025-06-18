import axios from 'axios'
import { setError } from '@/stores/error'
import { setupAxiosInterceptors } from './api'

const api = axios.create({
    baseURL: 'http://localhost:8071/v1',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

setupAxiosInterceptors(api)

export async function fetchAssessmentByPatientId(id) {
    try {
        const response = await api.get(`/assessments/${id}`)
        return response.data.data
    } catch (error) {
        if (error.response?.status !== 401) {
            if (axios.isAxiosError(error) && error.response) {
                setError(
                    error.response.data.message ||
                        "Erreur lors de la récupération de l'évaluation.",
                )
            } else {
                setError('Erreur de connexion.')
            }
        }
        throw error
    }
}
