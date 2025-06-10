import { ref } from 'vue'

export const globalError = ref('')

export function setError(message) {
  globalError.value = message
}

export function clearError() {
  globalError.value = ''
}