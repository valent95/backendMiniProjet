# Guide de Déploiement sur Koyeb

Ce guide explique comment déployer l'application Pharmacie Spring Boot sur Koyeb avec une base de données PostgreSQL.

## Prérequis

- Un compte [Koyeb](https://www.koyeb.com)
- Un compte GitHub avec votre repository poussé
- Une base de données PostgreSQL (Koyeb en propose une)
- Un compte Gmail ou un serveur SMTP pour l'envoi d'emails

## Étapes de déploiement

### 1. Créer une base de données PostgreSQL sur Koyeb

1. Allez sur le dashboard Koyeb
2. Cliquez sur **Databases** dans le menu de gauche
3. Cliquez sur **Create Database**
4. Configurez :
   - **Engine** : PostgreSQL (version 14+)
   - **Name** : `pharmacie` (ou autre nom)
   - **Region** : Sélectionnez la région la plus proche
5. Cliquez sur **Create**
6. Attendez que la base soit créée, puis notez les paramètres de connexion :
   - Host
   - Port
   - User
   - Password
   - Database name

### 2. Initialiser la base de données

Avant de déployer l'application, vous devez créer le schéma initial. Deux options :

**Option A : Avec le profil 'create'**
Vous pouvez d'abord déployer l'application avec le profil `create` (qui crée le schéma et charge les données) :
```
SPRING_PROFILES_ACTIVE=create
```

**Option B : Créer le schéma manuellement**
Utilisez un client PostgreSQL (pgAdmin4, DBeaver, etc.) pour créer le schéma initial :
```sql
-- Exécutez les requêtes SQL du fichier src/main/resources/schema.sql
```

### 3. Créer une application Web Service sur Koyeb

1. Allez sur le dashboard Koyeb
2. Cliquez sur **Web Services** puis **Create a new Web Service**
3. Sélectionnez **GitHub** comme source
4. Autorisez Koyeb à accéder à votre GitHub
5. Sélectionnez le repository contenant ce projet
6. Configurez :
   - **Name** : `pharmacie-api` (ou autre nom)
   - **Build Command** : `mvn clean package -DskipTests`
   - **Run Command** : `java -jar target/*.jar`
   - **Port** : `8080`
   - **Region** : Sélectionnez la région la plus proche

### 4. Configurer les variables d'environnement

Dans les paramètres du Web Service, allez dans **Environment** et ajoutez les variables suivantes :

#### Variables database PostgreSQL
```
JDBC_URI=jdbc:postgresql://<HOST>:<PORT>/<DATABASE>?user=<USER>&password=<PASSWORD>
```

Remplacez :
- `<HOST>` : Host de votre base Koyeb
- `<PORT>` : Port (par défaut 5432)
- `<DATABASE>` : Nom de la base (ex: `pharmacie`)
- `<USER>` : Utilisateur Koyeb
- `<PASSWORD>` : Mot de passe Koyeb

Exemple :
```
JDBC_URI=jdbc:postgresql://ep-royal-mouse-27957554.eu-central-1.pg.koyeb.app:5432/pharmacie?user=koyeb&password=monmotdepasse123
```

#### Variables Spring Mail (pour l'envoi d'emails)

**Avec Gmail :**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-app
MAIL_FROM=votre-email@gmail.com
```

> **Note Gmail** : Utilisez un **mot de passe d'application** et non votre mot de passe Gmail normal. Voir [Créer un mot de passe d'application Gmail](https://support.google.com/accounts/answer/185833).

> **Astuce email** : Vous pouvez utiliser `votre-email+alias@gmail.com` pour différents alias (ex: `john+pharmacie@gmail.com`, `john+fournisseur1@gmail.com`) - tous les emails arrivent dans `john@gmail.com`.

**Avec un autre serveur SMTP :**
```
MAIL_HOST=smtp.votreserveur.com
MAIL_PORT=587
MAIL_USERNAME=votre-username
MAIL_PASSWORD=votre-password
MAIL_FROM=noreply@votredomaine.com
```

#### Autres variables
```
SPRING_PROFILES_ACTIVE=deploy
```

### 5. Déployer

1. Après configuration des variables d'environnement
2. Cliquez sur **Create Web Service**
3. Koyeb va :
   - Récupérer le code du repository
   - Exécuter `mvn clean package -DskipTests`
   - Créer une image Docker
   - Déployer l'application

Le déploiement peut prendre 5-10 minutes. Les logs sont visibles dans le dashboard.

### 6. Tester l'application

Une fois déployée, votre application est disponible à :
```
https://votre-app-name-<random>.koyeb.app
```

#### Accéder à l'API
```
GET https://votre-app-name-<random>.koyeb.app/api
```

#### Accéder à Swagger UI
```
https://votre-app-name-<random>.koyeb.app/swagger-ui/index.html
```

#### Déclencher l'approvisionnement
```bash
curl -X POST https://votre-app-name-<random>.koyeb.app/api/approvisionnement/declencher
```

## Configuration du schéma initial

Lors du premier déploiement avec le profil `create`, les tables suivantes seront créées :

- `CATEGORIE` - Catégories de médicaments
- `MEDICAMENT` - Médicaments avec niveaux de stock
- `SUPPLIER` - Fournisseurs
- `SUPPLIER_CATEGORIE` - Relation many-to-many
- `DISPENSAIRE` - Points de distribution
- `COMMANDE` - Commandes
- `LIGNE` - Lignes de commande

Et les données d'initialisation seront chargées depuis `data.sql`.

## Considérations de sécurité

1. **Mots de passe** : Ne les mettez jamais dans le code source
2. **Variables d'environnement** : Utilisez uniquement des variables d'environnement
3. **HTTPS** : Koyeb fournit HTTPS par défaut
4. **Database** : Assurez-vous que seule votre application peut accéder à la DB
5. **Email** : Utilisez des mots de passe d'application, pas le mot de passe principal

## Dépannage

### Les emails ne s'envoient pas
- Vérifiez les variables `MAIL_USERNAME` et `MAIL_PASSWORD`
- Vérifiez les logs : `Spring Mail configuration failed`
- Testez la connexion SMTP depuis votre machine locale

### La base de données ne se connecte pas
- Vérifiez le format de `JDBC_URI`
- Vérifiez que la base est en cours d'exécution
- Vérifiez l'utilisateur et le mot de passe
- Vérifiez que le firewall autorise les connexions

### L'application démarre mais les pages retournent 404
- Vérifiez que `SPRING_PROFILES_ACTIVE=deploy` est défini
- Vérifiez les logs de déploiement

## Scaling et performance

- Koyeb peut auto-scaler l'application en fonction de la charge CPU/mémoire
- Configurez les autoscaling rules dans les paramètres du service
- La base PostgreSQL peut aussi être scalée indépendamment

## Mise à jour de l'application

Pour mettre à jour l'application :
1. Poussez les modifications vers GitHub
2. Le webhook Koyeb redéploiera automatiquement
