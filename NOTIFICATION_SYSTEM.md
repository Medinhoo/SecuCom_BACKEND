# Système de Notifications SecuCom

Ce document décrit le système de notifications implémenté dans SecuCom, conforme aux spécifications du TFE.

## Vue d'ensemble

Le système de notifications permet d'informer automatiquement les utilisateurs des événements importants dans l'application, notamment :
- Création de nouveaux collaborateurs
- Création de déclarations DIMONA
- Changements de statut des déclarations DIMONA

## Architecture

### Entités principales

#### Notification
- **Table** : `TNotification`
- **Champs principaux** :
  - `id` : Identifiant unique (UUID)
  - `message` : Contenu de la notification (max 500 caractères)
  - `type` : Type de notification (enum)
  - `is_read` : Statut de lecture (boolean)
  - `created_at` : Date de création
  - `recipient_id` : Référence vers l'utilisateur destinataire
  - `entity_id` : Référence vers l'entité concernée (optionnel)

#### NotificationType (Enum)
- `DIMONA_CREATED` : Nouvelle déclaration DIMONA créée
- `DIMONA_STATUS_CHANGED` : Statut d'une déclaration DIMONA modifié
- `COLLABORATOR_CREATED` : Nouveau collaborateur ajouté

### Composants

#### NotificationService
Service principal gérant toute la logique métier des notifications :
- Création de notifications
- Récupération des notifications par utilisateur
- Marquage comme lu/non lu
- Suppression et nettoyage automatique
- Méthodes spécialisées pour chaque type d'événement

#### NotificationRepository
Repository Spring Data JPA avec des méthodes optimisées :
- Recherche par utilisateur avec tri par date
- Filtrage des notifications non lues
- Comptage des notifications non lues
- Marquage en lot comme lu
- Suppression automatique des anciennes notifications

#### NotificationController
API REST exposant les fonctionnalités aux clients :
- `GET /notifications` : Récupérer toutes les notifications
- `GET /notifications/unread` : Récupérer les notifications non lues
- `GET /notifications/unread/count` : Compter les notifications non lues
- `PUT /notifications/{id}/read` : Marquer une notification comme lue
- `PUT /notifications/read-all` : Marquer toutes les notifications comme lues
- `DELETE /notifications/{id}` : Supprimer une notification

## Intégrations

### CollaboratorService
Lors de la création d'un collaborateur, le service déclenche automatiquement :
```java
notificationService.notifyCollaboratorCreated(
    collaboratorId,
    collaboratorName,
    companyId,
    createdByUserId
);
```

### DimonaService
- **Création DIMONA** : Notification automatique lors de la création
- **Changement de statut** : Notification lors de la mise à jour du statut via `updateDimonaStatus()`

## Logique de notification

### Création de collaborateur
- **Destinataires** : 
  - Tous les utilisateurs du secrétariat social (sauf le créateur)
  - Tous les contacts de l'entreprise concernée (sauf le créateur)
- **Message** : "Un nouveau collaborateur '[Nom]' a été ajouté."

### Création DIMONA
- **Destinataires** : 
  - Tous les utilisateurs du secrétariat social (sauf le créateur)
  - Tous les contacts de l'entreprise concernée (sauf le créateur)
- **Message** : "Une nouvelle déclaration DIMONA a été créée pour '[Nom]'."

### Changement de statut DIMONA
- **Destinataires** : Tous les contacts de l'entreprise concernée
- **Message** : "Le statut de la déclaration DIMONA pour '[Nom]' a été mis à jour: [Nouveau statut]."

## Sécurité

### Contrôle d'accès
- Chaque utilisateur ne peut voir que ses propres notifications
- Les administrateurs peuvent voir les notifications de tous les utilisateurs
- Authentification JWT requise pour tous les endpoints

### Séparation des données
- Les notifications respectent la séparation des espaces (Secrétariat vs Entreprise)
- Aucune fuite d'informations entre entreprises clientes

## Performance

### Optimisations
- Index sur `recipient_id` et `created_at` pour les requêtes fréquentes
- Pagination disponible pour les grandes listes
- Requêtes optimisées avec `@Query` personnalisées
- Nettoyage automatique des anciennes notifications (30 jours)

### Pagination
```java
GET /notifications/paginated?page=0&size=10
```

## Tests

### NotificationServiceTest
Tests unitaires complets couvrant :
- Création de notifications
- Récupération par utilisateur
- Marquage comme lu
- Gestion des erreurs
- Validation des données

### Couverture
- Tests unitaires pour le service principal
- Tests d'intégration recommandés pour les contrôleurs
- Tests de performance pour les requêtes avec pagination

## Configuration

### Base de données
```sql
-- La table est créée automatiquement par JPA/Hibernate
-- Index recommandés pour la production :
CREATE INDEX idx_notification_recipient_created ON TNotification(recipient_id, created_at DESC);
CREATE INDEX idx_notification_unread ON TNotification(recipient_id, is_read);
```

### Nettoyage automatique
Le service propose une méthode `cleanupOldNotifications()` qui supprime les notifications de plus de 30 jours. Cette méthode peut être appelée :
- Manuellement via l'endpoint admin `DELETE /notifications/cleanup`
- Automatiquement via un scheduler (à implémenter selon les besoins)

## Évolutions futures

### Améliorations possibles
1. **Notifications en temps réel** : WebSocket ou Server-Sent Events
2. **Templates de messages** : Système de templates pour personnaliser les messages
3. **Préférences utilisateur** : Permettre aux utilisateurs de configurer leurs notifications
4. **Notifications par email** : Envoi d'emails pour les notifications importantes
5. **Catégories de notifications** : Système de catégories plus granulaire
6. **Notifications push** : Pour les applications mobiles futures

### Intégrations futures
- Système de workflow pour les approbations
- Notifications de rappel pour les échéances
- Alertes de conformité réglementaire

## Conformité TFE

Ce système de notifications respecte intégralement les spécifications du TFE :
- ✅ Entité Notification avec tous les champs requis
- ✅ Enum NotificationType avec les 3 types spécifiés
- ✅ Service complet avec toutes les fonctionnalités
- ✅ Repository avec requêtes optimisées
- ✅ Contrôleur REST avec sécurité
- ✅ Intégration dans CollaboratorService et DimonaService
- ✅ Respect de l'architecture en couches
- ✅ Tests unitaires
- ✅ Documentation complète
