-- Users (3 users, one of each role)
INSERT INTO tuser (id, username, email, password, first_name, last_name, phone_number, account_status, created_at, dtype, position, specialization, fonction, permissions)
VALUES 
('11111111-1111-1111-1111-111111111111', 'admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User', '+32 123 456 789', 'ACTIVE', CURRENT_TIMESTAMP, 'User', NULL, NULL, NULL, NULL),
('22222222-2222-2222-2222-222222222222', 'secretariat', 'secretariat@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Secretariat', 'User', '+32 987 654 321', 'ACTIVE', CURRENT_TIMESTAMP, 'SecretariatEmployee', 'Consultant RH', 'Paie', NULL, NULL),
('33333333-3333-3333-3333-333333333333', 'company', 'company@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Company', 'User', '+32 456 789 123', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Directeur RH', 'FULL_ACCESS');

-- User roles
INSERT INTO user_roles (user_id, roles)
VALUES 
('11111111-1111-1111-1111-111111111111', 'ROLE_ADMIN'),
('22222222-2222-2222-2222-222222222222', 'ROLE_SECRETARIAT'),
('33333333-3333-3333-3333-333333333333', 'ROLE_COMPANY');

-- Social Secretariat
INSERT INTO tsocial_secretariat (id, name, company_number, address, phone, email, website)
VALUES ('44444444-4444-4444-4444-444444444444', 'Acerta', 'BE0123456789', '123 Main Street, 1000 Brussels, Belgium', '+32 2 123 45 67', 'contact@acerta.be', 'https://www.acerta.be');

-- Companies (5 companies)
INSERT INTO tcompany (id, name, phone_number, email, iban, security_fund, work_accident_insurance, bce_number, onss_number, legal_form, company_name, creation_date, vat_number, work_regime, salary_reduction, activity_sector, category, work_calendar, collaboration_start_date, subscription_formula, declaration_frequency)
VALUES 
('55555555-5555-5555-5555-555555555555', 'TechCorp', '+32 2 555 12 34', 'contact@techcorp.be', 'BE68 5390 0754 7034', '123456-78', 'ETHIAS-789456', '0123.456.789', 'RSZ123456789', 'SPRL', 'TechCorp SPRL', '2020-01-15', 'BE0123456789', '40h/semaine', '20%', 'IT Services', 'PME', 'Standard', '2023-01-01', 'Premium', 'Mensuelle'),
('66666666-6666-6666-6666-666666666666', 'BelConstruction', '+32 2 555 98 76', 'info@belconstruction.be', 'BE71 3350 0254 9869', '987654-32', 'AG-123789', '9876.543.210', 'RSZ987654321', 'SA', 'BelConstruction SA', '2018-06-20', 'BE9876543210', '38h/semaine', '15%', 'Construction', 'Grande Entreprise', 'Flexible', '2023-03-01', 'Standard', 'Trimestrielle'),
('77777777-7777-7777-7777-777777777777', 'FoodCo', '+32 2 555 11 22', 'contact@foodco.be', 'BE72 3350 0254 9870', '123789-45', 'AXA-456123', '4567.890.123', 'RSZ456789012', 'SPRL', 'FoodCo SPRL', '2019-03-15', 'BE4567890123', '38h/semaine', '10%', 'Food Industry', 'PME', 'Flexible', '2023-02-01', 'Basic', 'Mensuelle'),
('88888888-8888-8888-8888-888888888888', 'LogiTrans', '+32 2 555 33 44', 'info@logitrans.be', 'BE73 3350 0254 9871', '456123-78', 'ALLIANZ-789123', '7890.123.456', 'RSZ789012345', 'SA', 'LogiTrans SA', '2017-09-10', 'BE7890123456', '40h/semaine', '12%', 'Transport', 'Grande Entreprise', 'Standard', '2023-04-01', 'Premium', 'Trimestrielle'),
('99999999-9999-9999-9999-999999999999', 'GreenEnergy', '+32 2 555 55 66', 'contact@greenenergy.be', 'BE74 3350 0254 9872', '789456-12', 'P&V-123456', '0123.789.456', 'RSZ012345678', 'SA', 'GreenEnergy SA', '2021-01-20', 'BE0123789456', '38h/semaine', '18%', 'Energy', 'PME', 'Flexible', '2023-05-01', 'Standard', 'Mensuelle');

-- Link SecretariatEmployee to SocialSecretariat
UPDATE tuser SET secretariat_id = '44444444-4444-4444-4444-444444444444' WHERE id = '22222222-2222-2222-2222-222222222222';

-- Link CompanyContact to Company
UPDATE tuser SET company_id = '55555555-5555-5555-5555-555555555555' WHERE id = '33333333-3333-3333-3333-333333333333';

-- Insert company joint committees
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('55555555-5555-5555-5555-555555555555', '200');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('55555555-5555-5555-5555-555555555555', '337');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('66666666-6666-6666-6666-666666666666', '124');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('66666666-6666-6666-6666-666666666666', '200');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('77777777-7777-7777-7777-777777777777', '118');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('77777777-7777-7777-7777-777777777777', '200');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('88888888-8888-8888-8888-888888888888', '140');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('88888888-8888-8888-8888-888888888888', '200');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('99999999-9999-9999-9999-999999999999', '326');
INSERT INTO company_joint_committees (company_id, joint_committee) VALUES ('99999999-9999-9999-9999-999999999999', '200');

-- Insert Collaborators (20 collaborators)
INSERT INTO tcollaborator (id, last_name, first_name, nationality, birth_date, birth_place, gender, language, civil_status, national_number, service_entry_date, type, job_function, contract_type, work_regime, work_duration_type, salary, joint_committee, task_description, iban, company_id, address_street, address_number, address_postal_code, address_city, address_country, establishment_street, establishment_number, establishment_postal_code, establishment_city, establishment_country, created_at, updated_at)
VALUES 
-- TechCorp Collaborators (4)
('c0001111-0000-0000-0000-000000000001', 'Dubois', 'Antoine', 'Belge', '1990-05-15', 'Bruxelles', 'M', 'FR', 'Célibataire', '90051512345', '2023-03-01', 0, 'Développeur Senior', 'CDI', 'Temps plein', 0, 4500.00, '200', 'Développement full-stack', 'BE68 5390 0754 7034', '55555555-5555-5555-5555-555555555555', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0001111-0000-0000-0000-000000000002', 'Martin', 'Sophie', 'Belge', '1992-08-20', 'Liège', 'F', 'FR', 'Mariée', '92082012345', '2023-04-01', 0, 'UX Designer', 'CDI', 'Temps plein', 0, 3800.00, '200', 'Design d''interface', 'BE68 5390 0754 7035', '55555555-5555-5555-5555-555555555555', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0001111-0000-0000-0000-000000000003', 'Janssens', 'Thomas', 'Belge', '1988-03-10', 'Anvers', 'M', 'NL', 'Célibataire', '88031012345', '2023-05-01', 0, 'DevOps Engineer', 'CDI', 'Temps plein', 0, 4200.00, '200', 'Infrastructure et déploiement', 'BE68 5390 0754 7036', '55555555-5555-5555-5555-555555555555', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0001111-0000-0000-0000-000000000004', 'Peeters', 'Julie', 'Belge', '1995-11-25', 'Gand', 'F', 'NL', 'Célibataire', '95112512345', '2023-06-01', 0, 'Data Analyst', 'CDI', 'Temps partiel', 0, 2800.00, '200', 'Analyse de données', 'BE68 5390 0754 7037', '55555555-5555-5555-5555-555555555555', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),

-- BelConstruction Collaborators (4)
('c0002222-0000-0000-0000-000000000001', 'Laurent', 'Pierre', 'Belge', '1985-08-22', 'Liège', 'M', 'FR', 'Marié', '85082212345', '2023-04-01', 1, 'Chef de Chantier', 'CDI', 'Temps plein', 1, 3800.00, '124', 'Gestion de chantier', 'BE71 3350 0254 9869', '66666666-6666-6666-6666-666666666666', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0002222-0000-0000-0000-000000000002', 'Maes', 'David', 'Belge', '1987-06-15', 'Charleroi', 'M', 'FR', 'Célibataire', '87061512345', '2023-05-01', 1, 'Maçon', 'CDI', 'Temps plein', 1, 3200.00, '124', 'Construction', 'BE71 3350 0254 9870', '66666666-6666-6666-6666-666666666666', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0002222-0000-0000-0000-000000000003', 'Willems', 'Marc', 'Belge', '1983-09-30', 'Namur', 'M', 'FR', 'Marié', '83093012345', '2023-06-01', 1, 'Électricien', 'CDI', 'Temps plein', 1, 3400.00, '124', 'Installation électrique', 'BE71 3350 0254 9871', '66666666-6666-6666-6666-666666666666', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0002222-0000-0000-0000-000000000004', 'Claes', 'Philippe', 'Belge', '1989-12-05', 'Mons', 'M', 'FR', 'Célibataire', '89120512345', '2023-07-01', 1, 'Plombier', 'CDI', 'Temps plein', 1, 3300.00, '124', 'Installation sanitaire', 'BE71 3350 0254 9872', '66666666-6666-6666-6666-666666666666', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),

-- FoodCo Collaborators (4)
('c0003333-0000-0000-0000-000000000001', 'Jacobs', 'Marie', 'Belge', '1991-04-18', 'Bruxelles', 'F', 'FR', 'Célibataire', '91041812345', '2023-03-15', 1, 'Chef Cuisinier', 'CDI', 'Temps plein', 1, 3600.00, '118', 'Préparation cuisine', 'BE72 3350 0254 9873', '77777777-7777-7777-7777-777777777777', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0003333-0000-0000-0000-000000000002', 'Mertens', 'Lucas', 'Belge', '1993-07-22', 'Anvers', 'M', 'NL', 'Marié', '93072212345', '2023-04-15', 1, 'Sous-Chef', 'CDI', 'Temps plein', 1, 3200.00, '118', 'Assistance cuisine', 'BE72 3350 0254 9874', '77777777-7777-7777-7777-777777777777', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0003333-0000-0000-0000-000000000003', 'Wouters', 'Emma', 'Belge', '1994-02-28', 'Gand', 'F', 'NL', 'Célibataire', '94022812345', '2023-05-15', 1, 'Pâtissier', 'CDI', 'Temps plein', 0, 3100.00, '118', 'Pâtisserie', 'BE72 3350 0254 9875', '77777777-7777-7777-7777-777777777777', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0003333-0000-0000-0000-000000000004', 'Goossens', 'Tom', 'Belge', '1990-10-12', 'Bruges', 'M', 'NL', 'Marié', '90101212345', '2023-06-15', 1, 'Boucher', 'CDI', 'Temps plein', 0, 3300.00, '118', 'Préparation viande', 'BE72 3350 0254 9876', '77777777-7777-7777-7777-777777777777', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),

-- LogiTrans Collaborators (4)
('c0004444-0000-0000-0000-000000000001', 'De Smet', 'Nicolas', 'Belge', '1988-01-15', 'Ostende', 'M', 'NL', 'Célibataire', '88011512345', '2023-04-01', 1, 'Chauffeur Poids Lourd', 'CDI', 'Temps plein', 1, 3400.00, '140', 'Transport routier', 'BE73 3350 0254 9877', '88888888-8888-8888-8888-888888888888', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0004444-0000-0000-0000-000000000002', 'Vermeulen', 'Sarah', 'Belge', '1992-05-20', 'Hasselt', 'F', 'NL', 'Mariée', '92052012345', '2023-05-01', 0, 'Dispatcher', 'CDI', 'Temps plein', 0, 3200.00, '140', 'Coordination transport', 'BE73 3350 0254 9878', '88888888-8888-8888-8888-888888888888', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0004444-0000-0000-0000-000000000003', 'Pauwels', 'Kevin', 'Belge', '1987-09-25', 'Louvain', 'M', 'NL', 'Célibataire', '87092512345', '2023-06-01', 1, 'Magasinier', 'CDI', 'Temps plein', 0, 2900.00, '140', 'Gestion entrepôt', 'BE73 3350 0254 9879', '88888888-8888-8888-8888-888888888888', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0004444-0000-0000-0000-000000000004', 'Aerts', 'Lisa', 'Belge', '1993-12-30', 'Malines', 'F', 'NL', 'Célibataire', '93123012345', '2023-07-01', 0, 'Gestionnaire Fleet', 'CDI', 'Temps plein', 0, 3500.00, '140', 'Gestion flotte', 'BE73 3350 0254 9880', '88888888-8888-8888-8888-888888888888', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', 'Rue de la Loi', '1', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),

-- GreenEnergy Collaborators (4)
('c0005555-0000-0000-0000-000000000001', 'Stevens', 'Michel', 'Belge', '1986-03-08', 'Liège', 'M', 'FR', 'Marié', '86030812345', '2023-05-01', 0, 'Ingénieur Énergie', 'CDI', 'Temps plein', 0, 4200.00, '326', 'Études énergétiques', 'BE74 3350 0254 9881', '99999999-9999-9999-9999-999999999999', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', 'Avenue Louise', '2', '1050', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0005555-0000-0000-0000-000000000002', 'Lemmens', 'Anne', 'Belge', '1990-07-13', 'Namur', 'F', 'FR', 'Célibataire', '90071312345', '2023-06-01', 0, 'Technicienne Solar', 'CDI', 'Temps plein', 1, 3600.00, '326', 'Installation panneaux', 'BE74 3350 0254 9882', '99999999-9999-9999-9999-999999999999', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', 'Rue Royale', '3', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0005555-0000-0000-0000-000000000003', 'Coppens', 'Jean', 'Belge', '1989-11-18', 'Charleroi', 'M', 'FR', 'Marié', '89111812345', '2023-07-01', 0, 'Analyste Performance', 'CDI', 'Temps plein', 0, 3800.00, '326', 'Analyse rendement', 'BE74 3350 0254 9883', '99999999-9999-9999-9999-999999999999', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', 'Boulevard Anspach', '4', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE),
('c0005555-0000-0000-0000-000000000004', 'Vandamme', 'Claire', 'Belge', '1991-02-23', 'Mons', 'F', 'FR', 'Célibataire', '91022312345', '2023-08-01', 0, 'Conseillère Énergie', 'CDI', 'Temps plein', 0, 3400.00, '326', 'Conseil client', 'BE74 3350 0254 9884', '99999999-9999-9999-9999-999999999999', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', 'Rue Neuve', '5', '1000', 'Bruxelles', 'Belgique', CURRENT_DATE, CURRENT_DATE);

-- Insert extra legal benefits for collaborators
INSERT INTO collaborator_extra_legal_benefits (collaborator_id, extra_legal_benefits)
SELECT id, 'Chèques-repas' FROM tcollaborator;

INSERT INTO collaborator_extra_legal_benefits (collaborator_id, extra_legal_benefits)
SELECT id, 'Assurance groupe' FROM tcollaborator WHERE company_id IN ('55555555-5555-5555-5555-555555555555', '99999999-9999-9999-9999-999999999999');

INSERT INTO collaborator_extra_legal_benefits (collaborator_id, extra_legal_benefits)
SELECT id, 'Voiture de société' FROM tcollaborator WHERE type = 0;

-- Insert Dimona declarations (5 records)
INSERT INTO tdimona (id, type, entry_date, exit_date, exit_reason, status, onss_reference, error_message, collaborator_id, company_id)
VALUES 
-- Active Dimona for a TechCorp developer
('d0001111-0000-0000-0000-000000000001', 'IN', '2023-03-01', NULL, NULL, 'ACTIVE', 'ONSS123456', NULL, 'c0001111-0000-0000-0000-000000000001', '55555555-5555-5555-5555-555555555555'),

-- Completed Dimona for a BelConstruction worker
('d0002222-0000-0000-0000-000000000001', 'OUT', '2023-04-01', '2023-12-31', 'Fin de contrat', 'COMPLETED', 'ONSS789012', NULL, 'c0002222-0000-0000-0000-000000000001', '66666666-6666-6666-6666-666666666666'),

-- Active Dimona for a FoodCo chef
('d0003333-0000-0000-0000-000000000001', 'IN', '2023-03-15', NULL, NULL, 'ACTIVE', 'ONSS345678', NULL, 'c0003333-0000-0000-0000-000000000001', '77777777-7777-7777-7777-777777777777'),

-- Error Dimona for a LogiTrans driver
('d0004444-0000-0000-0000-000000000001', 'IN', '2023-04-01', NULL, NULL, 'ERROR', 'ONSS901234', 'Invalid NISS number', 'c0004444-0000-0000-0000-000000000001', '88888888-8888-8888-8888-888888888888'),

-- Active Dimona for a GreenEnergy engineer
('d0005555-0000-0000-0000-000000000001', 'IN', '2023-05-01', NULL, NULL, 'ACTIVE', 'ONSS567890', NULL, 'c0005555-0000-0000-0000-000000000001', '99999999-9999-9999-9999-999999999999');
