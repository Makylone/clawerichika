# 🤖 Clawerichika - Discord Bot

![Java Version](https://img.shields.io/badge/Java-21-orange)
![JDA Version](https://img.shields.io/badge/JDA-5.0.0--beta-blue)

**Clawerichika** est un bot Discord, développé en Java 21 avec l'API JDA 5.
Il intègre un système complet de modération, des tests unitaires et un pipeline de déploiement continu (CI/CD) vers un VPS Linux.

## 🚀 Fonctionnalités

### 🛡️ Modération
* **/ban** : Bannir un utilisateur (avec gestion de la suppression des messages).
* **/kick** : Expulser un membre du serveur.
* **/timeout** : Rendre muet un membre pour une durée précise (ex: `60s`, `10m`).
* **/unban** : Débannir un utilisateur via son ID.
* **/disconnect** : Expulser un membre d'un salon vocal.

### 👑 Gestion Spécifique
* **/boby** : Commande spéciale de gestion de rôle (Logique de rotation de rôle unique).

---

## 🛠️ Stack Technique

* **Langage :** Java 21 (OpenJDK)
* **Framework Discord :** JDA 5 (Java Discord API)
* **Gestion de projet :** Maven
* **Tests Unitaires :** JUnit 5 + Mockito (Couverture des commandes critiques)
* **CI/CD :** GitHub Actions (Build, Test, Release, Deploy via SSH)
* **Serveur :** VPS Linux (Ubuntu/Debian) avec Systemd

---

## ⚙️ Installation (Local)

Pour tester le bot sur votre machine :

1.  **Prérequis :**
    * Java 21 installé.
    * Un Token de Bot Discord (sur le [Developer Portal](https://discord.com/developers/applications)).

2.  **Cloner le projet :**
    ```bash
    git clone https://github.com/Makylone/clawerichika.git
    cd clawerichika
    ```

3.  **Configurer l'environnement :**
    Créez les variables d'environnement ou configurez votre IDE avec :
    * `BOT_TOKEN` : Le token de votre bot.

4.  **Lancer le bot :**
    ```bash
    # Via le wrapper Maven
    ./mvnw clean compile exec:java
    ```

---

## 🧪 Lancer les Tests

Le projet utilise **Mockito** pour simuler les interactions Discord sans se connecter réellement à l'API.

```bash
./mvnw clean test
```

Les tests couvrent les cas nominaux (succès) et les erreurs (permissions manquantes, utilisateur introuvable, etc.).

---

## 🚢 Déploiement (CI/CD)

Le déploiement est entièrement automatisé via GitHub Actions.

### Workflow
1. Push sur la branche `main` ou création d'un Tag (v1.0.0).
2. Build & Test : GitHub lance les tests unitaires.
3. Release : Si c'est un Tag, une Release GitHub est créée avec le `.jar`.
4. Deploy : Le `.jar` est envoyé sur le VPS via SCP et le service est redémarré.

### Configuration du Serveur (VPS)
Le bot tourne comme un service Systemd pour assurer sa stabilité (redémarrage automatique en cas de crash).

Fichier `/etc/systemd/system/discord-bot.service` :

```ini
[Unit]
Description=Clawerichika Discord Bot
After=network.target

[Service]
User=debian
WorkingDirectory=/home/debian/bot
ExecStart=/usr/bin/java -jar /home/debian/bot/clawerichika.jar
Restart=always
EnvironmentFile=/etc/clawerichika.env

[Install]
WantedBy=multi-user.target
```

### Variables Secrètes (GitHub Secrets)
Pour que la CI fonctionne, les secrets suivants sont configurés sur le repo :
* `HOST` : IP du VPS.
* `USERNAME` : Utilisateur SSH.
* `PASSWORD` : Mot de passe SSH (ou clé).
* `PORT` : Port SSH (22).

---

## 📂 Structure du Projet

```
src
├── main
│   ├── java/com/Makylone/clawerichika
│   │   ├── commands       # Logique des commandes (/ban, /kick...)
│   │   ├── core           # Gestionnaire d'événements (Listener)
│   │   ├── config         # Gestion de la configuration (Env vars)
│   │   └── Main.java      # Point d'entrée
│   └── resources          # Fichiers statiques (logback.xml...)
└── test
    └── java/com/Makylone/clawerichika
        └── commands       # Tests unitaires (Mockito)
```

---

Développé avec ❤️ par Makylone.
