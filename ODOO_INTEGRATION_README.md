# Intégration Odoo - Service de création de tâches

Ce document décrit l'intégration avec Odoo pour la création automatique de tâches dans les projets.

## Configuration

Ajoutez les propriétés suivantes dans votre fichier `application.properties` :

```properties
# Odoo Configuration
odoo.url=https://mehdi-corp.odoo.com
odoo.database=mehdi-corp
odoo.username=ton.email@exemple.com
odoo.password=TON_MDP
```

## Utilisation du service

Le service `OdooService` fournit deux méthodes principales :

### 1. Recherche ou création d'un partenaire

```java
@Autowired
private OdooService odooService;

// Recherche ou crée un partenaire dans Odoo
Long partnerId = odooService.findOrCreatePartner(
    "123456789",           // Numéro BCE de l'entreprise
    "Nouvelle Société",    // Nom de l'entreprise
    "societe@email.com",   // Email
    "+32470000000"         // Téléphone
);
```

### 2. Création d'une tâche

```java
// Crée une tâche dans un projet Odoo
Long taskId = odooService.createTask(
    "Créer un site web",                    // Nom de la tâche
    "Développement d'un site vitrine",      // Description
    "2025-08-05",                          // Date limite (YYYY-MM-DD)
    "2",                                   // Priorité (0=Low, 1=Normal, 2=High, 3=Very High)
    partnerId,                             // ID du partenaire (peut être null)
    1L                                     // ID du projet Odoo
);
```

## Exemple d'utilisation complète

```java
@Service
public class ExampleService {
    
    @Autowired
    private OdooService odooService;
    
    public void createTaskForNewCollaborator(Company company, Collaborator collaborator) {
        try {
            // 1. Recherche ou crée le partenaire
            Long partnerId = odooService.findOrCreatePartner(
                company.getBceNumber(),
                company.getName(),
                company.getEmail(),
                company.getPhoneNumber()
            );
            
            // 2. Crée la tâche
            Long taskId = odooService.createTask(
                "Nouveau collaborateur : " + collaborator.getFirstName() + " " + collaborator.getLastName(),
                "Intégration du nouveau collaborateur dans l'entreprise " + company.getName(),
                LocalDate.now().plusDays(7).toString(), // Échéance dans 7 jours
                "1", // Priorité normale
                partnerId,
                1L // ID du projet par défaut
            );
            
            log.info("Tâche Odoo créée avec succès : ID = {}", taskId);
            
        } catch (OdooIntegrationException e) {
            log.error("Erreur lors de la création de la tâche Odoo", e);
            // Gérer l'erreur selon vos besoins
        }
    }
}
```

## Gestion des erreurs

Le service lance une `OdooIntegrationException` en cas d'erreur :
- Problème de connexion à Odoo
- Erreur d'authentification
- Erreur lors de la création des données

## Points d'intégration suggérés

Vous pouvez appeler ces méthodes dans vos services existants :

- **CollaboratorService.createCollaborator()** : Créer une tâche "Nouveau collaborateur"
- **CollaboratorService.deleteCollaborator()** : Créer une tâche "Fin de collaboration"
- **CompanyService** : Créer une tâche "Nouvelle entreprise"
- **Lors de la fin d'un contrat** : Créer une tâche "Fin de contrat"

## Notes techniques

- Le service utilise XML-RPC pour communiquer avec Odoo
- L'authentification est gérée automatiquement
- Les clients XML-RPC sont réutilisés pour optimiser les performances
- Tous les appels sont synchrones
