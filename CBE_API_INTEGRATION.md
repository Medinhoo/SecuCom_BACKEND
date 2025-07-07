# Intégration API CBE (Crossroads Bank for Enterprises)

## Vue d'ensemble

Cette intégration permet de rechercher des informations d'entreprises belges via l'API CBE officielle. Elle fournit des endpoints pour rechercher des entreprises par numéro BCE, numéro TVA ou nom.

## Configuration

### 1. Token API CBE

Obtenez votre token API depuis le dashboard CBE API et ajoutez-le dans votre fichier `application.properties` :

```properties
# CBE API Configuration
cbe.api.base-url=https://cbeapi.be/api/v1
cbe.api.token=VOTRE_TOKEN_CBE_API
```

### 2. Variables d'environnement (recommandé pour la production)

```bash
export CBE_API_TOKEN=votre_token_ici
```

## Endpoints disponibles

### 1. Vérification d'existence en base de données

- `GET /api/company/check/bce/{bceNumber}` - Vérifie si le numéro BCE existe déjà en DB
- `GET /api/company/check/vat/{vatNumber}` - Vérifie si le numéro TVA existe déjà en DB

### 2. Recherche via API CBE externe

- `GET /api/company/lookup/bce/{bceNumber}` - Recherche par numéro BCE
- `GET /api/company/lookup/vat/{vatNumber}` - Recherche par numéro TVA
- `GET /api/company/lookup/search?name={nom}` - Recherche par nom d'entreprise

## Flow d'utilisation recommandé

```javascript
// 1. Vérifier si l'entreprise existe déjà en DB
const existsResponse = await fetch('/api/company/check/bce/0783225603');
const exists = await existsResponse.json();

if (!exists) {
    // 2. Si elle n'existe pas, rechercher via l'API CBE
    const lookupResponse = await fetch('/api/company/lookup/bce/0783225603');
    const companyData = await lookupResponse.json();
    
    // 3. Pré-remplir le formulaire avec les données trouvées
    fillForm(companyData);
}
```

## Format de réponse

### CompanyLookupDto

```json
{
    "bceNumber": "0783225603",
    "bceNumberFormatted": "0783.225.603",
    "name": "ACME",
    "companyName": "ACME SRL",
    "legalForm": "Société à responsabilité limitée",
    "legalFormShort": "SRL",
    "email": "info@example.com",
    "phoneNumber": "+32 123 45 67 89",
    "website": "example.com",
    "address": {
        "street": "Rue des Looney Tunes",
        "number": "42",
        "box": "",
        "postalCode": "4000",
        "city": "Liège",
        "country": null
    },
    "startDate": null
}
```

## Gestion d'erreurs

L'API gère automatiquement les erreurs suivantes :

- **404** : Entreprise non trouvée dans la base CBE
- **401** : Token CBE API invalide
- **429** : Limite de taux CBE API dépassée
- **503** : Service CBE API indisponible
- **502** : Erreur de communication avec l'API CBE

## Formats de numéros supportés

L'API CBE accepte les formats suivants :
- `1234567890` (10 chiffres)
- `BE1234567890` (avec préfixe BE)
- `1234.567.890` (avec points)

## Sécurité

- Le token API est stocké côté serveur et jamais exposé au frontend
- Tous les appels sont authentifiés via JWT
- Les endpoints respectent les rôles ADMIN et SECRETARIAT

## Logs et monitoring

Les appels API sont loggés avec les informations suivantes :
- Numéro/nom recherché
- Succès/échec de l'appel
- Temps de réponse
- Erreurs détaillées

## Exemple d'utilisation complète

```javascript
async function handleBceInput(bceNumber) {
    try {
        // Vérifier existence en DB
        const existsResponse = await fetch(`/api/company/check/bce/${bceNumber}`);
        const exists = await existsResponse.json();
        
        if (exists) {
            showMessage('Cette entreprise existe déjà dans le système');
            return;
        }
        
        // Rechercher via CBE API
        const lookupResponse = await fetch(`/api/company/lookup/bce/${bceNumber}`);
        
        if (!lookupResponse.ok) {
            throw new Error('Entreprise non trouvée');
        }
        
        const companyData = await lookupResponse.json();
        
        // Pré-remplir le formulaire
        document.getElementById('name').value = companyData.name || '';
        document.getElementById('companyName').value = companyData.companyName || '';
        document.getElementById('legalForm').value = companyData.legalForm || '';
        document.getElementById('email').value = companyData.email || '';
        document.getElementById('phoneNumber').value = companyData.phoneNumber || '';
        
        if (companyData.address) {
            document.getElementById('street').value = companyData.address.street || '';
            document.getElementById('number').value = companyData.address.number || '';
            document.getElementById('postalCode').value = companyData.address.postalCode || '';
            document.getElementById('city').value = companyData.address.city || '';
        }
        
        showMessage('Informations pré-remplies depuis la base CBE');
        
    } catch (error) {
        showError('Erreur lors de la recherche : ' + error.message);
    }
}
