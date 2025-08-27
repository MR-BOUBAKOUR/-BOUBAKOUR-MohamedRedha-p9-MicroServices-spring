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
VALUES (1, 1, 'VERY_LOW', 'ACCEPTED', 'Observations cliniques simples, aucun signe inquiétant.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (1, 'Le patient se sent généralement bien, pas de plainte majeure.'),
    (1, 'Tension artérielle et poids dans les normes.'),
    (1, 'Glycémie à jeun 0,95 g/L.'),
    (1, 'Examen physique normal.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (1, 'Aucune recommandation spécifique pour le moment.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (1, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 2
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (2, 2, 'LOW', 'ACCEPTED', 'Stress élevé et légère anomalie auditive, cholestérol légèrement élevé.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (2, 'Le patient se plaint de fatigue et de stress au travail.'),
    (2, 'Légère perte auditive signalée récemment.'),
    (2, 'Cholestérol total 2,2 g/L, LDL 1,5 g/L.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (2, 'Suivi conseillé pour stress et contrôle lipidique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (2, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 3
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (3, 3, 'MODERATE', 'ACCEPTED', 'Fumeur, LDL légèrement élevé, légère toux, tests respiratoires normaux.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (3, 'Le patient fumeur depuis 5 ans.'),
    (3, 'Toux légère signalée.'),
    (3, 'Spirométrie normale, radiographie thoracique sans anomalies.'),
    (3, 'LDL légèrement élevé (1,6 g/L).');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (3, 'Arrêt du tabac recommandé, suivi lipidique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (3, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 4
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (4, 4, 'MODERATE', 'ACCEPTED', 'Essoufflement modéré, douleurs lombaires, légère élévation HbA1C.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (4, 'Le patient se plaint d’essoufflement à l’effort et de douleurs lombaires après position assise prolongée.'),
    (4, 'IMC 28, tension normale, rythme cardiaque régulier.'),
    (4, 'Hémoglobine A1C 6,2 %.'),
    (4, 'Cholestérol normal.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (4, 'Kinésithérapie et suivi endocrinien conseillé.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (4, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 5
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (5, 5, 'HIGH', 'ACCEPTED', 'Risque métabolique élevé, hyperglycémie et hypertension.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (5, 'Patient à haut risque métabolique.'),
    (5, 'IMC 31 kg/m², antécédents familiaux de diabète et hypertension.'),
    (5, 'Glycémie à jeun 1,25 g/L, HbA1c 7,2 %.'),
    (5, 'Polyurie et soif excessive.'),
    (5, 'Tension 145/90 mmHg.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (5, 'Régime alimentaire, activité physique et suivi diabétologique conseillé.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (5, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Reset sequence so that the next id starts at 6
-- -----------------------
SELECT setval('assessments_id_seq', 5);