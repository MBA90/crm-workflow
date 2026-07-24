# CRM WORKFLOW Service

A Spring Boot microservice for managing CRM WORKFLOW, backed by a PostgreSQL database with schema
managed by Liquibase.

> 📄 **New here?** For a business & solution overview of what this service does and why, see
> [**docs/BUSINESS_MODEL.md**](docs/BUSINESS_MODEL.md).

## Tech Stack

- Java 21
- Spring Boot 3.5.15 (Web, Data JPA, Validation)
- PostgreSQL (`postgresql` JDBC driver)
- Liquibase (schema migrations)
- MapStruct 1.5.5 (entity/DTO mapping)
- Lombok
- Maven

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included Maven Wrapper)
- Docker & Docker Compose (for the PostgreSQL database)

### Configuration

Connection and server settings live in `src/main/resources/application.yaml`.
Defaults:

| Setting        | Default                                |
|----------------|----------------------------------------|
| Server port    | `8401`                                 |
| Datasource URL | `jdbc:postgresql://localhost:5432/crm` |
| Username       | `crm_workflow`                         |
| Schema         | `crm_workflow`                         |

Liquibase runs on startup using `classpath:db/changelog/master.xml` (context
`dev`) to create the WORKFLOW tables, sequences, and indexes. JPA `ddl-auto` is
`none` — the schema is owned entirely by Liquibase. The changelog is currently
empty; add changesets as the domain model lands.

### Database Setup

A `docker-compose.yml` at the project root starts a PostgreSQL 16 instance
(container `postgres_db`) with a superuser `admin` / `admin` and a `crm`
database, exposed on port `5432`:

```bash
docker compose up -d
```

The compose file only creates the `admin` superuser and `crm` database. The
application connects as a dedicated `crm_workflow` role that owns its own schema,
so after the container is up, create the role and schema once:

```sql

CREATE USER crm_workflow
  WITH PASSWORD 'admin';

CREATE SCHEMA crm_workflow
  AUTHORIZATION crm_workflow;

```
Run them against the `crm` database.

With the role and schema in place, Liquibase can create its tracking tables and
apply the changelog on the next application start.

### Build & Run

```bash
# Run the app
./mvnw spring-boot:run

# Build a jar
./mvnw clean package

# Run tests
./mvnw test
```

The service starts on http://localhost:8401.
## Docker

A `Dockerfile` is provided that packages the built JAR on top of an
`eclipse-temurin:21-jre-alpine` base image. Build the JAR first, then the image:

```bash
./mvnw clean package
docker build -t crm-workflow .
```

Run the container, mapping the service's port. Note the `Dockerfile` currently
exposes `8401` while the app listens on `8401`, so either update `EXPOSE` or
override the port at run time:

```bash
docker run --rm -p 8401:8401 crm-workflow
```

## CI/CD

A `Jenkinsfile` defines a declarative pipeline that:

- **Build** — runs `mvn clean package -DskipTests` and archives the resulting JAR.
- **Test** — runs `mvn test` and publishes the JUnit surefire reports.
- **Docker Build & Push** — logs in to Docker Hub, builds the image tagged with
  the Jenkins `BUILD_NUMBER`, and pushes it, then removes the local image
  afterward.

The pipeline requires JDK 21 (`jdk-21`) and Maven (`maven-3.9`) tool
installations configured in Jenkins, plus a `docker-hub-credentials`
username/password credential. The `DOCKER_HUB_USER` build parameter defaults to
`mba90` — update it to your own Docker Hub account. The pipeline still uses
`crm-workflow` for `IMAGE_NAME` and the artifact pattern
`target/crm-workflow-*.jar`, which does not match this project's artifactId
(`crm-workflow`); update both in the `Jenkinsfile`.