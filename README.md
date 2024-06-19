# SitemapDiff

SitemapDiff ist eine fortschrittliche Webanwendung, die im Auftrag der Hochzeitsportal24 GmbH entwickelt wurde. Ziel der Anwendung ist es, dem Team von Hochzeitsportal24 ein effizientes und benutzerfreundliches Tool zur Verfügung zu stellen, um Änderungen auf verschiedenen Webseiten zu überwachen und zu verwalten. Raffael Schulz, der Inhaber von Hochzeitsportal24, hat die Anforderungen und Ideen für die App formuliert, um sie optimal in den Arbeitsalltag seines Teams zu integrieren.

## Features

- **Google Login**: Einfache und sichere Authentifizierung mittels Google-Login.
- **Webseiten-Tracking**: Nutzer können URLs von Webseiten hinzufügen, die überwacht werden sollen.
- **Automatische Sitemap-Erkennung**: Basierend auf den hinzugefügten URLs werden automatisch die entsprechenden `sitemap.xml`-Dateien erkannt und verarbeitet.
- **Datenbank-Speicherung**: Inhalte der Sitemaps werden bei jedem Crawl in einer MongoDB-Datenbank gespeichert.
- **Manuelle und automatische Crawls**: Crawls können manuell gestartet oder per Cronjob automatisch durchgeführt werden.
- **Änderungserkennung**: Unterschiede zwischen zwei Crawls, wie hinzugefügte oder entfernte URLs, werden übersichtlich angezeigt.
- **Interaktive URLs**: Geänderte URLs können per Klick besucht, ins Clipboard kopiert oder (in zukünftigen Versionen) per Email verschickt werden.
- **Geplante Erweiterung - Dashboard Thumbnails**: Am Ende des Projekts soll die Funktion hinzugefügt werden, automatisch Thumbnails für jede getrackte Webseite zu erstellen und im Dashboard anzuzeigen.

## Verwendete Technologien

- **Backend**:
  - **Spring Boot**: Für ein robustes und skalierbares Backend.
  - **Spring Security**: Zur Implementierung des Google-Logins und zur Sicherstellung einer sicheren Authentifizierung.
  - **Spring Session**: Für die Verwaltung der Benutzersitzungen.
  - **MongoDB**: Als NoSQL-Datenbank zur Speicherung der Sitemap-Daten und Benutzerinformationen.

- **Frontend**:
  - **React**: Für eine dynamische und reaktionsschnelle Benutzeroberfläche.
  - **TypeScript**: Zur Verbesserung der Code-Qualität durch statische Typisierung.
  - **Material-UI**: Für ein modernes und ansprechendes Design der Benutzeroberfläche.

- **Weitere Tools**:
  - **Axios**: Für die Kommunikation zwischen Frontend und Backend.
  - **Docker**: Zur Containerisierung der Anwendung für eine einfache Bereitstellung und Skalierung.
  - **GitHub Actions**: Für Continuous Integration und Continuous Deployment (CI/CD).

## Praktischer Nutzen

SitemapDiff bietet dem Team von Hochzeitsportal24 eine effiziente Möglichkeit, Änderungen auf ihren wichtigsten Webseiten im Blick zu behalten. Durch die automatisierte Erkennung und Speicherung von Sitemap-Daten sowie die visuelle Darstellung von Änderungen können Mitarbeiter schnell auf neue oder entfernte Inhalte reagieren. Dies spart Zeit und erhöht die Effizienz im Arbeitsalltag erheblich.

Die geplante Erweiterung um Dashboard-Thumbnails wird dem Team zudem einen schnellen visuellen Überblick über die überwachten Webseiten bieten, was die Nutzung der Anwendung noch intuitiver und benutzerfreundlicher macht.

## Setup und Installation

### Voraussetzungen

- **Java** und **Maven** für das Backend.
- **Node.js** und **npm** für das Frontend.
- **MongoDB**-Instanz für die Datenbank.
- **Google OAuth Credentials** für die Authentifizierung.

### Schritt-für-Schritt-Anleitung

1. **Repository klonen**:
    ```bash
    git clone https://github.com/IhrBenutzername/SitemapDiff.git
    cd SitemapDiff
    ```

2. **Umgebungsvariablen konfigurieren**:
   Erstellen Sie eine `.env`-Datei im Stammverzeichnis des Projekts und fügen Sie die folgenden Variablen hinzu:
    ```plaintext
    APP_URL=http://localhost:3000
    GOOGLE_ID=your-google-client-id
    GOOGLE_SECRET=your-google-client-secret
    ```

3. **Google OAuth-Anmeldedaten einrichten**:
   - Gehen Sie zur [Google Cloud Console](https://console.cloud.google.com/).
   - Erstellen Sie ein neues Projekt oder wählen Sie ein bestehendes Projekt aus.
   - Navigieren Sie zu **APIs & Services** > **Credentials**.
   - Klicken Sie auf **Create Credentials** und wählen Sie **OAuth 2.0 Client IDs**.
   - Konfigurieren Sie den OAuth-Zustimmungsbildschirm und erstellen Sie die Anmeldedaten.
   - Kopieren Sie die Client-ID und das Client-Geheimnis und fügen Sie sie in die `.env`-Datei ein.

4. **Backend starten**:
    - **Befehl**:
        ```bash
        cd backend
        mvn spring-boot:run
        ```

5. **Frontend starten**:
    - **Befehl**:
        ```bash
        cd frontend
        npm install
        npm start
        ```

6. **MongoDB einrichten**:
    - Stellen Sie sicher, dass eine MongoDB-Instanz läuft und konfigurieren Sie die Verbindungseinstellungen in `application.properties`.

7. **Docker verwenden (optional)**:
    - Um die Anwendung mit Docker auszuführen:
        ```bash
        docker-compose up --build
        ```

## Konfiguration

### `application.properties`

```properties
app.url=${APP_URL}
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_SECRET}
spring.data.mongodb.uri=mongodb://localhost:27017/yourdatabase
spring.session.store-type=mongodb
spring.session.mongodb.collection-name=sessions
server.servlet.session.timeout=30d
