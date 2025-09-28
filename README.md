# VALIDUS Backend

Backend Spring Boot 3.3 (Java 21) pour l’application VALIDUS de gestion de factures : réception, achats, double validation métier, comptabilité, trésorerie et audit.

## Table des matières
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Lancement](#lancement)
  - [Profil `dev` (H2)](#profil-dev-h2)
  - [Profil `dev-tc` (Testcontainers SQL Server)](#profil-dev-tc-testcontainers-sql-server)
  - [Profil `prod`](#profil-prod)
- [Tests](#tests)
- [Endpoints principaux](#endpoints-principaux)
- [Exemples `curl`](#exemples-curl)
- [Sécurité et rôles](#sécurité-et-rôles)

## Architecture
- Architecture hexagonale : ports/adapters, services applicatifs transactionnels, contrôleurs REST minces.
- Modules principaux :
  - **ReceptionService** (création/édition factures, stockage pièces, IA extraction).
  - **PurchasingService** (soumission, validation PO via ERP mock).
  - **WorkflowService** (double validation métier).
  - **AccountingService**, **TreasuryService**, **AuditControlService**, **DafService** (workflow paiements).
  - **AiRestAdapter** (appel microservice IA via Resilience4j).
- Observabilité : logs JSON (Logback + MDC), AuditLog persistant.

## Stack technique
- Java 21, Spring Boot 3.3, Spring Web/Data JPA/Validation/Security, OAuth2 Resource Server, Springdoc OpenAPI.
- MapStruct, Lombok, Flyway (prod), H2 (dev), SQL Server (prod/dev-tc), Testcontainers, Resilience4j.
- Ports externes : `ERPAdapter` (mock), `AttachmentStoragePort` (FS), `NotificationPort` (mock log), `AiAdapter` (REST).

## Prérequis
- JDK 21
- Maven 3.9+
- Docker (optionnel mais requis pour Testcontainers SQL Server)

## Configuration
Principales propriétés (voir `src/main/resources`):

- `application.properties` (profil `dev`) : H2 en mémoire, sécurité mock (header `X-Mock-Roles`), stockage local `./storage`.
- `application-dev-tc.yml` : active Testcontainers SQL Server.
- `application-prod.yml` : SQL Server + Flyway + configuration OIDC (remplacer `<ton_issuer_oidc>`/`<ton_client_id>`).
- Propriétés IA :
  - `ai.base-url` : URL microservice IA.
  - `ai.api-key` : clé API envoyée via header `X-API-KEY`.
  - Resilience4j : circuit breaker + retry pour l’IA.

## Lancement
### Profil `dev` (H2)
```bash
mvn spring-boot:run
```
Swagger UI : http://localhost:8080/swagger-ui/index.html

### Profil `dev-tc` (Testcontainers SQL Server)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev-tc
```
Testcontainers démarre automatiquement SQL Server (Docker requis).

### Profil `prod`
Configurer les secrets (SQL Server, OIDC, AI) puis packager :
```bash
mvn clean package -Pprod
java -jar target/validus-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Tests
```bash
mvn clean verify
```
Lancement des tests unitaires et intégration (profil par défaut `dev`).

## Endpoints principaux
- `POST /api/invoices` : créer une facture (roles `RECEPTION`, `ADMIN`).
- `PUT /api/invoices/{id}` : modifier facture (statuts DRAFT/RECEIVED).
- `POST /api/invoices/{id}/attachments` (multipart).
- `POST /api/invoices/{id}/submit` : soumettre à la double validation (Achats).
- `POST /api/invoices/{id}/accounting` (Compta).
- `POST /api/invoices/{id}/ready-for-payment` (Compta/Tresorerie).
- `POST /api/invoices/{id}/cancel` (Admin).
- `GET /api/invoices` : recherche paginée.
- `GET /api/invoices/{id}/audit` : historique.
- `POST /api/workflow/{invoiceId}/assign|approve|reject` : pilotage validations.
- `POST /api/ai/invoices/{id}/extract|anomaly`, `GET /near-duplicate|summary` : proxy IA.
- `POST /api/payments/batches` : créer lot paiements (Tréso) + `/submit`, `/approve-audit`, `/reject`, `/approve-daf`, `/execute`.
- Actuator health : `/actuator/health`.

## Exemples `curl`
1. **Création facture + workflow complet**
```bash
# 1. Création
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -H "X-Mock-Roles: RECEPTION" \
  -d '{
    "number": "INV-2024-0001",
    "supplierId": "SUP-001",
    "companyCode": "COMP1",
    "invoiceDate": "2024-01-10",
    "dueDate": "2024-02-10",
    "currency": "EUR",
    "amountHT": 1000,
    "amountTVA": 200,
    "amountTTC": 1200,
    "hasPaperOriginal": true,
    "lines": [{"designation":"Prestations","quantity":1,"unitPrice":1000,"taxRate":20,"amountLine":1200}]
  }'

# 2. Upload PDF
curl -X POST http://localhost:8080/api/invoices/{invoiceId}/attachments \
  -H "X-Mock-Roles: RECEPTION" \
  -F type=INVOICE_PDF \
  -F file=@invoice.pdf

# 3. Soumission Achats
curl -X POST http://localhost:8080/api/invoices/{invoiceId}/submit \
  -H "X-Mock-Roles: ACHATS"

# 4. Affectation valideurs
curl -X POST http://localhost:8080/api/workflow/{invoiceId}/assign \
  -H "Content-Type: application/json" -H "X-Mock-Roles: ACHATS" \
  -d '{"buyerApprover":"userA","businessApprover":"userB"}'

# 5. Approvals
curl -X POST http://localhost:8080/api/workflow/{invoiceId}/approve \
  -H "Content-Type: application/json" -H "X-Mock-Roles: METIER" \
  -d '{"comment":"OK"}'

curl -X POST http://localhost:8080/api/workflow/{invoiceId}/approve \
  -H "Content-Type: application/json" -H "X-Mock-Roles: METIER" \
  -d '{"comment":"OK niveau 2"}'

# 6. Comptabilité
curl -X POST http://localhost:8080/api/invoices/{invoiceId}/accounting \
  -H "X-Mock-Roles: COMPTA"

curl -X POST http://localhost:8080/api/invoices/{invoiceId}/ready-for-payment \
  -H "X-Mock-Roles: COMPTA"

# 7. Création lot paiement
curl -X POST http://localhost:8080/api/payments/batches \
  -H "Content-Type: application/json" -H "X-Mock-Roles: TRESO" \
  -d '{"companyCode":"COMP1","invoiceIds":["{invoiceId}"]}'

curl -X POST http://localhost:8080/api/payments/batches/{batchId}/submit -H "X-Mock-Roles: TRESO"
curl -X POST http://localhost:8080/api/payments/batches/{batchId}/approve-audit -H "X-Mock-Roles: AUDIT"
curl -X POST http://localhost:8080/api/payments/batches/{batchId}/approve-daf -H "X-Mock-Roles: DAF"
curl -X POST http://localhost:8080/api/payments/batches/{batchId}/execute -H "X-Mock-Roles: TRESO"
```

2. **Rejet lot par Audit**
```bash
curl -X POST http://localhost:8080/api/payments/batches/{batchId}/reject \
  -H "Content-Type: application/json" \
  -H "X-Mock-Roles: AUDIT" \
  -d '{"reason":"Justificatif manquant"}'
```

## Sécurité et rôles
- Profil `dev` : authentification mock (header `X-Mock-Roles`).
- Profil `prod` : OAuth2 Resource Server (JWT Azure AD). Les scopes/roles sont traduits en autorités `ROLE_<ROLE>`.
- Rôles disponibles : `RECEPTION`, `ACHATS`, `METIER`, `COMPTA`, `TRESO`, `AUDIT`, `DAF`, `ADMIN`.

## Contributeurs
- Généré automatiquement par l’agent IA (ChatGPT) selon les spécifications VALIDUS.
