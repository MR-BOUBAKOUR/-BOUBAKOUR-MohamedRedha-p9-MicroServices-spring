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

export async function fetchAssessmentsByPatientId(patientId) {
    try {
        const response = await api.get(`/assessments/patient/${patientId}`)
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

export async function generateAssessmentByPatientId(patientId) {
    try {
        const response = await api.get(`/assessments/patient/${patientId}/generate`)
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

export async function createAssessment(patientId, newAssessment) {
    try {
        const response = await api.post(`/assessments/patient/${patientId}/create`, newAssessment)
        return response.data.data
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(
                error.response.data.message ||
                "Erreur lors de la création de l'évaluation."
            )
        } else {
            setError('Erreur de connexion.')
        }
        throw error
    }
}

export async function fetchAssessmentById(assessmentId) {
    try {
        const response = await api.get(`/assessments/${assessmentId}`)
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

export async function acceptAssessment(assessmentId) {
    try {
        const response = await api.patch(`/assessments/${assessmentId}/accept`)
        return response.data.data
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(
                error.response.data.message ||
                "Erreur lors de l'acceptation de l'évaluation."
            )
        } else {
            setError('Erreur de connexion.')
        }
        throw error
    }
}

export async function updateAssessment(assessmentId, updatedAssessment) {
    try {
        const response = await api.patch(`/assessments/${assessmentId}`, updatedAssessment)
        return response.data.data
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(
                error.response.data.message ||
                "Erreur lors de la mise à jour de l'évaluation."
            )
        } else {
            setError('Erreur de connexion.')
        }
        throw error
    }
}

export async function refusePendingAssessment(assessmentId) {
    try {
        const response = await api.patch(`/assessments/${assessmentId}/refuse-pending`)
        return response.data.data
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(
                error.response.data.message ||
                "Erreur lors du refus transitoire de l'évaluation."
            )
        } else {
            setError('Erreur de connexion.')
        }
        throw error
    }
}

export async function refuseAssessment(assessmentId) {
    try {
        const response = await api.patch(`/assessments/${assessmentId}/refuse`)
        return response.data.data
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(
                error.response.data.message ||
                "Erreur lors du refus de l'évaluation."
            )
        } else {
            setError('Erreur de connexion.')
        }
        throw error
    }
}
