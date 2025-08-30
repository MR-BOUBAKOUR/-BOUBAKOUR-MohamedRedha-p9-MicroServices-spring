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

export async function downloadAssessmentPdf(assessmentId) {
    try {
        // responseType 'blob' tells Axios to return raw binary data instead of trying to parse JSON
        const response = await api.get(`/assessments/${assessmentId}/download`, {
            responseType: 'blob'
        });

        // Convert the raw bytes into a Blob object, telling the browser it's a PDF
        const blobResponse = new Blob([response.data], { type: 'application/pdf' });

        // Create a temporary URL that points to the Blob
        const url = window.URL.createObjectURL(blobResponse);

        // Create an invisible <a> element and set it up to download the file
        const link = document.createElement('a');
        link.href = url;
        link.download = `assessment_${assessmentId}.pdf`;

        // Append it to the DOM, trigger a click to start the download, then remove it
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        // Release the memory used by the temporary URL
        window.URL.revokeObjectURL(url);
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            setError(error.response.data.message || "Erreur lors du téléchargement du PDF.");
        } else {
            setError("Erreur de connexion.");
        }
        throw error;
    }
}
