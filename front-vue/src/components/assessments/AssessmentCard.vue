<script setup>
import { computed, ref } from 'vue'
import {
    acceptAssessment,
    downloadAssessmentPdf,
    refusePendingAssessment,
} from '@/services/assessment-service.js'
import { setError } from '@/stores/error.js'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
    assessment: {
        type: Object,
        required: true,
    },
})

const emit = defineEmits(['accepted', 'reload'])

const currentStatus = ref(props.assessment.status)

const statusIcon = computed(() => {
    switch (currentStatus.value) {
        case 'PENDING':
            return '/icons/status_pending.svg'
        case 'ACCEPTED':
            return '/icons/status_accepted.svg'
        case 'UPDATED':
            return '/icons/status_updated.svg'
        case 'MANUAL':
            return '/icons/status_manual.svg'
        case 'REFUSED_PENDING':
            return '/icons/status_pending.svg'
        case 'REFUSED':
            return '/icons/status_refused.svg'
        default:
            return null
    }
})

const levelIcon = computed(() => {
    switch (props.assessment.level) {
        case 'VERY_LOW':
            return '/icons/risk_very_low.svg'
        case 'LOW':
            return '/icons/risk_low.svg'
        case 'MODERATE':
            return '/icons/risk_moderate.svg'
        case 'HIGH':
            return '/icons/risk_high.svg'
        default:
            return null
    }
})

const canEdit = computed(
    () => currentStatus.value === 'PENDING' || currentStatus.value === 'REFUSED_PENDING',
)

const canDownload = computed(() => ['ACCEPTED', 'UPDATED', 'MANUAL'].includes(currentStatus.value))

const handleAccept = async () => {
    try {
        const updated = await acceptAssessment(props.assessment.id)
        currentStatus.value = updated.status

        // Émettre un event pour rafraîchir la section infos
        emit('accepted', updated)
    } catch (err) {
        setError(err.message || 'Erreur lors de l’acceptation.')
    }
}

const handleModify = () => {
    router.push({
        name: 'assessment-edit',
        params: {
            patientId: props.assessment.patId,
            assessmentId: props.assessment.id,
        },
    })
}

const handleRefusedPending = async () => {
    try {
        const refusedPending = await refusePendingAssessment(props.assessment.id)
        currentStatus.value = refusedPending.status
    } catch (err) {
        setError(err.message || 'Erreur lors du refus transitoire.')
    }
}

const handleReload = () => {
    emit('reload', props.assessment)
    // Can do better... FOR NOW *
    currentStatus.value = 'REFUSED'
}

const handleManual = () => {
    router.push({
        name: 'assessment-create-manual',
        params: {
            patientId: props.assessment.patId,
            assessmentId: props.assessment.id,
        },
    })
}

const handleDownload = async () => {
    try {
        await downloadAssessmentPdf(props.assessment.id)
    } catch (err) {
        setError(err.message || 'Erreur lors téléchargement du PDF.')
    }
}
</script>

<template>
    <section class="assessment-card">
        <div class="card-container">
            <!-- Left: 80% -->
            <div class="card-left">
                <!-- Main information section -->
                <div class="info-main">
                    <p><strong>CONTEXTE</strong></p>
                    <ul>
                        <li v-for="(item, index) in assessment.context" :key="index">{{ item }}</li>
                    </ul>

                    <p><strong>ANALYSE</strong></p>
                    <p>{{ assessment.analysis }}</p>

                    <p><strong>RECOMMANDATIONS</strong></p>
                    <ul>
                        <li v-for="(item, index) in assessment.recommendations" :key="index">
                            {{ item }}
                        </li>
                    </ul>
                </div>

                <!-- Secondary information section -->
                <div class="info-secondary">
                    <p><strong>CRÉÉ LE</strong></p>
                    <p>{{ new Date(assessment.createdAt).toLocaleString() }}</p>

                    <p><strong>SOURCES</strong></p>
                    <ul>
                        <li v-for="(item, index) in assessment.sources" :key="index">{{ item }}</li>
                    </ul>
                </div>
            </div>

            <!-- Right: 20% -->
            <div class="card-right">
                <div class="info-boxes">
                    <div class="level-box">
                        <p class="level-label">RISQUE</p>
                        <img :src="levelIcon" alt="level" width="100" height="100" />
                    </div>

                    <div class="status-box">
                        <p class="status-label">STATUT</p>
                        <img :src="statusIcon" alt="status" width="100" height="100" />
                    </div>
                </div>

                <div v-if="canEdit">
                    <div v-if="currentStatus === 'PENDING'" class="action-box">
                        <button class="button-accept" @click="handleAccept">Accepter</button>
                        <button class="button-modify" @click="handleModify">Modifier</button>
                        <button class="button-refuse" @click="handleRefusedPending">Refuser</button>
                    </div>

                    <div v-else-if="currentStatus === 'REFUSED_PENDING'" class="action-box">
                        <button class="button-reload" @click="handleReload">
                            Relancer une évaluation AI
                        </button>
                        <button class="button-manual" @click="handleManual">
                            Créer une évaluation manuellement
                        </button>
                    </div>
                </div>

                <div v-if="canDownload" class="action-box">
                    <button class="button-download" @click="handleDownload">
                        Télécharger l’évaluation
                    </button>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.assessment-card {
    border: 1px solid #bbb;
    border-radius: 8px;
    margin-bottom: 1rem;
    padding: 2rem;
}

/* Container flex */
.card-container {
    display: flex;
    flex-direction: row;
    column-gap: 200px;
}

/* Left content 80% */
.card-left {
    width: 80%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 4rem;
}

.info-secondary {
    opacity: 0.9;
    font-size: 0.65em;
}

/* Right panel 20% */
.card-right {
    width: 20%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 4rem;
}

.info-boxes {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 12px;
}

.level-box,
.status-box {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    border: 1px solid #aaa;
    border-radius: 4px;
    padding: 1rem 0;
}

.level-box img,
.status-box img {
    height: auto;
}

.level-box p,
.status-box p {
    margin: 0;
    text-align: center;
}

/* Action box */
.action-box {
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: 0.5rem;
}

.action-box button {
    padding: 0.5rem;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
    color: white;
    border: none;
    transition: background-color 0.2s;
}

.button-accept,
.button-download {
    background-color: #4caf50;
}

.button-modify {
    background-color: #2196f3;
}

.button-refuse {
    background-color: #f44336;
}

.button-reload,
.button-manual {
    background-color: #ff9800;
}

.action-box button:hover {
    opacity: 0.9;
}
</style>
