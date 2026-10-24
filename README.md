# IT-Praktika

React-Anwendung zum Finden und Erfassen von Informatik-Praktikumsstellen.
Das Backend ist ein separates Java-21-/Spring-Boot-Projekt mit MySQL.

## Frontend starten

```bash
cd frontend
npm install
npm run dev
```

Die React-Anwendung und ihre npm-Konfiguration stehen unter `frontend/`.

## Spring Boot und MySQL starten

Voraussetzung: Docker Desktop mit Docker Compose. Alternativ werden Java 21+
und Maven benötigt, wenn das Backend lokal gestartet wird.

1. `.env.example` nach `.env` kopieren und lokale Datenbankpasswörter sowie
   einen zufälligen `JWT_SECRET` eintragen. Einen JWT-Schlüssel kannst du unter
   PowerShell erzeugen:

   ```powershell
   [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   ```

   Die `.env`-Datei enthält lokale Geheimnisse und darf nicht committet werden.
2. Im Projektverzeichnis starten:

   ```bash
   docker compose up --build
   ```

Beim ersten Start erstellt MySQL die Datenbank. Spring Boot führt danach
automatisch die Flyway-Migration aus und startet die API unter
`http://localhost:8080`.

Die Java-Anwendung, Konfiguration und Migrationen stehen unter `backend/`.
Beispielendpunkte:

- `GET /actuator/health` – API- und Datenbankstatus
- `GET /api/v1/stellen` – veröffentlichte Stellen
- `GET /api/v1/stellen?beruf=ICT%20Fachmann&standort=Bern&page=0&size=20`
- `GET /api/v1/stellen/{id}` – veröffentlichte Stelle per UUID
- `GET /api/v1/firmen` – Firmenprofile

Der Stellen-Endpunkt unterstützt Suche über `suche`, Filter über `beruf` und
`standort` sowie Seitennummer und Seitengrösse (`page`, `size`; maximal 100).
Fehlerhafte Parameter werden mit HTTP `400` zurückgegeben.

Die Flyway-Migration importiert die 84 Einträge der
[UZH-Praktikums-Firmenliste](https://www.ifi.uzh.ch/de/studies/bsc-info/firmenliste.html)
als mögliche Firmen für Initiativbewerbungen. Sie werden nicht automatisch als
Partnerfirmen markiert. Öffentlich angegebene E-Mail-Adressen, Kontaktpersonen,
Telefonnummern, Firmen-Websites und verlinkte Praktikumsstellen werden
übernommen, soweit die UZH-Liste sie veröffentlicht. Ein E-Mail-Button wird
nur bei veröffentlichter E-Mail-Adresse angezeigt; Verfügbarkeit und
Bewerbungskontakt bitte direkt beim Unternehmen prüfen. Die Übernahme ist ein
Snapshot vom 20.10.2026 und wird nicht automatisch mit der UZH-Seite
synchronisiert.

Die React-Oberfläche bietet Login und Schüler-Registrierung auf derselben Seite.
Das Backend hasht Passwörter mit BCrypt, gibt nach dem Login ein JWT aus und
prüft eingehende Tokens serverseitig. Öffentliche Registrierung kann nur
Schülerkonten erstellen; Admin-Schreibzugriffe verlangen eine serverseitig
geprüfte Admin-Rolle. Das Admin-Dashboard sowie die Stellen- und
Firmenverwaltung arbeiten mit der Datenbank.
Für das erste Admin-Konto kann der einmalige Bootstrap mit
`ADMIN_BOOTSTRAP_EMAIL` und `ADMIN_BOOTSTRAP_PASSWORD` aktiviert werden; beide
Werte nach erfolgreichem ersten Anlegen aus `.env` entfernen. Die übrigen
Admin-Funktionen können Firmen und Stellen anlegen, bearbeiten und löschen.

Weitere Endpunkte:

- `POST /api/v1/auth/register` und `POST /api/v1/auth/login` – Schülerkonto
  erstellen und anmelden
- `POST /api/v1/auth/logout` – aktuelles JWT widerrufen
- `POST /api/v1/auth/password-reset` und
  `POST /api/v1/auth/password-reset/confirm` – Passwort-Reset anfordern und
  bestätigen
- `GET`, `POST /api/v1/admin/stellen`, `PUT`/`DELETE
  /api/v1/admin/stellen/{id}` – Admin-Stellenpflege
- `GET`/`POST /api/v1/firmen`, `PUT /api/v1/firmen/{id}` und
  `DELETE /api/v1/firmen/delete/{id}` – Firmen lesen und administrieren
- `GET`, `POST /api/v1/me/merkliste`, `PUT`/`DELETE
  /api/v1/me/merkliste/{eintragId}` – persönliche Merkliste; nur für
  angemeldete Schülerkonten
- `DELETE /api/v1/users/me` – Konto nach Passwortbestätigung löschen

Der Passwort-Reset antwortet unabhängig davon, ob ein passendes Konto existiert,
generisch mit `202 Accepted`. Für den tatsächlichen Mailversand müssen die
SMTP-Werte in `.env` gesetzt sein. `.env.example` enthält die Variablennamen.
Eine neue Datenbank wird nicht mit Demo-Firmen oder -Stellen befüllt; Admins
pflegen die ersten Inhalte. Firmen- und Schulkontakte sowie Bewerbungen bleiben
beim abgestimmten E-Mail-Ablauf.

Eine verständliche Übersicht des Backend-Aufbaus, des aktuellen
Authentifizierungsstands und der nächsten Lernschritte steht im
[`Backend-Merkblatt`](backend/MERKBLATT_BACKEND.md).

### Backend-Struktur

```text
backend/
├── BACKEND_TODO.txt
├── MERKBLATT_BACKEND.md
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/ch/itpraktika/api/
        │   ├── common/
        │   ├── company/
        │   ├── config/
        │   ├── internship/
        │   ├── merkliste/
        │   └── user/
        └── resources/
            ├── application.yml
            └── db/migration/
                └── V1__...sql bis V4__...sql
```

Für den produktiven Betrieb sind HTTPS/Reverse-Proxy, Backups, Monitoring und
Datenschutz-/Aufbewahrungsregeln noch festzulegen und zu verifizieren.
Die Testcontainers-Integrationstests benötigen einen erreichbaren Docker-
Daemon; Details und offene Punkte stehen in
[`backend/BACKEND_TODO.txt`](backend/BACKEND_TODO.txt). Neue
Datenbankänderungen kommen als neue Flyway-Migration hinzu; bereits
ausgeführte Migrationen sollten nicht nachträglich verändert werden.
