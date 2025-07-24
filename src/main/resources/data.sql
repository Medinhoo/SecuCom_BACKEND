-- Users (Admin + 2 Secretariat employees + 5 Company contacts)
-- MOTS DE PASSE POUR TOUS LES UTILISATEURS: "password"

INSERT INTO tuser (id, username, email, password, first_name, last_name, phone_number, account_status, created_at, dtype, position, specialization, fonction, permissions)
VALUES 
-- Admin: username=admin, password=password
(RANDOM_UUID(), 'admin', 'admin@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'System', '+32 2 123 456 789', 'ACTIVE', CURRENT_TIMESTAMP, 'User', NULL, NULL, NULL, NULL),
-- Secrétariat 1: username=secretariat1, password=password
(RANDOM_UUID(), 'secretariat1', 'marie.dupont@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Marie', 'Dupont', '+32 2 987 654 321', 'ACTIVE', CURRENT_TIMESTAMP, 'SecretariatEmployee', 'Consultant RH Senior', 'Paie et Administration', NULL, NULL),
-- Secrétariat 2: username=secretariat2, password=password
(RANDOM_UUID(), 'secretariat2', 'pierre.martin@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Pierre', 'Martin', '+32 2 321 654 987', 'ACTIVE', CURRENT_TIMESTAMP, 'SecretariatEmployee', 'Consultant RH Junior', 'Déclarations Sociales', NULL, NULL),
-- Company contacts
(RANDOM_UUID(), 'company1', 'contact@techcorp.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Antoine', 'Dubois', '+32 2 456 789 123', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Directeur RH', 'FULL_ACCESS'),
(RANDOM_UUID(), 'company2', 'info@belconstruction.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Sophie', 'Laurent', '+32 2 789 123 456', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Responsable Personnel', 'FULL_ACCESS'),
(RANDOM_UUID(), 'company3', 'contact@foodco.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Marc', 'Leroy', '+32 2 555 11 22', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Chef du Personnel', 'FULL_ACCESS'),
(RANDOM_UUID(), 'company4', 'info@logitrans.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Julie', 'Moreau', '+32 2 555 33 44', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Directrice RH', 'FULL_ACCESS'),
(RANDOM_UUID(), 'company5', 'contact@greenenergy.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Thomas', 'Bernard', '+32 2 555 55 66', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Responsable RH', 'FULL_ACCESS');

-- User roles
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_ADMIN' FROM tuser WHERE username = 'admin';
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_SECRETARIAT' FROM tuser WHERE username IN ('secretariat1', 'secretariat2');
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_COMPANY' FROM tuser WHERE username IN ('company1', 'company2', 'company3', 'company4', 'company5');

-- Social Secretariat
INSERT INTO tsocial_secretariat (id, name, company_number, address, phone, email, website)
VALUES (RANDOM_UUID(), 'SecuCom Social Secretariat', 'BE0987654321', '123 Avenue Louise, 1050 Brussels, Belgium', '+32 2 123 45 67', 'contact@secucom.be', 'https://www.secucom.be');

-- Companies (5 companies)
INSERT INTO tcompany (id, name, phone_number, email, iban, security_fund, work_accident_insurance, bce_number, onss_number, legal_form, company_name, creation_date, vat_number, work_regime, salary_reduction, activity_sector, category, work_calendar, collaboration_start_date, subscription_formula, declaration_frequency, address_street, address_number, address_box, address_postal_code, address_city, address_country)
VALUES 
(RANDOM_UUID(), 'TechCorp', '+32 2 555 12 34', 'contact@techcorp.be', 'BE68 5390 0754 7034', '123456-78', 'ETHIAS-789456', '0123456789', '1234567', 'SPRL', 'TechCorp SPRL', '2020-01-15', 'BE0123456789', '40h/semaine', '20%', 'IT Services', 'PME', 'Standard', '2023-01-01', 'Premium', 'Mensuelle', 'Avenue Louise', '123', 'B3', '1050', 'Bruxelles', 'Belgique'),
(RANDOM_UUID(), 'BelConstruction', '+32 2 555 98 76', 'info@belconstruction.be', 'BE71 3350 0254 9869', '987654-32', 'AG-123789', '9876.543.210', 'RSZ987654321', 'SA', 'BelConstruction SA', '2018-06-20', 'BE9876543210', '38h/semaine', '15%', 'Construction', 'Grande Entreprise', 'Flexible', '2023-03-01', 'Standard', 'Trimestrielle', 'Rue de la Loi', '45', 'B', '1000', 'Bruxelles', 'Belgique'),
(RANDOM_UUID(), 'FoodCo', '+32 2 555 11 22', 'contact@foodco.be', 'BE72 3350 0254 9870', '123789-45', 'AXA-456123', '4567.890.123', 'RSZ456789012', 'SPRL', 'FoodCo SPRL', '2019-03-15', 'BE4567890123', '38h/semaine', '10%', 'Food Industry', 'PME', 'Flexible', '2023-02-01', 'Basic', 'Mensuelle', 'Boulevard Anspach', '78', NULL, '1000', 'Bruxelles', 'Belgique'),
(RANDOM_UUID(), 'LogiTrans', '+32 2 555 33 44', 'info@logitrans.be', 'BE73 3350 0254 9871', '456123-78', 'ALLIANZ-789123', '7890.123.456', 'RSZ789012345', 'SA', 'LogiTrans SA', '2017-09-10', 'BE7890123456', '40h/semaine', '12%', 'Transport', 'Grande Entreprise', 'Standard', '2023-04-01', 'Premium', 'Trimestrielle', 'Chaussée de Charleroi', '234', NULL, '1060', 'Saint-Gilles', 'Belgique'),
(RANDOM_UUID(), 'GreenEnergy', '+32 2 555 55 66', 'contact@greenenergy.be', 'BE74 3350 0254 9872', '789456-12', 'P&V-123456', '0123.789.456', 'RSZ012345678', 'SA', 'GreenEnergy SA', '2021-01-20', 'BE0123789456', '38h/semaine', '18%', 'Energy', 'PME', 'Flexible', '2023-05-01', 'Standard', 'Mensuelle', 'Avenue de Tervueren', '156', NULL, '1150', 'Woluwe-Saint-Pierre', 'Belgique');

-- Link SecretariatEmployees to SocialSecretariat
UPDATE tuser SET secretariat_id = (SELECT id FROM tsocial_secretariat LIMIT 1) WHERE username IN ('secretariat1', 'secretariat2');

-- Link CompanyContacts to Companies
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'TechCorp' LIMIT 1) WHERE username = 'company1';
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'BelConstruction' LIMIT 1) WHERE username = 'company2';
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'FoodCo' LIMIT 1) WHERE username = 'company3';
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'LogiTrans' LIMIT 1) WHERE username = 'company4';
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'GreenEnergy' LIMIT 1) WHERE username = 'company5';

-- Document Templates
INSERT INTO tdocument_template (id, name, display_name, description, file_name, file_path, mapping_config_path, active, document_type, email_enabled, default_email_subject, default_email_body, default_recipients, default_cc_recipients, created_at, updated_at)
VALUES 
(RANDOM_UUID(), 'CNT_Employe', 'Contrat de travail employé', 'Template pour générer un contrat de travail pour un employé', 'CNT_Employe.docx', 'CNT_Employe.docx', 'templates/mappings/CNT_Employe.json', true, 'CONTRAT', true, 'Nouveau contrat de travail - {collaboratorName}', 'Bonjour,\n\nVeuillez trouver ci-joint le contrat de travail pour {collaboratorName}.\n\nCordialement', '["COMPANY_EMAIL"]', '["CURRENT_USER_EMAIL"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Contrat_etudiant', 'Contrat étudiant', 'Template pour générer un contrat étudiant', 'Contrat_etudiant.docx', 'Contrat_etudiant.docx', 'templates/mappings/Contrat_etudiant.json', true, 'CONTRAT', true, 'Contrat étudiant - {collaboratorName}', 'Bonjour,\n\nVeuillez trouver ci-joint le contrat étudiant pour {collaboratorName}.\n\nCordialement', '["COMPANY_EMAIL"]', '["CURRENT_USER_EMAIL"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Attestation_travail', 'Attestation de travail', 'Template pour générer une attestation de travail', 'Attestation_travail.docx', 'Attestation_travail.docx', 'templates/mappings/Attestation_travail.json', true, 'DOCUMENT', true, 'Attestation de travail - {collaboratorName}', 'Bonjour,\n\nVeuillez trouver ci-joint l''attestation de travail pour {collaboratorName}.\n\nCordialement', '["COLLABORATOR_EMAIL"]', '["CURRENT_USER_EMAIL"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Collaborators (2 per company = 10 total)
INSERT INTO tcollaborator (id, last_name, first_name, nationality, birth_date, birth_place, gender, language, civil_status, national_number, service_entry_date, type, job_function, contract_type, work_regime, work_duration_type, salary, joint_committee, task_description, iban, company_id, address_street, address_number, address_postal_code, address_city, address_country, establishment_street, establishment_number, establishment_postal_code, establishment_city, establishment_country, created_at, updated_at)
VALUES 
-- TechCorp collaborators
(RANDOM_UUID(), 'Dupont', 'Jean', 'Belge', '1985-03-15', 'Bruxelles', 'M', 'FR', 'MARRIED', '85031512345', '2023-01-15', 0, 'Développeur Senior', 'CDI', 'Temps plein', 0, 3500.00, '200', 'Développement d''applications web', 'BE68539007547034', (SELECT id FROM tcompany WHERE name = 'TechCorp'), 'Rue de la Paix', '12', '1000', 'Bruxelles', 'Belgique', 'Avenue Louise', '123', '1050', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Martin', 'Sophie', 'Belge', '1990-07-22', 'Liège', 'F', 'FR', 'SINGLE', '90072298765', '2023-02-01', 0, 'Analyste Business', 'CDI', 'Temps plein', 0, 3200.00, '200', 'Analyse des besoins métier', 'BE68539007547035', (SELECT id FROM tcompany WHERE name = 'TechCorp'), 'Avenue des Arts', '45', '1000', 'Bruxelles', 'Belgique', 'Avenue Louise', '123', '1050', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- BelConstruction collaborators
(RANDOM_UUID(), 'Leroy', 'Marc', 'Belge', '1982-11-08', 'Charleroi', 'M', 'FR', 'MARRIED', '82110834567', '2023-03-15', 1, 'Chef de chantier', 'CDI', 'Temps plein', 0, 3800.00, '124', 'Supervision des travaux de construction', 'BE71335002549869', (SELECT id FROM tcompany WHERE name = 'BelConstruction'), 'Rue du Commerce', '78', '6000', 'Charleroi', 'Belgique', 'Rue de la Loi', '45', '1000', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Moreau', 'Julie', 'Belge', '1988-05-12', 'Namur', 'F', 'FR', 'SINGLE', '88051245678', '2023-04-01', 0, 'Ingénieur Qualité', 'CDI', 'Temps plein', 0, 3600.00, '200', 'Contrôle qualité des constructions', 'BE71335002549870', (SELECT id FROM tcompany WHERE name = 'BelConstruction'), 'Place de l''Ange', '23', '5000', 'Namur', 'Belgique', 'Rue de la Loi', '45', '1000', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- FoodCo collaborators
(RANDOM_UUID(), 'Bernard', 'Thomas', 'Belge', '1987-09-03', 'Gand', 'M', 'NL', 'MARRIED', '87090356789', '2023-02-15', 0, 'Chef de production', 'CDI', 'Temps plein', 0, 3400.00, '118', 'Gestion de la production alimentaire', 'BE72335002549870', (SELECT id FROM tcompany WHERE name = 'FoodCo'), 'Korenlei', '15', '9000', 'Gent', 'Belgique', 'Boulevard Anspach', '78', '1000', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Janssen', 'Emma', 'Belge', '1992-12-18', 'Anvers', 'F', 'NL', 'SINGLE', '92121867890', '2023-05-01', 0, 'Responsable Qualité', 'CDI', 'Temps plein', 0, 3100.00, '118', 'Contrôle qualité alimentaire', 'BE72335002549871', (SELECT id FROM tcompany WHERE name = 'FoodCo'), 'Meir', '89', '2000', 'Antwerpen', 'Belgique', 'Boulevard Anspach', '78', '1000', 'Bruxelles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- LogiTrans collaborators
(RANDOM_UUID(), 'Dubois', 'Pierre', 'Belge', '1984-06-25', 'Mons', 'M', 'FR', 'MARRIED', '84062578901', '2023-04-15', 1, 'Chauffeur poids lourd', 'CDI', 'Temps plein', 1, 2800.00, '140', 'Transport de marchandises', 'BE73335002549871', (SELECT id FROM tcompany WHERE name = 'LogiTrans'), 'Grand Place', '12', '7000', 'Mons', 'Belgique', 'Chaussée de Charleroi', '234', '1060', 'Saint-Gilles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Laurent', 'Marie', 'Belge', '1989-01-30', 'Tournai', 'F', 'FR', 'SINGLE', '89013089012', '2023-06-01', 0, 'Coordinatrice logistique', 'CDI', 'Temps plein', 0, 3300.00, '200', 'Coordination des transports', 'BE73335002549872', (SELECT id FROM tcompany WHERE name = 'LogiTrans'), 'Rue Royale', '56', '7500', 'Tournai', 'Belgique', 'Chaussée de Charleroi', '234', '1060', 'Saint-Gilles', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- GreenEnergy collaborators
(RANDOM_UUID(), 'Peeters', 'Luc', 'Belge', '1986-04-14', 'Louvain', 'M', 'NL', 'MARRIED', '86041490123', '2023-05-15', 0, 'Ingénieur Énergies Renouvelables', 'CDI', 'Temps plein', 0, 4000.00, '200', 'Développement de projets éoliens', 'BE74335002549872', (SELECT id FROM tcompany WHERE name = 'GreenEnergy'), 'Oude Markt', '34', '3000', 'Leuven', 'Belgique', 'Avenue de Tervueren', '156', '1150', 'Woluwe-Saint-Pierre', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Van Der Berg', 'Lisa', 'Belge', '1991-08-07', 'Hasselt', 'F', 'NL', 'SINGLE', '91080701234', '2023-07-01', 0, 'Analyste Environnementale', 'CDI', 'Temps plein', 0, 3500.00, '200', 'Études d''impact environnemental', 'BE74335002549873', (SELECT id FROM tcompany WHERE name = 'GreenEnergy'), 'Grote Markt', '67', '3500', 'Hasselt', 'Belgique', 'Avenue de Tervueren', '156', '1150', 'Woluwe-Saint-Pierre', 'Belgique', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Dimonas IN (2 per collaborator for contracts = 20 total)
INSERT INTO tdimona (id, entry_date, exit_date, exit_reason, onss_reference, status, type, error_message, collaborator_id, company_id)
SELECT 
    RANDOM_UUID(),
    DATEADD('DAY', -30, CURRENT_TIMESTAMP),
    NULL,
    NULL,
    'IN' || LPAD(CAST(ROW_NUMBER() OVER() AS VARCHAR), 6, '0'),
    'TO_CONFIRM',
    'IN',
    NULL,
    c.id,
    c.company_id
FROM tcollaborator c, (SELECT 1 as n UNION SELECT 2) numbers;

-- Dimonas OUT (1 per collaborator for terminated contracts = 10 total)
INSERT INTO tdimona (id, entry_date, exit_date, exit_reason, onss_reference, status, type, error_message, collaborator_id, company_id)
SELECT 
    RANDOM_UUID(),
    NULL,
    DATEADD('DAY', -5, CURRENT_TIMESTAMP),
    'Fin de contrat',
    'OUT' || LPAD(CAST(ROW_NUMBER() OVER() AS VARCHAR), 6, '0'),
    'TO_CONFIRM',
    'OUT',
    NULL,
    c.id,
    c.company_id
FROM tcollaborator c;

-- Contrats (2 per collaborator = 20 total)
-- First insert CNT_Employe contracts
INSERT INTO tdocument (id, template_id, company_id, generated_by, generated_file_name, generated_file_path, pdf_file_path, status, error_message, created_at, document_type)
SELECT 
    RANDOM_UUID(),
    (SELECT id FROM tdocument_template WHERE name = 'CNT_Employe'),
    c.company_id,
    (SELECT id FROM tuser WHERE username = 'secretariat1'),
    'CNT_Employe_' || c.first_name || '_' || c.last_name || '_1.docx',
    '/generated-documents/CNT_Employe_' || c.first_name || '_' || c.last_name || '_1.docx',
    '/generated-documents/CNT_Employe_' || c.first_name || '_' || c.last_name || '_1.pdf',
    'COMPLETED',
    NULL,
    DATEADD('DAY', -10, CURRENT_TIMESTAMP),
    'CONTRAT'
FROM tcollaborator c;

-- Then insert Contrat_etudiant contracts
INSERT INTO tdocument (id, template_id, company_id, generated_by, generated_file_name, generated_file_path, pdf_file_path, status, error_message, created_at, document_type)
SELECT 
    RANDOM_UUID(),
    (SELECT id FROM tdocument_template WHERE name = 'Contrat_etudiant'),
    c.company_id,
    (SELECT id FROM tuser WHERE username = 'secretariat1'),
    'Contrat_etudiant_' || c.first_name || '_' || c.last_name || '_2.docx',
    '/generated-documents/Contrat_etudiant_' || c.first_name || '_' || c.last_name || '_2.docx',
    '/generated-documents/Contrat_etudiant_' || c.first_name || '_' || c.last_name || '_2.pdf',
    'COMPLETED',
    NULL,
    DATEADD('DAY', -20, CURRENT_TIMESTAMP),
    'CONTRAT'
FROM tcollaborator c;

-- Insert CNT_Employe contrat records
INSERT INTO tcontrat (id, collaborator_id, dimona_in_id, dimona_out_id, contrat_status, start_date, end_date)
SELECT 
    d.id,
    c.id,
    (SELECT dim.id FROM tdimona dim WHERE dim.collaborator_id = c.id AND dim.type = 'IN' LIMIT 1),
    (SELECT dim.id FROM tdimona dim WHERE dim.collaborator_id = c.id AND dim.type = 'OUT' LIMIT 1),
    'TERMINE',
    DATEADD('DAY', -10, CURRENT_DATE),
    DATEADD('DAY', -5, CURRENT_DATE)
FROM tcollaborator c
JOIN tdocument d ON d.document_type = 'CONTRAT' 
    AND d.generated_file_name = 'CNT_Employe_' || c.first_name || '_' || c.last_name || '_1.docx';

-- Insert Contrat_etudiant contrat records
INSERT INTO tcontrat (id, collaborator_id, dimona_in_id, dimona_out_id, contrat_status, start_date, end_date)
SELECT 
    d.id,
    c.id,
    (SELECT dim.id FROM tdimona dim WHERE dim.collaborator_id = c.id AND dim.type = 'IN' LIMIT 1 OFFSET 1),
    NULL,
    'ACTIF',
    DATEADD('DAY', -20, CURRENT_DATE),
    NULL
FROM tcollaborator c
JOIN tdocument d ON d.document_type = 'CONTRAT' 
    AND d.generated_file_name = 'Contrat_etudiant_' || c.first_name || '_' || c.last_name || '_2.docx';

-- Update dimonas to link them to contracts
UPDATE tdimona SET contrat_id = (
    SELECT c.id FROM tcontrat c 
    WHERE c.dimona_in_id = tdimona.id
) WHERE type = 'IN';

UPDATE tdimona SET contrat_id = (
    SELECT c.id FROM tcontrat c 
    WHERE c.dimona_out_id = tdimona.id
) WHERE type = 'OUT';

-- Note: Generic documents (like Attestation_travail) are not inserted as sample data
-- because they don't have a corresponding entity class with a discriminator value.
-- Only Contrat documents are supported in the current entity hierarchy.
