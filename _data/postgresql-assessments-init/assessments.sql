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
-- Patient 6
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (6, 6, 'VERY_LOW', 'ACCEPTED', 'Observations cliniques simples, aucun signe inquiétant.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (6, 'Le patient se sent généralement bien, pas de plainte majeure.'),
    (6, 'Tension artérielle et poids dans les normes.'),
    (6, 'Glycémie à jeun 0,95 g/L.'),
    (6, 'Examen physique normal.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (6, 'Aucune recommandation spécifique pour le moment.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (6, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 7
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (7, 7, 'LOW', 'ACCEPTED', 'Stress élevé et légère anomalie auditive, cholestérol légèrement élevé.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (7, 'Le patient se plaint de fatigue et de stress au travail.'),
    (7, 'Légère perte auditive signalée récemment.'),
    (7, 'Cholestérol total 2,2 g/L, LDL 1,5 g/L.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (7, 'Suivi conseillé pour stress et contrôle lipidique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (7, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 8
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (8, 8, 'MODERATE', 'ACCEPTED', 'Fumeur, LDL légèrement élevé, légère toux, tests respiratoires normaux.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (8, 'Le patient fumeur depuis 5 ans.'),
    (8, 'Toux légère signalée.'),
    (8, 'Spirométrie normale, radiographie thoracique sans anomalies.'),
    (8, 'LDL légèrement élevé (1,6 g/L).');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (8, 'Arrêt du tabac recommandé, suivi lipidique.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (8, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 9
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (9, 9, 'MODERATE', 'ACCEPTED', 'Essoufflement modéré, douleurs lombaires, légère élévation HbA1C.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (9, 'Le patient se plaint d’essoufflement à l’effort et de douleurs lombaires après position assise prolongée.'),
    (9, 'IMC 28, tension normale, rythme cardiaque régulier.'),
    (9, 'Hémoglobine A1C 6,2 %.'),
    (9, 'Cholestérol normal.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (9, 'Kinésithérapie et suivi endocrinien conseillé.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (9, 'exemple généré au lancement du service, pas de source');

-- -----------------------
-- Patient 10
-- -----------------------
INSERT INTO assessments (id, pat_id, level, status, analysis, created_at, updated_at)
VALUES (10, 10, 'HIGH', 'ACCEPTED', 'Risque métabolique élevé, hyperglycémie et hypertension.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO assessment_contexts (assessment_id, context_item)
VALUES
    (10, 'Patient à haut risque métabolique.'),
    (10, 'IMC 31 kg/m², antécédents familiaux de diabète et hypertension.'),
    (10, 'Glycémie à jeun 1,25 g/L, HbA1c 7,2 %.'),
    (10, 'Polyurie et soif excessive.'),
    (10, 'Tension 145/90 mmHg.');

INSERT INTO assessment_recommendations (assessment_id, recommendation_item)
VALUES
    (10, 'Régime alimentaire, activité physique et suivi diabétologique conseillé.');

INSERT INTO assessment_sources (assessment_id, source_item)
VALUES
    (10, 'exemple généré au lancement du service, pas de source');