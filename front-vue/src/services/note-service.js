import axios from 'axios';
import { setError, clearError } from '@/stores/error';

const api = axios.create({
  baseURL: 'http://localhost:8071/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function fetchNotesByPatientId(id) {
  try {
    const response = await api.get(`/notes/${id}`);
    return response.data.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      setError(error.response.data.message || "Erreur lors de la récupération des notes.");
    } else {
      setError("Erreur de connexion.");
    }
    throw error;
  }
}

export async function createNote(newNote) {
  try {
    clearError();
    const response = await api.post('/notes', newNote);
    return response.data.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      setError(error.response.data.message || "Erreur lors de la création de la note.");
    } else {
      setError("Erreur de connexion.");
    }
    throw error;
  }
}