-- Main table
CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    pat_id BIGINT NOT NULL,
    level VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    analysis TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Table for contexts (list of strings)
CREATE TABLE IF NOT EXISTS assessment_contexts (
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    context_item TEXT NOT NULL
);

-- Table for recommendations (list of strings)
CREATE TABLE IF NOT EXISTS assessment_recommendations (
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    recommendation_item TEXT NOT NULL
);

-- Table for sources (list of strings)
CREATE TABLE IF NOT EXISTS assessment_sources (
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    source_item TEXT NOT NULL
);

-- Index
CREATE INDEX IF NOT EXISTS idx_assessments_pat_id ON assessments(pat_id);

-- -----------------------
-- Patient 1
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (1, 1, 'VERY_LOW', 'ACCEPTED', 'Observations cliniques simples, aucun signe inquiétant. Glycémie stable, IMC normal, tension dans les normes.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (1, 'Le patient se sent généralement bien, pas de plainte majeure.'),
    (1, 'Tension artérielle et poids dans les normes.'),
    (1, 'Glycémie à jeun 0,95 g/L.'),
    (1, 'Examen physique normal.'),
    (1, 'Pas d’antécédents familiaux de diabète.'),
    (1, 'Activité physique modérée, alimentation équilibrée.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (1, 'Maintenir le mode de vie actuel.'),
    (1, 'Surveillance annuelle du contrôle glycémique.'),
    (1, 'Suivi de routine de la tension artérielle et du profil lipidique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (1, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 2
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (2, 2, 'LOW', 'ACCEPTED', 'Stress élevé, légère perte auditive récente, cholestérol légèrement élevé. Glycémie stable, IMC légèrement élevé. Symptômes bénins.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (2, 'Le patient se plaint de fatigue et de stress au travail.'),
    (2, 'Légère perte auditive signalée récemment.'),
    (2, 'Cholestérol total 2,2 g/L, LDL 1,5 g/L.'),
    (2, 'Pas d’infection active récente.'),
    (2, 'Réaction cutanée aux médicaments au cours des 3 derniers mois.'),
    (2, 'IMC 27, activité physique faible, consommation de sucre modérée.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (2, 'Suivi conseillé pour stress et contrôle lipidique.'),
    (2, 'Encourager activité physique régulière.'),
    (2, 'Réduction de la consommation de sucre.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (2, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 3
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (3, 3, 'MODERATE', 'ACCEPTED', 'Fumeur, LDL légèrement élevé, légère toux, tests respiratoires normaux. Glycémie légèrement élevée, IMC modéré. Risque cardiovasculaire modéré.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (3, 'Le patient fumeur depuis 5 ans.'),
    (3, 'Toux légère signalée.'),
    (3, 'Spirométrie normale, radiographie thoracique sans anomalies.'),
    (3, 'LDL légèrement élevé (1,6 g/L).'),
    (3, 'Dyspnée nocturne occasionnelle.'),
    (3, 'Reprise récente du tabac après arrêt.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (3, 'Arrêt du tabac recommandé.'),
    (3, 'Suivi lipidique régulier.'),
    (3, 'Encourager activité physique.'),
    (3, 'Surveillance respiratoire si dyspnée persiste.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (3, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 4
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (4, 4, 'MODERATE', 'ACCEPTED', 'Essoufflement modéré, douleurs lombaires, légère élévation HbA1C. IMC 28, tension normale, début de tabagisme. Risque modéré de complications métaboliques.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (4, 'Le patient se plaint d’essoufflement à l’effort et de douleurs lombaires après position assise prolongée.'),
    (4, 'IMC 28, tension normale, rythme cardiaque régulier.'),
    (4, 'Hémoglobine A1C 6,2 %.'),
    (4, 'Cholestérol normal.'),
    (4, 'Signale vertiges occasionnels.'),
    (4, 'A commencé à fumer récemment.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (4, 'Kinésithérapie conseillée pour douleurs lombaires.'),
    (4, 'Suivi endocrinien recommandé pour HbA1C.'),
    (4, 'Arrêt du tabac recommandé.'),
    (4, 'Pratique d’activité physique adaptée.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (4, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 5
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (5, 5, 'HIGH', 'PENDING', 'Risque métabolique élevé, hyperglycémie et hypertension. IMC 31, antécédents familiaux de diabète. Polyurie et soif excessive. Risque élevé de complications cardiovasculaires et diabétiques.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (5, 'Patient à haut risque métabolique.'),
    (5, 'IMC 31 kg/m², antécédents familiaux de diabète et hypertension.'),
    (5, 'Glycémie à jeun 1,25 g/L, HbA1c 7,2 %.'),
    (5, 'Polyurie et soif excessive.'),
    (5, 'Tension 145/90 mmHg.'),
    (5, 'Sédentaire, consommation élevée de sucre, fatigue chronique.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (5, 'Régime alimentaire adapté.'),
    (5, 'Activité physique régulière.'),
    (5, 'Suivi diabétologique rapproché.'),
    (5, 'Éducation thérapeutique pour gestion du risque métabolique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (5, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Reset sequence so that the next id starts at 6
-- -----------------------
SELECT setval('assessments_id_seq', 5);