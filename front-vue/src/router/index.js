import { createRouter, createWebHistory } from 'vue-router'
import PatientsView from '../views/PatientsView.vue'
import PatientView from '@/views/PatientView.vue'
import LoginView from '@/views/LoginView.vue'
import PatientEditView from '@/views/PatientEditView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/patients',
            name: 'patients',
            component: PatientsView,
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView,
        },
        {
            path: '/patients/:id',
            name: 'patient',
            component: PatientView,
        },
        {
            path: '/patients/:id/edit',
            name: 'patient-edit',
            component: PatientEditView,
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/patients',
        },
    ],
})

export default router
