# Système de Génération de Documents - SecuCom

## Vue d'ensemble

Le système de génération de documents permet de créer automatiquement des contrats et autres documents en utilisant des templates DOCX et en remplissant automatiquement les variables avec les données des entités du système (Company, Collaborator, etc.).

## Architecture

### Composants principaux

1. **DocumentTemplate** : Entité représentant un template de document
2. **DocumentGeneration** : Entité représentant une génération de document
3. **DocumentTemplateService** : Service pour gérer les templates
4. **DocumentGenerationService** : Service pour générer les documents
5. **DocumentController** : Contrôleur REST pour les APIs

### Fichiers de configuration

- **Templates DOCX** : Stockés dans `src/main/resources/`
- **Mappings JSON** : Stockés dans `src/main/resources/templates/mappings/`
- **Documents générés** : Stockés dans `src/main/resources/generated-documents/`

## Configuration

### Template CNT_Employe

Le template de contrat de travail employé est configuré avec :

- **Fichier template** : `CNT_Employe.docx`
- **Configuration** : `templates/mappings/CNT_Employe.json`
- **Variables supportées** :
  - Données entreprise : nom, adresse, numéros BCE/ONSS/TVA
  - Données collaborateur : nom, prénom, adresse, fonction, salaire
  - Données manuelles : dates, type de contrat, durée de travail

### Variables dans les templates

Les variables dans les templates DOCX doivent être au format `{{nom_variable}}`.

Exemple : `{{entreprise_nom}}`, `{{collaborateur_prenom}}`, `{{date_debut_contrat}}`

## APIs REST

### Endpoints disponibles

#### Templates
- `GET /api/documents/templates` - Liste tous les templates actifs
- `GET /api/documents/templates/{templateId}` - Détails d'un template
- `GET /api/documents/templates/{templateId}/variables` - Variables d'un template
- `GET /api/documents/templates/by-name/{templateName}/variables` - Variables par nom de template

#### Génération
- `POST /api/documents/generate` - Générer un document
- `GET /api/documents/generations` - Historique des générations
- `GET /api/documents/generations/{generationId}` - Détails d'une génération
- `GET /api/documents/generations/{generationId}/download` - Télécharger le document DOCX

### Exemple d'utilisation

#### 1. Récupérer les templates disponibles
```http
GET /api/documents/templates
```

Réponse :
```json
[
  {
    "id": "uuid",
    "name": "CNT_Employe",
    "displayName": "Contrat de travail employé",
    "description": "Template pour générer un contrat de travail pour un employé",
    "active": true,
    "variables": [...]
  }
]
```

#### 2. Récupérer les variables d'un template
```http
GET /api/documents/templates/by-name/CNT_Employe/variables
```

Réponse :
```json
[
  {
    "name": "entreprise_nom",
    "displayName": "Nom de l'entreprise",
    "entity": "Company",
    "field": "name",
    "type": "string",
    "required": true
  },
  {
    "name": "date_debut_contrat",
    "displayName": "Date de début du contrat",
    "entity": "manual",
    "field": null,
    "type": "date",
    "required": true
  }
]
```

#### 3. Générer un document
```http
POST /api/documents/generate
Content-Type: application/json

{
  "templateId": "uuid-du-template",
  "companyId": "uuid-de-lentreprise",
  "collaboratorId": "uuid-du-collaborateur",
  "manualFields": {
    "date_debut_contrat": "2025-01-15",
    "type_contrat": "CDI",
    "duree_travail": "38h/semaine",
    "lieu_signature": "Bruxelles",
    "date_signature": "2025-01-10"
  }
}
```

Réponse :
```json
{
  "id": "uuid-generation",
  "templateName": "CNT_Employe",
  "templateDisplayName": "Contrat de travail employé",
  "companyName": "Ma Société SPRL",
  "collaboratorName": "Jean Dupont",
  "generatedFileName": "CNT_Employe_uuid_20250107_143022.docx",
  "status": "COMPLETED",
  "createdAt": "2025-01-07T14:30:22",
  "formData": {...}
}
```

#### 4. Télécharger le document généré
```http
GET /api/documents/generations/{generationId}/download
```

## Flux utilisateur

1. **Sélection du template** : L'utilisateur choisit "Contrat de travail employé"
2. **Formulaire dynamique** : Le système affiche un formulaire avec :
   - Sélecteur d'entreprise (dropdown des companies)
   - Sélecteur de collaborateur (dropdown des collaborators)
   - Champs manuels (dates, type de contrat, etc.)
3. **Génération** : L'utilisateur soumet le formulaire
4. **Téléchargement** : Le document DOCX est généré et peut être téléchargé
5. **Historique** : L'utilisateur peut consulter l'historique et re-télécharger

## Mapping des données

### Entité Company
- `name` → `{{entreprise_nom}}`
- `companyName` → `{{entreprise_denomination}}`
- `address.street` → `{{entreprise_rue}}`
- `address.number` → `{{entreprise_numero}}`
- `address.postalCode` → `{{entreprise_code_postal}}`
- `address.city` → `{{entreprise_ville}}`
- `bceNumber` → `{{entreprise_bce}}`
- `onssNumber` → `{{entreprise_onss}}`
- `vatNumber` → `{{entreprise_tva}}`

### Entité Collaborator
- `firstName` → `{{collaborateur_prenom}}`
- `lastName` → `{{collaborateur_nom}}`
- `address.street` → `{{collaborateur_rue}}`
- `address.number` → `{{collaborateur_numero}}`
- `address.postalCode` → `{{collaborateur_code_postal}}`
- `address.city` → `{{collaborateur_ville}}`
- `nationality` → `{{collaborateur_nationalite}}`
- `birthDate` → `{{collaborateur_date_naissance}}`
- `birthPlace` → `{{collaborateur_lieu_naissance}}`
- `nationalNumber` → `{{collaborateur_numero_national}}`
- `jobFunction` → `{{collaborateur_fonction}}`
- `salary` → `{{collaborateur_salaire}}`

### Champs manuels
- `date_debut_contrat` : Date de début du contrat
- `date_fin_contrat` : Date de fin (optionnel)
- `type_contrat` : CDI, CDD, Intérim, Stage
- `duree_travail` : Durée de travail
- `periode_essai` : Période d'essai (optionnel)
- `lieu_signature` : Lieu de signature
- `date_signature` : Date de signature

## Évolutions futures

### PDF Conversion
Actuellement, seuls les documents DOCX sont générés. La conversion PDF peut être ajoutée en :
1. Installant LibreOffice headless
2. Implémentant la méthode `convertToPdf()` dans `DocumentGenerationService`
3. Utilisant la commande : `soffice --headless --convert-to pdf --outdir outputPath docxFilePath`

### Nouveaux templates
Pour ajouter un nouveau template :
1. Créer le fichier DOCX avec les variables `{{nom_variable}}`
2. Créer le fichier JSON de mapping dans `templates/mappings/`
3. Ajouter l'initialisation dans `DocumentTemplateInitializer`

### Envoi par email
L'envoi par email peut être ajouté en intégrant le système d'email existant du projet publiPostage.

## Sécurité

- Authentification JWT requise pour tous les endpoints
- Validation des permissions sur les entités référencées
- Stockage sécurisé des documents générés
- Historique des générations par utilisateur
