# Project guidance

## Overview

Mylib is a personal book library manager. It manages books, tags, reading
history, statistics, ISBN lookups, and exports. It is a single Maven application
with a server-rendered Thymeleaf interface.

Use `pom.xml` and the source code as the source of truth: `readme.md` still
mentions Java 13 and Spring Boot 2, but the project currently targets Java 23
and Spring Boot 3.2.4.

## Project layout

- `src/main/java/com/obourgain/mylib/MylibApplication.java`: application entry point.
- `web/`: Spring MVC controllers.
- `service/`: business logic for books, readings, tags, users, and statistics.
- `db/`: Spring Data JPA repositories.
- `vobj/`: domain entities.
- `ext/`: book metadata and cover integrations with external sites.
- `util/auth/`: Google OAuth2 configuration and authenticated user handling.
- `util/search/`: Lucene search integration.
- `util/img/`: local cover image storage.
- `src/main/resources/templates/`: Thymeleaf pages and shared fragments.
- `src/main/resources/static/`: JavaScript, CSS, and bundled Bootstrap assets.
- `src/main/resources/sql/stats/`: statistics queries.
- `src/main/resources/application.yml`: application configuration.
- `src/test/resources/`: test fixtures; currently includes an Amazon HTML sample.

Java package paths above are relative to `src/main/java/com/obourgain/mylib/`.

## Build and run

Use JDK 23. Run commands from the repository root:

```sh
./mvnw test
./mvnw package
./mvnw spring-boot:run
```

The Maven wrapper pins Maven 3.5.0. A locally installed Maven can also run
the same goals. Packaging creates `target/mylib-23.jar` at the current project
version; run it with `java -jar target/mylib-23.jar`.

Starting the application requires `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`
in the environment. The default HTTP port is `2017`. The Google OAuth2 redirect
URI in `WebSecurity` explicitly uses HTTPS, so local login may require matching
proxy and Google client configuration.

## Implementation conventions

- Follow the surrounding Java formatting and naming conventions.
- Keep request handling in controllers, business logic in services, and database
  access in repositories.
- Preserve user ownership checks when reading or modifying books and related data.
- Keep book persistence and Lucene index updates consistent when changing book flows.
- Update templates and related JavaScript together when changing form fields or routes.
- Keep custom frontend changes in `static/css/style.css`, `static/js/mylib.js`,
  and `static/js/stats.js`; avoid editing bundled Bootstrap files.
- Use fixtures for external HTML parsing tests instead of relying on live websites.
- Keep dependency and framework upgrades scoped to an explicit task.

## Data and verification

The application uses a file-backed H2 database at `~/mylib/db/database`, cover
images in `~/mylib/store`, and a local Lucene index. Hibernate is configured with
`ddl-auto: update`. Use isolated storage when testing startup or persistence
changes; do not modify an existing personal library as part of verification.

Keep credentials out of source control. `secret.sh` is ignored and may be used
locally to export OAuth2 credentials.

For code changes, run the relevant Maven tests and package the application when
compilation or integration is affected. There are currently no Java test files
in the repository, so a successful test goal alone does not establish behavior
coverage. Add focused regression tests for substantive behavior changes, and
report any checks that could not run. For UI changes, verify the affected page
and interactions when a runnable environment is available.
