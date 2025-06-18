import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import PatientsView from '../views/PatientsView.vue'
import PatientView from '@/views/PatientView.vue'
import LoginView from '@/views/LoginView.vue'
import PatientEditView from '@/views/PatientEditView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: { requiresGuest: true },
        },
        {
            path: '/patients',
            name: 'patients',
            component: PatientsView,
            meta: { requiresAuth: true },
        },
        {
            path: '/patients/:id',
            name: 'patient',
            component: PatientView,
            meta: { requiresAuth: true },
        },
        {
            path: '/patients/:id/edit',
            name: 'patient-edit',
            component: PatientEditView,
            meta: { requiresAuth: true },
        },
        {
            path: '/',
            redirect: '/patients',
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/patients',
        },
    ],
})

// Global navigation guard: runs before every route change
// It checks if the user is allowed to access the route
// Based on route meta and auth state, it allows or redirects using `next()`
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    if (!authStore.isInitialized && !authStore.isInitializing) {
        console.log('Loading authentication...')
        await authStore.initAuth()
    }
    while (authStore.isInitializing) {
        await new Promise((resolve) => setTimeout(resolve, 50)) // Waiting for the init's result
    }

    // Check if the target route requires authentication
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
    // Check if the target route should only be accessible to guests
    const requiresGuest = to.matched.some((record) => record.meta.requiresGuest)

    if (requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    } else if (requiresGuest && authStore.isAuthenticated) {
        next('/patients')
    } else {
        // Otherwise, allow navigation to proceed
        next()
    }
})

export default router
