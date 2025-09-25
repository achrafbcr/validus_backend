# VALIDUS Backend — SQL Server

Projet Spring Boot **préconfiguré pour SQL Server** (sans H2, sans `data.sql`).

## Prérequis
- SQL Server en local (port 1433), base `validus` existante (ou sera créée si `ddl-auto=update` suffit)
- Java 17, Maven 3.9+

## Lancer dans IntelliJ
1. Ouvrir le dossier `validus-backend` (projet Maven)
2. Run `com.validus.ValidusApplication`
3. Swagger UI: http://localhost:8080/swagger-ui.html

## Authentification (HTTP Basic - démo)
- admin / admin123
- user / user123

## Endpoints
- GET  /api/invoices
- POST /api/invoices
- GET  /api/invoices/{id}
- PATCH /api/invoices/{id}/status?next=ACCOUNTED
- DELETE /api/invoices/{id}

## Notes
- `application.properties` pointe sur: `jdbc:sqlserver://localhost:1433;databaseName=validus;encrypt=true;trustServerCertificate=true`  
  Modifie `username/password` si nécessaire.
- `spring.jpa.hibernate.ddl-auto=update` crée/maj le schéma au démarrage.
- Pas de `data.sql` inclus pour éviter les soucis d’ordre d’initialisation sur SQL Server.
