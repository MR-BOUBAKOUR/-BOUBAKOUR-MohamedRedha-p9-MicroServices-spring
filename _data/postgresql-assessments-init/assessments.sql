CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    pat_id BIGINT NOT NULL,
    level VARCHAR(50) NOT NULL,
    context TEXT,
    analysis TEXT,
    recommendations TEXT,
    sources TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACCEPTED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Patient 6 – Jean Dupont
INSERT INTO assessments (pat_id, level, context, analysis, recommendations, sources, status)
VALUES
    (6,
     'VERY_LOW',
     'Le patient se sent généralement bien, pas de plainte majeure. Tension artérielle et poids dans les normes. Glycémie à jeun 0,95 g/L. Examen physique normal.',
     'Observations cliniques simples, aucun signe inquiétant.',
     'Aucune recommandation spécifique pour le moment.',
     'exemple généré au lancement du service, pas de source',
     'ACCEPTED');

-- Patient 7 – Marie Martin
INSERT INTO assessments (pat_id, level, context, analysis, recommendations, sources, status)
VALUES
    (7,
     'LOW',
     'Le patient se plaint de fatigue et de stress au travail. Légère perte auditive signalée récemment. Cholestérol total 2,2 g/L, LDL 1,5 g/L.',
     'Stress élevé et légère anomalie auditive, cholestérol légèrement élevé.',
     'Suivi conseillé pour stress et contrôle lipidique.',
     'exemple généré au lancement du service, pas de source',
     'ACCEPTED');

-- Patient 8 – Pierre Durand
INSERT INTO assessments (pat_id, level, context, analysis, recommendations, sources, status)
VALUES
    (8,
     'MODERATE',
     'Le patient fumeur depuis 5 ans. Toux légère signalée. Spirométrie normale, radiographie thoracique sans anomalies. LDL légèrement élevé (1,6 g/L).',
     'Fumeur, LDL légèrement élevé, légère toux, tests respiratoires normaux.',
     'Arrêt du tabac recommandé, suivi lipidique.',
     'exemple généré au lancement du service, pas de source',
     'ACCEPTED');

-- Patient 9 – Sophie Bernard
INSERT INTO assessments (pat_id, level, context, analysis, recommendations, sources, status)
VALUES
    (9,
     'MODERATE',
     'Le patient se plaint d’essoufflement à l’effort et de douleurs lombaires après position assise prolongée. IMC 28, tension normale, rythme cardiaque régulier. Hémoglobine A1C 6,2 %. Cholestérol normal.',
     'Essoufflement modéré, douleurs lombaires, légère élévation HbA1C.',
     'Kinésithérapie et suivi endocrinien conseillé.',
     'exemple généré au lancement du service, pas de source',
     'ACCEPTED');

-- Patient 10 – Laurent Petit
INSERT INTO assessments (pat_id, level, context, analysis, recommendations, sources, status)
VALUES
    (10,
     'HIGH',
     'Patient à haut risque métabolique. IMC 31 kg/m², antécédents familiaux de diabète et hypertension. Glycémie à jeun 1,25 g/L, HbA1c 7,2 %. Polyurie et soif excessive. Tension 145/90 mmHg.',
     'Risque métabolique élevé, hyperglycémie et hypertension.',
     'Régime alimentaire, activité physique et suivi diabétologique conseillé.',
     'exemple généré au lancement du service, pas de source',
     'ACCEPTED');