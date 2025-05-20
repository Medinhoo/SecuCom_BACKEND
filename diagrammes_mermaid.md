# Diagrammes MermaidJS pour la section 6 (Analyse)

## 6.1 Diagramme de composants

```mermaid
graph TD
    A[SecuCom] --> B[Module Gestion des Utilisateurs]
    A --> C[Module Gestion des Entreprises]
    A --> D[Module Gestion des Collaborateurs]
    A --> E[Module Gestion des DIMONA]
    A --> F[Module Gestion des Notifications]
    
    B --> B3[Authentification]
    B --> B4[Audit et traçabilité]
    B --> B1[Gestion des profils]
    B --> B2[Gestion des rôles et permissions]
    
    C --> C2[Gestion CRUD des contacts d'entreprise]
    C --> C1[Gestion CRUD d'entreprises]
    
    D --> D1[Gestion CRUD des collaborateurs]
    D --> D2[Gestion des informations personnelles]
    
    E --> E1[Gestion CRUD de déclarations]
    E --> E3[Gestion des statuts des déclarations]
    
    F --> F1[Gestion des notifications]
    F --> F3[Historique des notifications]
    
    B2 -.-> C2
    C1 -.-> D1
    D1 -.-> E1
    
    %% Connexions avec le module de notifications
    E3 -.-> F1
    D1 -.-> F1
```

## 6.2 Diagramme de classes

```mermaid
classDiagram
    %% Disposition optimisée pour éviter les chevauchements
    
    %% Définition des énumérations
    class Role {
        <<enumeration>>
        ROLE_COMPANY
        ROLE_SECRETARIAT
        ROLE_ADMIN
    }
    
    class AccountStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        LOCKED
        PENDING
    }
    
    class CollaboratorType {
        <<enumeration>>
        EMPLOYEE
        WORKER
        FREELANCE
        INTERN
        STUDENT
    }
    
    class WorkDurationType {
        <<enumeration>>
        FIXED
        VARIABLE
    }
    
    class DimonaStatus {
        <<enumeration>>
        TO_SEND
        TO_CONFIRM
        TO_CORRECT
        CREATED
        REJECTED
    }
    
    class NotificationType {
        <<enumeration>>
        DIMONA_CREATED
        DIMONA_STATUS_CHANGED
        COLLABORATOR_CREATED
    }
    
    %% Définition des classes
    class User {
        -UUID id
        -String firstName
        -String lastName
        -String username
        -String email
        -String password
        -String phoneNumber
        -Set~Role~ roles
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -LocalDateTime lastLogin
        -AccountStatus accountStatus
        +addRole(Role) void
        +hasRole(Role) boolean
    }
    
    class SocialSecretariat {
        -UUID id
        -String name
        -String bceNumber
        -Set~SecretariatEmployee~ employees
        +addEmployee(SecretariatEmployee) void
        +removeEmployee(SecretariatEmployee) void
    }
    
    class SecretariatEmployee {
        -UUID id
        -String position
        -String specialization
        -SocialSecretariat secretariat
    }
    
    class Company {
        -UUID id
        -String name
        -String phoneNumber
        -String email
        -String bceNumber
        -String onssNumber
        -String legalForm
        -String vatNumber
        -String activitySector
        -String IBAN
        -String securityFund
        -String workAccidentInsurance
        -String companyName
        -LocalDate creationDate
        -String workRegime
        -List~String~ jointCommittees
        -Set~CompanyContact~ contacts
        -Set~Collaborator~ collaborators
        +addContact(CompanyContact) void
        +removeContact(CompanyContact) void
        +addCollaborator(Collaborator) void
        +removeCollaborator(Collaborator) void
    }
    
    class CompanyContact {
        -UUID id
        -String position
        -Company company
        -Set~Permission~ permissions
    }
    
    class Collaborator {
        -UUID id
        -String firstName
        -String lastName
        -String nationalNumber
        -String nationality
        -LocalDate birthDate
        -String birthPlace
        -String gender
        -String language
        -String civilStatus
        -CollaboratorType type
        -String jobFunction
        -String contractType
        -String workRegime
        -WorkDurationType workDurationType
        -BigDecimal salary
        -String jointCommittee
        -String iban
        -Company company
        -Address address
        -Address establishmentUnitAddress
    }
    
    class Dimona {
        -UUID id
        -String type
        -Date entryDate
        -Date exitDate
        -String exitReason
        -DimonaStatus status
        -String onssReference
        -String errorMessage
        -Collaborator collaborator
        -Company company
    }
    
    class Address {
        -String street
        -String number
        -String box
        -String postalCode
        -String city
        -String country
    }
    
    class Notification {
        -UUID id
        -String message
        -NotificationType type
        -boolean read
        -LocalDateTime createdAt
        -User recipient
        -UUID entityId
    }
    
    %% Relations d'héritage
    User <|-- SecretariatEmployee
    User <|-- CompanyContact
    
    %% Relations d'association - Groupe 1
    User "*" -- "*" Role : has
    
    %% Relations d'association - Groupe 2
    SocialSecretariat "1" *-- "*" SecretariatEmployee : employs
    SocialSecretariat "1" --> "0..1" Address : located at
    
    %% Relations d'association - Groupe 3
    Company "1" *-- "*" CompanyContact : has
    Company "1" *-- "*" Collaborator : employs
    Company "1" --> "0..1" Address : located at
    
    %% Relations d'association - Groupe 4
    Collaborator "1" --> "1" Address : has
    Collaborator "1" --> "1" Address : establishment
    
    %% Relations d'association - Groupe 5
    Company "1" o-- "*" Dimona : declares
    Collaborator "1" o-- "*" Dimona : associated with
    
    %% Relations d'association - Groupe 6
    User "1" -- "*" Notification : receives
```

## 6.3 Diagramme d'entités relationnelles

```mermaid
erDiagram
    TUSER {
        UUID id PK
        string firstName
        string lastName
        string username
        string email
        string password
        string phoneNumber
        string DTYPE
        datetime createdAt
        datetime updatedAt
        datetime lastLogin
        string accountStatus
        string position
        string specialization
        UUID secretariat_id FK
        string fonction
        string permissions
        UUID company_id FK
    }
    
    USER_ROLES {
        UUID user_id FK
        string roles
    }
    
    TSOCIAL_SECRETARIAT {
        UUID id PK
        string name
        string companyNumber
        string address
        string phone
        string email
        string website
    }
    
    TCOMPANY {
        UUID id PK
        string name
        string phoneNumber
        string email
        string IBAN
        string securityFund
        string workAccidentInsurance
        string bceNumber
        string onssNumber
        string legalForm
        string companyName
        date creationDate
        string vatNumber
        string workRegime
        string salaryReduction
        string activitySector
        string category
        string workCalendar
        date collaborationStartDate
        string subscriptionFormula
        string declarationFrequency
    }
    
    COMPANY_JOINT_COMMITTEES {
        UUID company_id FK
        string joint_committee
    }
    
    TCOLLABORATOR {
        UUID id PK
        string lastName
        string firstName
        string nationality
        date birthDate
        string birthPlace
        string gender
        string language
        string civilStatus
        date civilStatusDate
        string partnerName
        date partnerBirthDate
        string nationalNumber
        date serviceEntryDate
        int type
        string jobFunction
        string contractType
        string workRegime
        int workDurationType
        decimal salary
        string jointCommittee
        string taskDescription
        string iban
        UUID company_id FK
        date createdAt
        date updatedAt
        string address_street
        string address_number
        string address_box
        string address_postal_code
        string address_city
        string address_country
        string establishment_street
        string establishment_number
        string establishment_box
        string establishment_postal_code
        string establishment_city
        string establishment_country
    }
    
    COLLABORATOR_DEPENDENTS {
        UUID collaborator_id FK
        string dependents
    }
    
    COLLABORATOR_EXTRA_LEGAL_BENEFITS {
        UUID collaborator_id FK
        string extra_legal_benefits
    }
    
    COLLABORATOR_SCHEDULE {
        UUID collaborator_id FK
        string day
        string schedule
    }
    
    TDIMONA {
        UUID id PK
        string type
        date entryDate
        date exitDate
        string exitReason
        int status
        string onssReference
        string errorMessage
        UUID collaborator_id FK
        UUID company_id FK
    }
    
    TNOTIFICATION {
        UUID id PK
        string message
        string type
        boolean read
        datetime createdAt
        UUID recipient_id FK
        UUID entityId
    }
    
    TUSER ||--o{ USER_ROLES : ""
    TUSER }o--|| TSOCIAL_SECRETARIAT : ""
    TUSER }o--|| TCOMPANY : ""
    TUSER ||--o{ TNOTIFICATION : ""
    
    TCOMPANY ||--o{ COMPANY_JOINT_COMMITTEES : ""
    TCOMPANY ||--o{ TCOLLABORATOR : ""
    TCOMPANY ||--o{ TDIMONA : ""
    
    TCOLLABORATOR ||--o{ COLLABORATOR_DEPENDENTS : ""
    TCOLLABORATOR ||--o{ COLLABORATOR_EXTRA_LEGAL_BENEFITS : ""
    TCOLLABORATOR ||--o{ COLLABORATOR_SCHEDULE : ""
    TCOLLABORATOR ||--o{ TDIMONA : ""
```

## 6.4 Diagrammes de séquences

### 6.4.1 Cas d'utilisation : Création d'une entreprise

```mermaid
sequenceDiagram
    actor Admin as Administrateur
    actor CC as Contact Entreprise
    actor ESS as Employé Secrétariat Social
    participant Sys as Système SecuCom
    
    Admin->>Sys: Créer compte utilisateur (type company)
    Sys-->>Admin: Afficher confirmation
    
    Note over Admin,CC: Transmission des identifiants<br/>(hors système)
    
    CC->>Sys: Se connecter au système
    Sys-->>CC: Afficher formulaire d'informations entreprise
    CC->>Sys: Compléter et soumettre informations entreprise
    Sys->>Sys: Valider données
    
    alt Données valides
        Sys-->>CC: Afficher confirmation
        ESS->>Sys: Accéder aux informations entreprise
        Sys-->>ESS: Afficher informations entreprise
    else Données invalides
        Sys-->>CC: Afficher erreurs de validation
    end
```

### 6.4.2 Cas d'utilisation : Ajout d'un employé

```mermaid
sequenceDiagram
    actor CE as Contact Entreprise
    actor SS as Secrétariat Social
    participant Sys as Système SecuCom
    
    alt Initiation par le Contact Entreprise
        CE->>Sys: Initier ajout d'un collaborateur
        Sys-->>CE: Afficher formulaire d'ajout
        CE->>Sys: Remplir et soumettre le formulaire
        Sys-->>CE: Confirmer création du collaborateur
        
        Sys->>SS: Notifier nouvel ajout de collaborateur
        SS->>Sys: Consulter données du collaborateur
        Sys-->>SS: Afficher détails collaborateur
        
    else Initiation par le Secrétariat Social
        SS->>Sys: Initier ajout d'un collaborateur
        Sys-->>SS: Afficher formulaire d'ajout
        SS->>Sys: Remplir et soumettre le formulaire
        Sys-->>SS: Confirmer création du collaborateur
        
        Sys->>CE: Notifier ajout d'un collaborateur
        CE->>Sys: Consulter données du collaborateur
        Sys-->>CE: Afficher détails collaborateur
    end
```

### 6.4.3 Cas d'utilisation : Création d'une déclaration DIMONA

```mermaid
sequenceDiagram
    actor CE as Contact Entreprise
    actor SS as Secrétariat Social
    participant Sys as Système SecuCom
    participant ONSS as Site ONSS (Externe)
    
    alt Initiation par le Contact Entreprise
        CE->>Sys: Initier création DIMONA
        Sys-->>CE: Afficher formulaire DIMONA
        CE->>Sys: Soumettre données DIMONA
        Sys-->>CE: Confirmer réception demande
        Sys-->>Sys: Mettre à jour statut DIMONA en "À envoyer"
        
        Sys->>SS: Notifier nouvelle demande DIMONA
        SS->>Sys: Consulter demande DIMONA
        Sys-->>SS: Afficher détails demande

        
    else Initiation par le Secrétariat Social
        SS->>Sys: Initier création DIMONA
        Sys-->>SS: Afficher formulaire DIMONA
        SS->>Sys: Soumettre données DIMONA
        Sys-->>SS: Confirmer enregistrement
        
        Sys-->>Sys: Mettre à jour statut DIMONA en "À confirmer"
        Sys->>CE: Notifier nouvelle demande DIMONA
        CE->>Sys: Consulter demande DIMONA
        Sys-->>CE: Afficher détails demande
        CE->>Sys: Confirmer données demande DIMONA
        Sys-->>Sys: Mettre à jour statut DIMONA en "À envoyer"
        Sys->>SS: Notifier demande DIMONA à envoyer

    end
    
    SS->>ONSS: Créer DIMONA sur le site de l'ONSS
    
    alt DIMONA acceptée par l'ONSS
        ONSS-->>SS: Confirmer création DIMONA
        
        SS->>Sys: Mettre à jour statut DIMONA (créée)
        Sys-->>CE: Notifier création DIMONA
    else DIMONA refusée ou données insuffisantes
        ONSS-->>SS: Refuser DIMONA / Signaler données insuffisantes
        
        SS->>Sys: Changer statut en "à corriger"
        Sys-->>CE: Notifier besoin de correction
        
        CE->>Sys: Modifier données DIMONA
        Sys-->>Sys: Mettre à jour statut DIMONA en "À envoyer"
        Sys-->>SS: Notifier corrections effectuées
        
        SS->>ONSS: Créer DIMONA sur le site de l'ONSS (nouvelle tentative)
        ONSS-->>SS: Confirmer création DIMONA
        
        SS->>Sys: Mettre à jour statut DIMONA (créée)
        Sys-->>CE: Notifier création DIMONA
    end
```

## 6.5 Diagramme d'activité

```mermaid
stateDiagram-v2
    [*] --> CreationEntreprise
    CreationEntreprise --> AjoutContact: Entreprise créée
    AjoutContact --> AjoutCollaborateur: Contact ajouté
    
    AjoutCollaborateur --> CreationDIMONA: Collaborateur ajouté
    CreationDIMONA --> SuiviDIMONA: DIMONA créée
    
    state SuiviDIMONA {
        [*] --> EnAttente
        EnAttente --> Acceptee: Confirmation ONSS
        EnAttente --> Rejetee: Erreur détectée
        Rejetee --> Corrigee: Correction effectuée
        Corrigee --> EnAttente: Nouvelle soumission
    }
    
    SuiviDIMONA --> EnregistrementPrestations: DIMONA acceptée
    EnregistrementPrestations --> GenerationFichePaie: Prestations validées
    GenerationFichePaie --> [*]: Fiche de paie générée
    
    state fork_state <<fork>>
    GenerationFichePaie --> fork_state
    fork_state --> ArchivageDocuments
    fork_state --> EnvoiDocuments
    
    ArchivageDocuments --> [*]
    EnvoiDocuments --> [*]
