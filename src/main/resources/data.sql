-- Users (5 users with different roles)
-- MOTS DE PASSE POUR TOUS LES UTILISATEURS: "password"

INSERT INTO tuser (id, username, email, password, first_name, last_name, phone_number, account_status, created_at, dtype, position, specialization, fonction, permissions)
VALUES 
-- Admin: username=admin, password=password
(RANDOM_UUID(), 'admin', 'admin@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'System', '+32 2 123 456 789', 'ACTIVE', CURRENT_TIMESTAMP, 'User', NULL, NULL, NULL, NULL),
-- Secrétariat 1: username=secretariat1, password=password
(RANDOM_UUID(), 'secretariat1', 'marie.dupont@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Marie', 'Dupont', '+32 2 987 654 321', 'ACTIVE', CURRENT_TIMESTAMP, 'SecretariatEmployee', 'Consultant RH Senior', 'Paie et Administration', NULL, NULL),
-- Entreprise 1: username=company1, password=password
(RANDOM_UUID(), 'company1', 'contact@techcorp.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Antoine', 'Dubois', '+32 2 456 789 123', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Directeur RH', 'FULL_ACCESS'),
-- Secrétariat 2: username=secretariat2, password=password
(RANDOM_UUID(), 'secretariat2', 'pierre.martin@secucom.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Pierre', 'Martin', '+32 2 321 654 987', 'ACTIVE', CURRENT_TIMESTAMP, 'SecretariatEmployee', 'Consultant RH Junior', 'Déclarations Sociales', NULL, NULL),
-- Entreprise 2: username=company2, password=password
(RANDOM_UUID(), 'company2', 'info@belconstruction.be', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Sophie', 'Laurent', '+32 2 789 123 456', 'ACTIVE', CURRENT_TIMESTAMP, 'CompanyContact', NULL, NULL, 'Responsable Personnel', 'FULL_ACCESS');

-- User roles
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_ADMIN' FROM tuser WHERE username = 'admin';
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_SECRETARIAT' FROM tuser WHERE username = 'secretariat1';
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_COMPANY' FROM tuser WHERE username = 'company1';
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_SECRETARIAT' FROM tuser WHERE username = 'secretariat2';
INSERT INTO user_roles (user_id, roles)
SELECT id, 'ROLE_COMPANY' FROM tuser WHERE username = 'company2';

-- Social Secretariat (1 secretariat)
INSERT INTO tsocial_secretariat (id, name, company_number, address, phone, email, website)
VALUES (RANDOM_UUID(), 'SecuCom Social Secretariat', 'BE0987654321', '123 Avenue Louise, 1050 Brussels, Belgium', '+32 2 123 45 67', 'contact@secucom.be', 'https://www.secucom.be');

-- Companies (20 companies)
INSERT INTO tcompany (id, name, phone_number, email, iban, security_fund, work_accident_insurance, bce_number, onss_number, legal_form, company_name, creation_date, vat_number, work_regime, salary_reduction, activity_sector, category, work_calendar, collaboration_start_date, subscription_formula, declaration_frequency)
VALUES 
(RANDOM_UUID(), 'TechCorp', '+32 2 555 12 34', 'contact@techcorp.be', 'BE68 5390 0754 7034', '123456-78', 'ETHIAS-789456', '0123456789', '1234567', 'SPRL', 'TechCorp SPRL', '2020-01-15', 'BE0123456789', '40h/semaine', '20%', 'IT Services', 'PME', 'Standard', '2023-01-01', 'Premium', 'Mensuelle'),
(RANDOM_UUID(), 'BelConstruction', '+32 2 555 98 76', 'info@belconstruction.be', 'BE71 3350 0254 9869', '987654-32', 'AG-123789', '9876.543.210', 'RSZ987654321', 'SA', 'BelConstruction SA', '2018-06-20', 'BE9876543210', '38h/semaine', '15%', 'Construction', 'Grande Entreprise', 'Flexible', '2023-03-01', 'Standard', 'Trimestrielle'),
(RANDOM_UUID(), 'FoodCo', '+32 2 555 11 22', 'contact@foodco.be', 'BE72 3350 0254 9870', '123789-45', 'AXA-456123', '4567.890.123', 'RSZ456789012', 'SPRL', 'FoodCo SPRL', '2019-03-15', 'BE4567890123', '38h/semaine', '10%', 'Food Industry', 'PME', 'Flexible', '2023-02-01', 'Basic', 'Mensuelle'),
(RANDOM_UUID(), 'LogiTrans', '+32 2 555 33 44', 'info@logitrans.be', 'BE73 3350 0254 9871', '456123-78', 'ALLIANZ-789123', '7890.123.456', 'RSZ789012345', 'SA', 'LogiTrans SA', '2017-09-10', 'BE7890123456', '40h/semaine', '12%', 'Transport', 'Grande Entreprise', 'Standard', '2023-04-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'GreenEnergy', '+32 2 555 55 66', 'contact@greenenergy.be', 'BE74 3350 0254 9872', '789456-12', 'P&V-123456', '0123.789.456', 'RSZ012345678', 'SA', 'GreenEnergy SA', '2021-01-20', 'BE0123789456', '38h/semaine', '18%', 'Energy', 'PME', 'Flexible', '2023-05-01', 'Standard', 'Mensuelle'),
(RANDOM_UUID(), 'HealthCare Plus', '+32 2 555 77 88', 'info@healthcareplus.be', 'BE75 3350 0254 9873', '654321-90', 'GENERALI-456789', '1234.567.890', 'RSZ123456780', 'SPRL', 'HealthCare Plus SPRL', '2019-08-10', 'BE1234567890', '38h/semaine', '22%', 'Healthcare', 'PME', 'Standard', '2023-06-01', 'Premium', 'Mensuelle'),
(RANDOM_UUID(), 'EduTech Solutions', '+32 2 555 99 00', 'contact@edutech.be', 'BE76 3350 0254 9874', '321654-87', 'BELFIUS-987654', '2345.678.901', 'RSZ234567891', 'SA', 'EduTech Solutions SA', '2020-11-05', 'BE2345678901', '40h/semaine', '16%', 'Education Technology', 'PME', 'Flexible', '2023-07-01', 'Standard', 'Mensuelle'),
(RANDOM_UUID(), 'RetailMax', '+32 2 555 11 33', 'info@retailmax.be', 'BE77 3350 0254 9875', '789012-34', 'KBC-321654', '3456.789.012', 'RSZ345678902', 'SPRL', 'RetailMax SPRL', '2018-04-22', 'BE3456789012', '38h/semaine', '14%', 'Retail', 'Grande Entreprise', 'Standard', '2023-08-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'AutoService Pro', '+32 2 555 22 44', 'contact@autoservice.be', 'BE78 3350 0254 9876', '456789-01', 'CORONA-654321', '4567.890.124', 'RSZ456789013', 'SA', 'AutoService Pro SA', '2017-12-15', 'BE4567890124', '40h/semaine', '18%', 'Automotive', 'PME', 'Standard', '2023-09-01', 'Basic', 'Mensuelle'),
(RANDOM_UUID(), 'FinanceFirst', '+32 2 555 33 55', 'info@financefirst.be', 'BE79 3350 0254 9877', '123456-78', 'ARGENTA-987321', '5678.901.234', 'RSZ567890124', 'SA', 'FinanceFirst SA', '2019-02-28', 'BE5678901234', '38h/semaine', '25%', 'Financial Services', 'Grande Entreprise', 'Flexible', '2023-10-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'CleanTech Industries', '+32 2 555 44 66', 'contact@cleantech.be', 'BE80 3350 0254 9878', '234567-89', 'BALOISE-123987', '6789.012.345', 'RSZ678901235', 'SPRL', 'CleanTech Industries SPRL', '2020-07-12', 'BE6789012345', '40h/semaine', '20%', 'Environmental Technology', 'PME', 'Standard', '2023-11-01', 'Standard', 'Mensuelle'),
(RANDOM_UUID(), 'MediaWorks', '+32 2 555 55 77', 'info@mediaworks.be', 'BE81 3350 0254 9879', '345678-90', 'VIVIUM-456123', '7890.123.457', 'RSZ789012346', 'SA', 'MediaWorks SA', '2018-09-30', 'BE7890123457', '38h/semaine', '17%', 'Media & Communication', 'PME', 'Flexible', '2023-12-01', 'Basic', 'Mensuelle'),
(RANDOM_UUID(), 'SportLife', '+32 2 555 66 88', 'contact@sportlife.be', 'BE82 3350 0254 9880', '456789-01', 'INTEGRALE-789456', '8901.234.567', 'RSZ890123457', 'SPRL', 'SportLife SPRL', '2021-03-18', 'BE8901234567', '40h/semaine', '15%', 'Sports & Recreation', 'PME', 'Standard', '2024-01-01', 'Premium', 'Mensuelle'),
(RANDOM_UUID(), 'TravelExpert', '+32 2 555 77 99', 'info@travelexpert.be', 'BE83 3350 0254 9881', '567890-12', 'TOURING-012345', '9012.345.678', 'RSZ901234568', 'SA', 'TravelExpert SA', '2019-11-25', 'BE9012345678', '38h/semaine', '19%', 'Tourism & Travel', 'PME', 'Flexible', '2024-02-01', 'Standard', 'Mensuelle'),
(RANDOM_UUID(), 'AgriTech Solutions', '+32 2 555 88 00', 'contact@agritech.be', 'BE84 3350 0254 9882', '678901-23', 'FIDEA-345678', '0123.456.790', 'RSZ012345679', 'SPRL', 'AgriTech Solutions SPRL', '2020-05-14', 'BE0123456790', '40h/semaine', '21%', 'Agriculture Technology', 'PME', 'Standard', '2024-03-01', 'Basic', 'Mensuelle'),
(RANDOM_UUID(), 'SecureTech', '+32 2 555 99 11', 'info@securetech.be', 'BE85 3350 0254 9883', '789012-34', 'LLOYD-678901', '1234.567.891', 'RSZ123456790', 'SA', 'SecureTech SA', '2018-01-08', 'BE1234567891', '38h/semaine', '23%', 'Security Technology', 'Grande Entreprise', 'Standard', '2024-04-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'BioPharma', '+32 2 555 00 22', 'contact@biopharma.be', 'BE86 3350 0254 9884', '890123-45', 'EUROP-901234', '2345.678.902', 'RSZ234567801', 'SA', 'BioPharma SA', '2017-06-19', 'BE2345678902', '40h/semaine', '26%', 'Pharmaceutical', 'Grande Entreprise', 'Flexible', '2024-05-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'DesignStudio', '+32 2 555 11 44', 'info@designstudio.be', 'BE87 3350 0254 9885', '901234-56', 'SIGNAL-234567', '3456.789.013', 'RSZ345678912', 'SPRL', 'DesignStudio SPRL', '2021-08-03', 'BE3456789013', '38h/semaine', '13%', 'Design & Creative', 'PME', 'Flexible', '2024-06-01', 'Standard', 'Mensuelle'),
(RANDOM_UUID(), 'LogisticsPro', '+32 2 555 22 55', 'contact@logisticspro.be', 'BE88 3350 0254 9886', '012345-67', 'ACERTA-567890', '4567.890.125', 'RSZ456789023', 'SA', 'LogisticsPro SA', '2019-10-17', 'BE4567890125', '40h/semaine', '16%', 'Logistics', 'Grande Entreprise', 'Standard', '2024-07-01', 'Premium', 'Trimestrielle'),
(RANDOM_UUID(), 'ConsultingPlus', '+32 2 555 33 66', 'info@consultingplus.be', 'BE89 3350 0254 9887', '123456-78', 'LIANTIS-890123', '5678.901.235', 'RSZ567890134', 'SPRL', 'ConsultingPlus SPRL', '2020-12-09', 'BE5678901235', '38h/semaine', '24%', 'Business Consulting', 'PME', 'Flexible', '2024-08-01', 'Standard', 'Mensuelle');

-- Link SecretariatEmployees to SocialSecretariat
UPDATE tuser SET secretariat_id = (SELECT id FROM tsocial_secretariat LIMIT 1) WHERE username IN ('secretariat1', 'secretariat2');

-- Link CompanyContacts to Companies
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'TechCorp' LIMIT 1) WHERE username = 'company1';
UPDATE tuser SET company_id = (SELECT id FROM tcompany WHERE name = 'BelConstruction' LIMIT 1) WHERE username = 'company2';

-- Insert Collaborators (50 collaborators distributed across companies)
INSERT INTO tcollaborator (id, last_name, first_name, nationality, birth_date, birth_place, gender, language, civil_status, national_number, service_entry_date, type, job_function, contract_type, work_regime, work_duration_type, salary, joint_committee, task_description, iban, company_id, address_street, address_number, address_postal_code, address_city, address_country, establishment_street, establishment_number, establishment_postal_code, establishment_city, establishment_country, created_at, updated_at)
SELECT 
    RANDOM_UUID(), 
    'Collaborateur' || ROWNUM(), 
    'Prénom' || ROWNUM(), 
    'Belge', 
    DATE '1980-01-01' + (RAND() * 15000), 
    'Bruxelles', 
    CASE WHEN RAND() > 0.5 THEN 'M' ELSE 'F' END,
    CASE WHEN RAND() > 0.5 THEN 'FR' ELSE 'NL' END,
    CASE WHEN RAND() > 0.5 THEN 'Marié' ELSE 'Célibataire' END,
    LPAD(CAST((80 + RAND() * 20) AS VARCHAR), 2, '0') || LPAD(CAST(RAND() * 100000000 AS VARCHAR), 8, '0'),
    DATE '2023-01-01' + (RAND() * 365),
    CAST(RAND() * 2 AS INT),
    'Fonction' || ROWNUM(),
    'CDI',
    'Temps plein',
    0,
    2500.00 + (RAND() * 3000),
    '200',
    'Description tâche ' || ROWNUM(),
    'BE' || LPAD(CAST(RAND() * 100000000000000 AS VARCHAR), 14, '0'),
    (SELECT id FROM tcompany ORDER BY RAND() LIMIT 1),
    'Rue Test',
    CAST(1 + RAND() * 100 AS VARCHAR),
    '1000',
    'Bruxelles',
    'Belgique',
    'Rue Test',
    CAST(1 + RAND() * 100 AS VARCHAR),
    '1000',
    'Bruxelles',
    'Belgique',
    CURRENT_DATE,
    CURRENT_DATE
FROM SYSTEM_RANGE(1, 50);

-- Insert Dimonas (20 dimonas)
INSERT INTO tdimona (id, entry_date, exit_date, exit_reason, onss_reference, status, type, error_message, collaborator_id, company_id)
SELECT 
    RANDOM_UUID(),
    CURRENT_TIMESTAMP - (RAND() * 365),
    NULL,
    NULL,
    'REF' || LPAD(CAST(ROWNUM() AS VARCHAR), 6, '0'),
    'ACCEPTED',
    'ENTREE',
    NULL,
    (SELECT id FROM tcollaborator ORDER BY RAND() LIMIT 1),
    (SELECT id FROM tcompany ORDER BY RAND() LIMIT 1)
FROM SYSTEM_RANGE(1, 20);