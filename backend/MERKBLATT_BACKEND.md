# Backend-Merkblatt: Codeausschnitte

Kurze Ausschnitte aus dem Projekt und Erklärungen, wie sie zusammenhängen.

## 1. Firma erstellen: Controller -> Service -> Repository

Der Controller nimmt JSON entgegen. `@Valid` prüft das DTO, danach delegiert
der Controller an den Service:

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<CompanyResponse> createCompany(
        @Valid @RequestBody CompanyDto request
) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(companyService.createCompany(request));
}
```

Der Service erzeugt die UUID, baut die Datenbank-Entity und speichert sie.
Das Repository übernimmt den Datenbankzugriff; nach dem Speichern wird die
Entity in ein Antwort-DTO umgewandelt:

```java
Company company = new Company(
        UUID.randomUUID().toString(),
        request.name(),
        request.location(),
        request.specialties(),
        request.email(),
        request.phone(),
        request.description(),
        request.website(),
        request.partner(),
        request.logoUrl()
);
return CompanyResponse.from(companyRepository.save(company));
```

Kurz: Der Controller kümmert sich um HTTP, der Service um den Ablauf und das
Repository um Speichern und Laden. Die Entity bleibt im Backend; nach aussen
geht `CompanyResponse`.

## 2. Admin-Rolle prüfen

An schreibenden Controller-Methoden steht:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Die Prüfung braucht `@EnableMethodSecurity` in der Security-Konfiguration.
`hasRole('ADMIN')` erwartet in Spring die Authority `ROLE_ADMIN`.

Die Rolle aus der Datenbank wird deshalb beim Laden des Benutzers übersetzt:

```java
String authorityRole = switch (user.getRole()) {
    case "admin" -> "ADMIN";
    case "student" -> "STUDENT";
    default -> throw new IllegalStateException("Unbekannte Benutzerrolle.");
};

return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getPasswordHash())
        .roles(authorityRole)
        .build();
```

Die Datenbank speichert `admin`; `.roles("ADMIN")` macht daraus Spring-seitig
`ROLE_ADMIN`. Beim JWT wird `ADMIN` wieder in `ROLE_ADMIN` umgewandelt:

```java
JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
authorities.setAuthoritiesClaimName("roles");
authorities.setAuthorityPrefix("ROLE_");
```

Damit kann `@PreAuthorize("hasRole('ADMIN')")` auch bei einer Anfrage mit
Bearer-Token die Rolle prüfen.

## 3. Merkliste: GET, POST, PUT, DELETE und Berechtigungen

Die Merkliste gehört zum angemeldeten Schülerkonto. Die Benutzer-ID wird
nicht vom Frontend gesendet: Der Controller nimmt die E-Mail aus dem
authentifizierten JWT (`Authentication`) und übergibt sie an den Service.
Am Controller schützt `@PreAuthorize("hasRole('STUDENT')")` alle Endpunkte.
Die Service-Abfragen begrenzen jeden Eintrag zusätzlich auf die ID des
angemeldeten Benutzers. So kann ein Schüler keine Einträge anderer Konten
lesen, ändern oder löschen.

| Methode | Route | Zweck | Erfolg |
| --- | --- | --- | --- |
| `GET` | `/api/v1/me/merkliste` | Eigene Einträge abrufen | `200 OK` mit Liste |
| `GET` | `/api/v1/me/merkliste/{eintragId}` | Einen eigenen Eintrag abrufen | `200 OK` mit Eintrag |
| `POST` | `/api/v1/me/merkliste` | Eine veröffentlichte Stelle merken | `201 Created` mit Eintrag |
| `PUT` | `/api/v1/me/merkliste/{eintragId}` | Stelle eines eigenen Eintrags ersetzen | `200 OK` mit aktualisiertem Eintrag |
| `DELETE` | `/api/v1/me/merkliste/{eintragId}` | Einen eigenen Eintrag entfernen | `204 No Content` |

POST und PUT erwarten als JSON:

```json
{
  "stelleId": "UUID-der-veröffentlichten-Stelle"
}
```

Die Response enthält die Eintrags-ID, Stellen-ID, Titel, Firma, Standort
und den Zeitpunkt, zu dem die Stelle gemerkt wurde. Titel und weitere
Stellendaten werden aus der Stelle gelesen; sie werden nicht nochmals in der
Merkliste gespeichert.

Der Ablauf folgt dem Muster Controller -> Service -> Repository:

1. `MerklisteController` nimmt Route, Request und authentifizierte Anmeldung
   entgegen. Hier stehen die HTTP-Methoden und die Rollenprüfung.
2. `MerklisteService` sucht das Benutzerkonto, prüft die veröffentlichte
   Stelle und setzt die Geschäftsregeln um. Doppelte Einträge werden mit
   `409 Conflict` abgewiesen; nicht gefundene Konten, Stellen oder eigene
   Einträge führen zu `404 Not Found`.
3. `MerklisteRepository` liest und speichert `MerklisteEintrag` in MySQL.
   Der Datenbank-Unique-Constraint verhindert doppelte Kombinationen aus
   Benutzer und Stelle auch auf Datenbankebene.
4. `MerklisteResponse` gibt die Antwortdaten zurück, ohne Entity oder
   Passwortdaten offenzulegen.

Zum Nachverfolgen bei Fehlern:

- Falscher oder fehlender Token: zuerst Login/JWT und Security-Filterkette
  prüfen; geschützte Routen liefern `401 Unauthorized`.
- Gültiger Token, aber keine Schülerrolle: `@PreAuthorize` prüfen;
  erwartet wird `ROLE_STUDENT`, ansonsten `403 Forbidden`.
- Eintrag eines anderen Benutzers: `findByIdAndUser_Id` und die vom Token
  abgeleitete Benutzer-ID prüfen; der Eintrag ist für dieses Konto nicht
  gefunden und liefert `404 Not Found`.
- `POST` oder `PUT` liefert `400 Bad Request`: JSON-Format und UUID in
  `stelleId` prüfen.
- `POST` oder `PUT` liefert `404 Not Found`: kontrollieren, ob die Stelle
  existiert und den Status `PUBLISHED` hat.
- `POST` oder `PUT` liefert `409 Conflict`: dieselbe Stelle ist für das
  Konto bereits gemerkt.
- Route oder Statuscode falsch: zuerst Controller, danach Service und
  Repository prüfen.

Die Endpunkt- und Rollenprüfungen sind in
`src/test/java/ch/itpraktika/api/merkliste/MerklisteControllerTest.java`
abgedeckt. Backend-Tests laufen mit `mvn -f backend/pom.xml test`.

## 4. Passwort prüfen: Login -> Spring Security -> BCrypt

Der Login-Controller übergibt E-Mail und Passwort als noch nicht geprüfte
Zugangsdaten an Spring Security:

```java
Authentication authentication = authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(
                request.email(),
                request.password()
        )
);
```

Spring lädt den Benutzer über `DatabaseUserDetailsService` und vergleicht
das eingegebene Passwort mit dessen gespeicherten Hash. Der Encoder wird als
Spring-Bean bereitgestellt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Ein Passwort wird beim sicheren Anlegen eines Benutzers gehasht; beim Login
wird nicht Klartext mit Hash verglichen. BCrypt übernimmt den Vergleich intern.
Der Login-Controller erstellt nur dann ein JWT, wenn `authenticate(...)`
erfolgreich zurückkehrt.

## 5. JWT ausstellen

Nach erfolgreicher Anmeldung erstellt der JWT-Service Claims und signiert sie:

```java
JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(JwtTokenService.ISSUER)
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plusSeconds(TOKEN_LIFETIME_SECONDS))
        .subject(authentication.getName())
        .claim("roles", roles)
        .build();

JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
return jwtEncoder
        .encode(JwtEncoderParameters.from(header, claims))
        .getTokenValue();
```

`sub` ist hier die E-Mail, `roles` enthält zum Beispiel `ADMIN`, und `exp`
begrenzt die Lebensdauer auf 15 Minuten. HS256 signiert den Token mit dem
geheimen Schlüssel. JWT-Inhalte sind lesbar, also niemals Passwörter oder
andere Geheimnisse als Claims eintragen.

Der Login gibt den Token mit Typ, Lebensdauer, E-Mail und Rolle zurück:

```java
return ResponseEntity.ok(new LoginResponse(
        accessToken,
        "Bearer",
        TOKEN_LIFETIME_SECONDS,
        authentication.getName(),
        role
));
```

Der Schlüssel wird über `JWT_SECRET` konfiguriert, nicht in Java fest
eingetragen. `JwtDecoder` prüft Signatur, Ablaufzeit und Aussteller des
eingehenden Bearer-Tokens. Erst danach kann die Rollenprüfung auf dem
Controller sicher verwendet werden.

```java
NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withSecretKey(jwtSecretKey(base64Secret))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtTokenService.ISSUER));
```

Der JWT-Converter liest zusätzlich den `roles`-Claim und macht aus `ADMIN`
eine Spring-Authority `ROLE_ADMIN`. In der Security-Filterkette sind Login und
öffentliche GET-Routen freigegeben; alle anderen Anfragen brauchen einen
gültigen Token.

## 6. Schülerkonto registrieren

Die öffentliche Registrierung erhält nur E-Mail und Passwort. Die Rolle wird
nicht aus der Anfrage übernommen, sondern im Service fest auf `student` gesetzt:

```java
User student = new User(
        UUID.randomUUID().toString(),
        normalizedEmail,
        passwordEncoder.encode(request.password()),
        "student"
);
userRepository.saveAndFlush(student);
```

Die E-Mail wird normalisiert, doppelte Adressen werden abgewiesen und das
Passwort muss mindestens 12 Zeichen haben. Wegen BCrypt darf es höchstens
72 UTF-8-Bytes enthalten. Der öffentliche Endpunkt kann deshalb keine
Admin-Konten erzeugen.

Das React-Frontend bietet Registrierung und Login auf derselben Seite an. Nach
erfolgreicher Registrierung wechselt es zurück zum Login; die Person meldet
sich danach mit dem neuen Konto an.

## 7. Erstes Admin-Konto anlegen

Der einmalige Bootstrap wird über zwei Umgebungsvariablen aktiviert:

```text
ADMIN_BOOTSTRAP_EMAIL=admin@example.com
ADMIN_BOOTSTRAP_PASSWORD=<eigenes sicheres Passwort>
```

Beim Start prüft `AdminBootstrap`, ob beide Angaben vorhanden und gültig sind.
Existiert die E-Mail noch nicht, wird ein Nutzer mit Rolle `admin` und
BCrypt-Hash angelegt. Ein vorhandener Admin wird nicht überschrieben. Ist
bereits ein Nicht-Admin mit dieser E-Mail vorhanden, bricht der Start mit einer
Fehlermeldung ab. Beide Bootstrap-Werte nach dem ersten erfolgreichen Start aus
`.env` entfernen; sie sind nicht für eine öffentliche Registrierung gedacht.

Der zentrale Teil beim Anlegen ist:

```java
User admin = new User(
        UUID.randomUUID().toString(),
        normalizedEmail,
        passwordEncoder.encode(password),
        "admin"
);
userRepository.save(admin);
```

Das Passwort wird vor dem Speichern mit BCrypt gehasht. Die Bootstrap-Klasse
gibt weder Passwort noch Hash in Logs aus.

Danach kann man sich bei `POST /api/v1/auth/login` anmelden. Die API gibt ein
JWT zurück, das der Client bei geschützten Anfragen als
`Authorization: Bearer <token>` mitsendet. Die Bootstrap-Variablen sind nicht
für die öffentliche Schüler-Registrierung gedacht.

## 8. Admin-Stellen und Firmen verwalten

Die Admin-Seiten verwenden die persistente API statt lokaler Beispiel- oder
localStorage-Daten. Für Stellen liegen HTTP-Controller, DTOs und
Geschäftsregeln getrennt:

| Methode | Route | Zweck | Erfolg |
| --- | --- | --- | --- |
| `GET` | `/api/v1/admin/stellen` | Alle Stellen inklusive Entwürfen abrufen | `200 OK` |
| `POST` | `/api/v1/admin/stellen` | Stelle anlegen | `201 Created` |
| `PUT` | `/api/v1/admin/stellen/{id}` | Stelle aktualisieren/veröffentlichen/archivieren | `200 OK` |
| `DELETE` | `/api/v1/admin/stellen/{id}` | Stelle entfernen | `204 No Content` |
| `GET` | `/api/v1/firmen` | Firmen abrufen | `200 OK` |
| `POST` | `/api/v1/firmen` | Firma anlegen | `201 Created` |
| `PUT` | `/api/v1/firmen/{id}` | Firmenprofil aktualisieren | `200 OK` |
| `DELETE` | `/api/v1/firmen/delete/{id}` | Firma entfernen | `204 No Content` |

Alle schreibenden Firmen- und Stellenrouten sowie die Admin-Stellenliste
erfordern `ROLE_ADMIN`. Die öffentliche Stellenliste gibt nur den Status
`PUBLISHED` aus. Der Admin setzt Statuswerte wie `DRAFT`, `PUBLISHED` oder
`ARCHIVED`; Responses liefern sie in Kleinbuchstaben an das Frontend.

Stellenanfragen speichern unter anderem Firma, Titel, Beruf, Standort,
Startoption, optionale Ansprechperson, Bewerbungslink, Beschreibung,
Anforderungen und Status. Ein Bewerbungslink sowie Website- und Logo-URL einer
Firma akzeptieren nur HTTP- oder HTTPS-Adressen. Logo-Dateien werden nicht
hochgeladen; die Oberfläche speichert eine URL.

Die Migrationen importieren 84 Firmen aus der öffentlichen
[UZH-Praktikums-Firmenliste](https://www.ifi.uzh.ch/de/studies/bsc-info/firmenliste.html)
als `initiativeListed = true`, aber `partner = false`. Kontaktperson,
Kontakt-E-Mail, Telefonnummer, Website und Praktikumslink werden übernommen,
sofern die Quelle sie angibt. Die Kontaktdaten sind nicht automatisch als
Bewerbungsadressen bestätigt; Interessierte sollen den Kontakt vor dem Versand
prüfen. Der Datenstand ist ein Snapshot vom 20.10.2026, kein automatischer
Import bei späteren Änderungen der UZH-Seite.

Der Service setzt die Geschäftsregeln um; Controller behandeln HTTP und
Repositorys kapseln den Datenbankzugriff. Neue Datenbankfelder brauchen eine
neue Flyway-Migration. Bereits ausgeführte Migrationen dürfen nicht editiert
werden.

## 9. Registrierung, Passwort-Reset und Kontolöschung

Die Authentifizierungsrouten sind:

| Methode | Route | Zweck |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Schülerkonto registrieren |
| `POST` | `/api/v1/auth/login` | Anmelden und 15-Minuten-JWT erhalten |
| `POST` | `/api/v1/auth/logout` | JWT widerrufen |
| `POST` | `/api/v1/auth/password-reset` | Reset-Mail anfordern; generische `202`-Antwort |
| `POST` | `/api/v1/auth/password-reset/confirm` | Einmal-Token und neues Passwort bestätigen |
| `DELETE` | `/api/v1/users/me` | Eigenes Konto nach Passwortbestätigung löschen |

Das Reset-Token wird nur gehasht gespeichert, läuft nach 30 Minuten ab und ist
einmalig. Die Reset-Anfrage antwortet auch bei unbekannter E-Mail generisch, um
keine Konten zu verraten. Für echte Zustellung muss SMTP in der Laufzeitumgebung
konfiguriert werden (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`,
`SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS` und `MAIL_FROM`). Ein fehlender
oder falsch konfigurierter SMTP-Server ersetzt keine Release-Prüfung des
Versands.

Rate-Limits sind derzeit pro IP bzw. IP und normalisierter E-Mail konfiguriert:
Login 10 Versuche pro 15 Minuten, Registrierung 5 pro Stunde, Reset-Anfrage 3
pro Stunde und Reset-Bestätigung 10 pro Stunde. Hinter einem Reverse-Proxy
muss die Client-IP vertrauenswürdig bestimmt werden. Eine vom Client
kontrollierbare `X-Forwarded-For`-Angabe darf nicht blind als IP-Quelle dienen.

Bei der Kontolöschung wird das Passwort geprüft, das Konto entfernt und die
Merkliste über Datenbank-Fremdschlüssel kaskadierend gelöscht. Das letzte
Admin-Konto kann nicht gelöscht werden. Datenaufbewahrungsfristen sind noch
eine offene Produkt-/Datenschutzentscheidung.

## 10. Tests und Release-Grenzen

`mvn -f backend/pom.xml test` führt Controller-, Validierungs-, Service- und
Security-Tests aus. Zuletzt wurden 71 Tests ausgeführt: 68 erfolgreich und
3 übersprungen. Die MySQL-/Flyway-Integrationstests benötigen einen
erreichbaren Docker-Daemon, der in der Entwicklungsumgebung nicht verfügbar
war. Die Migrationen müssen daher vor Release noch gegen eine echte MySQL-
Testumgebung geprüft werden.

Firmen- und Schulkontakt sowie Bewerbungen bleiben beim abgestimmten
E-Mail-Ablauf; sie werden nicht im Backend gespeichert. Der KI-Bewerbungshelfer
ist auf später verschoben. HTTPS, Produktions-Backups, Monitoring,
SMTP-Zustellung, Datenschutzinformationen und Aufbewahrungsfristen sind
Release-Voraussetzungen, die in
[`BACKEND_TODO.txt`](BACKEND_TODO.txt) nachgehalten werden.
