# Technology Stack

**Analysis Date:** 2026-04-22

## Languages

**Primary:**
- Java 21 (source/target/release = 21) — configured in root `pom.xml` via `<maven.compiler.release>21</maven.compiler.release>`. All backend, middleware, and utility modules compile against Java 21.
- Kotlin 2.3.0 — transitive runtime dependency of Javalin, pinned via `kotlin-stdlib` in `qqq-middleware-javalin/pom.xml`. No first-party Kotlin sources.

**Secondary:**
- JavaScript (scripted user code) — executed at runtime via Nashorn in `qqq-language-support-javascript`. Used to let QQQ application developers write custom scripts/logic that get evaluated by the framework. Not used for framework source code itself.
- Groovy (tooling only) — developer scripts in `qqq-dev-tools/bin/*.groovy` (e.g. `createTableToRecordEntity.groovy`, `liquibaseColumnsToEntityFields.groovy`). Not shipped with framework artifacts.
- Bash — developer tooling under `qqq-dev-tools/bin/*.sh` and shell-exec blocks embedded in `pom.xml` for Jacoco summary parsing.
- Java 11 (legacy overrides, to be reviewed for 4.0) — `qqq-utility-lambdas/pom.xml` sets `<maven.compiler.source>11</maven.compiler.source>`, and `qqq-middleware-javalin/pom.xml` has a compiler-plugin block that sets `<source>11</source><target>11</target>` specifically for the Javalin OpenAPI annotation processor. These coexist with the project-wide Java 21 release target.

## Runtime

**Environment:**
- JVM: Java 21 LTS (migrated from Java 17 in v0.35.0 per `CHANGELOG.md`).
- Qodana CI analysis is pinned to `projectJDK: 17` in `qodana.yaml` (lagging behind the 21 migration — candidate for 4.0 cleanup).

**Package Manager:**
- Apache Maven (3.8+ required per `README.md`). No Maven wrapper (`mvnw` / `.mvn/`) is present — contributors must supply their own `mvn`.
- Build metadata: CI-friendly `${revision}` property drives all module versions; `flatten-maven-plugin` 1.7.3 resolves `${revision}` at publish time.
- Lockfile: none (Maven does not use lockfiles; version pinning is via explicit `<version>` in `pom.xml` / `dependencyManagement`).

## Frameworks

**Core application framework:**
- QQQ (this project) — metadata-driven low-code framework. The root `README.md` markets it as "Metadata-driven application framework for building business software in Java."

**HTTP / middleware:**
- Javalin 6.7.0 — `io.javalin:javalin` in `qqq-middleware-javalin/pom.xml`. Used as the embedded HTTP server.
  - Annotation processor: `io.javalin.community.openapi:openapi-annotation-processor` 6.7.0 generates OpenAPI specs at compile time.
- Jetty 11.0.26 — overridden globally via `jetty.version` in root `pom.xml` and imported as BOM (`org.eclipse.jetty:jetty-bom`). Pinned for CVE remediation (HTTP/2). Transitively loaded by Javalin.
- Kotlin stdlib 2.3.0 — required by Javalin.
- SLF4J Simple 1.7.36 / 2.0.16 — present in `qqq-middleware-javalin` and `qqq-middleware-slack` to route SLF4J loggers.

**CLI:**
- picocli 4.7.7 (`info.picocli:picocli` + `picocli-shell-jline3`) — used in `qqq-middleware-picocli`. Entrypoint `com.kingsrook.qqq.frontend.picocli.QPicoCliImplementation`.

**Scheduling:**
- Quartz 2.5.2 (`org.quartz-scheduler:quartz`) — in `qqq-backend-core/pom.xml`. Implementation classes live under `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/scheduler/quartz/`.
- c3p0 / mchange-commons-java 0.3.2 — transitively required by Quartz, version pinned in `qqq-backend-core/pom.xml`.

**Scripting:**
- Nashorn 15.7 (`org.openjdk.nashorn:nashorn-core`) — JS engine in `qqq-language-support-javascript`. (Nashorn was removed from the JDK; `openjdk.nashorn` is the standalone fork.)

**Templating:**
- Velocity Engine Core 2.4.1 (`org.apache.velocity:velocity-engine-core`) — `qqq-backend-core`.

**Document rendering / parsing:**
- Jackson 2.21.0 (`jackson-databind`, `jackson-datatype-jsr310`, `jackson-dataformat-yaml`) — JSON + YAML.
- org.json 20251224 (`org.json:json`) — secondary JSON usage (including in `SecretsManagerUtils.java`).
- commonmark 0.27.1 — Markdown.
- jsoup 1.22.1 — HTML parsing.
- openhtmltopdf 1.0.10 (`openhtmltopdf-core`, `openhtmltopdf-pdfbox`) — HTML → PDF.
- fastexcel 0.19.0 + fastexcel-reader (`org.dhatim:fastexcel`) — streaming Excel I/O.
- Apache POI 5.5.1 (`poi`, `poi-ooxml`) — Excel / Office docs.
- Apache Commons CSV 1.14.1 — CSV parsing.
- ICU4J 78.2 (`com.ibm.icu:icu4j`) — Unicode / locale handling.

**Email / messaging:**
- Jakarta Mail 2.0.2 (`com.sun.mail:jakarta.mail`) — SMTP mail.
- Angus Activation 2.0.3 (`org.eclipse.angus:angus-activation`) — jakarta.activation handler implementations.

**Auth / identity:**
- Auth0 Java SDK 3.0.0 (`com.auth0:auth0`).
- Auth0 java-jwt 4.5.0.
- Auth0 jwks-rsa 0.23.0.
- Nimbus OAuth2 OIDC SDK 11.31.1 (`com.nimbusds:oauth2-oidc-sdk`).

**Testing:**
- JUnit Jupiter 6.0.2 (`junit-jupiter-engine`, `junit-jupiter-params`) — managed in root `pom.xml`.
- AssertJ 3.27.6 (`org.assertj:assertj-core`).
- Mockito 5.21.0 (`mockito-core`) + ByteBuddy 1.18.4 (`net.bytebuddy:byte-buddy`).
- WireMock 3.13.2 (`org.wiremock:wiremock`) — HTTP stubbing in `qqq-backend-core`.
- Testcontainers 2.0.3 (`org.testcontainers:testcontainers`, `testcontainers-mongodb`, `testcontainers-postgresql`, `testcontainers-junit-jupiter`).
- LocalStack 0.2.23 (`cloud.localstack:localstack-utils`) — AWS emulation for `qqq-backend-core` and `qqq-backend-module-filesystem`.
- H2 2.4.240 (`com.h2database:h2`) — in-memory RDBMS for tests in `qqq-backend-module-rdbms`, `qqq-middleware-javalin`, `qqq-middleware-picocli`, `qqq-middleware-api`, `qqq-middleware-health`.
- Unirest Java 3.14.5 (`com.konghq:unirest-java`) — HTTP test client in `qqq-middleware-javalin` and `qqq-middleware-api`.
- JNA 5.18.1 (`net.java.dev.jna:jna`) — required by Testcontainers.

**Build/Dev:**
- maven-compiler-plugin 3.14.1
- maven-surefire-plugin 3.5.4
- maven-checkstyle-plugin 3.6.0 (Checkstyle engine 13.0.0)
- maven-pmd-plugin 3.28.0 (PMD core/java 7.20.0)
- spotbugs-maven-plugin 4.9.8.2 + find-sec-bugs plugin 1.14.0
- jacoco-maven-plugin 0.8.14 (coverage gate: 80% instructions, 95% classes at root; individual modules override, e.g. `qqq-middleware-lambda` at 10%, `qqq-middleware-health` at 70/90)
- flatten-maven-plugin 1.7.3 (resolves `${revision}`)
- versions-maven-plugin 2.21.0
- gitflow-maven-plugin 1.21.0 (production=`main`, development=`dev`, release prefix=`rel/`, tag prefix=`version-`)
- maven-shade-plugin 2.4.3 (activated via `-P buildShadedJar`, phase wired through `plugin.shade.phase` property)
- maven-source-plugin 3.4.0 + maven-javadoc-plugin 3.12.0 (release profile only)
- maven-gpg-plugin 3.2.8 (release profile only)
- central-publishing-maven-plugin 0.10.0 (Sonatype Central — `publishingServerId=central`, `autoPublish=true`)
- maven-assembly-plugin (no explicit version, `qqq-middleware-picocli`) — builds jar-with-dependencies for CLI.
- appassembler-maven-plugin 1.10 — generates `ValidateApiVersions` program in `qqq-middleware-javalin`.
- exec-maven-plugin 3.6.3 — runs shell block that parses Jacoco HTML/XML for a per-module coverage summary during `verify`.

## Key Dependencies

**Critical:**
- `org.apache.commons:commons-lang3` 3.20.0 — replaced legacy `commons-lang` 2.x (CVE remediation per `CHANGELOG.md`).
- `commons-validator:commons-validator` 1.10.1 — `ValidationUtils`.
- `commons-io:commons-io` 2.21.0 — required by FastExcel.
- `com.github.hervian:safety-mirror` 4.0.1 — generates `Method` objects from method references; used by `QFieldMetaData` to derive fields from getter method references.
- `io.github.cdimascio:dotenv-java` 3.2.0 — `.env` file loading.
- `org.apache.logging.log4j:log4j-api` / `log4j-core` 2.25.3 — logging backend for all modules; `log4j-slf4j-impl` 2.25.3 routes SLF4J callers (many third-party deps) to Log4j.
- `com.google.protobuf:protobuf-java` 4.33.3 — pinned in root `dependencyManagement` to remediate a DoS CVE brought transitively by `mysql-connector-j`.

**AWS / Infrastructure (AWS SDK v1 + v2 mixed):**
- AWS SDK v2 BOM `software.amazon.awssdk:bom` 2.41.10 (`qqq-backend-core/pom.xml`) importing:
  - `software.amazon.awssdk:quicksight` — dashboard rendering via `actions/dashboard/widgets/QuickSightChartRenderer.java`.
  - `software.amazon.awssdk:apigateway`.
- AWS SDK v1 1.12.797:
  - `aws-java-sdk-secretsmanager` — in `SecretsManagerUtils.java`.
  - `aws-java-sdk-sqs` — in `scheduler/schedulable/runner/SchedulableSQSQueueRunner.java`, `actions/queues/SQSQueuePoller.java`, `actions/queues/GetQueueSize.java`, `qqq-utility-lambdas/QPostToSQSLambda.java`.
  - `aws-java-sdk-ses` — in `model/metadata/messaging/ses/SendSESAction.java`.
  - `aws-java-sdk-s3` — in `qqq-backend-module-filesystem/pom.xml`, used throughout `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/s3/`.
- `com.amazonaws:aws-lambda-java-core` 1.4.0, `aws-lambda-java-events` 3.16.1, `aws-lambda-java-runtime-interface-client` 2.9.0 — used in `qqq-middleware-lambda` and `qqq-utility-lambdas`.
- Note: Mixing AWS SDK v1 (`com.amazonaws`) and v2 (`software.amazon.awssdk`) is a known 4.0 cleanup candidate.

**Google:**
- `com.google.api-client:google-api-client` 1.35.2
- `com.google.auth:google-auth-library-oauth2-http` 1.41.0
- `com.google.apis:google-api-services-drive` v3-rev20251114-2.0.0 — Google Drive integration.

**Database drivers:**
- `com.mysql:mysql-connector-j` 9.5.0 — migrated from deprecated `mysql:mysql-connector-java`.
- `org.postgresql:postgresql` 42.7.9.
- `org.xerial:sqlite-jdbc` 3.51.1.0.
- `org.mongodb:mongodb-driver-sync` 5.6.2.
- `com.mchange:c3p0` 0.11.2 — connection pooling.

**Protocols / integrations:**
- `org.apache.sshd:sshd-sftp` 2.16.0 — SFTP backend.
- `com.slack.api:slack-api-client` 1.46.0 — Slack middleware.

## Configuration

**Environment:**
- `.env` files supported via `dotenv-java` 3.2.0 (`io.github.cdimascio:dotenv-java`) loaded in `qqq-backend-core`.
- AWS SecretsManager integration at `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/SecretsManagerUtils.java` for runtime secret resolution.
- Configuration is primarily expressed as Java metadata (QInstance, table/backend/process metadata) rather than external config files.

**Build:**
- `pom.xml` (root): aggregator + `dependencyManagement` + plugin config for all 17 modules.
- `qqq-bom/pom.xml`: `qqq-bom-pom` BOM for downstream consumers.
- `checkstyle/config.xml`, `checkstyle/license.txt`: Checkstyle rules + license header enforcement (tabs forbidden, Allman-style braces (`LeftCurly option=nl`), star imports forbidden, one top-level class).
- `pmd/ruleset.xml`: PMD rules.
- `spotbugs/exclude-filter.xml`: SpotBugs exclusions.
- `suppression.xml`: top-level suppression rules.
- `spotbugs-summary.csv`: tracked SpotBugs summary artifact.
- `qodana.yaml`: JetBrains Qodana CI config (profile `qodana.starter`, linter `jetbrains/qodana-jvm:latest`, `projectJDK: 17` — stale vs runtime Java 21).
- `.circleci/config.yml`: CircleCI pipeline.
- `.circleci/mvn-settings.xml`: Maven settings used in CI.

**CI / Release:**
- CircleCI orb `kingsrook/qqq-orb@0.6.0` provides jobs `mvn_test_only`, `mvn_publish`, `static_analysis`.
- Workflows: `test_only` (non-release branches), `publish_snapshot` (`develop`), `publish_feature` (`feature/*` with `publish*` tags), `publish_release_candidate` (`release/*`), `publish_release` (`main` + `v*` tags), `publish_hotfix_release` (`hotfix/*`).
- Publishing: `central-publishing-maven-plugin` to Sonatype Central with `publishingServerId=central`; GPG signing via `maven-gpg-plugin` in `release` profile.
- Snapshot repository: `qqq-middleware-javalin/pom.xml` references `https://central.sonatype.com/repository/maven-snapshots/` for `qqq-frontend-material-dashboard` test-scope snapshots.

## Platform Requirements

**Development:**
- Java 21 JDK (per root `<maven.compiler.release>21</maven.compiler.release>` and `README.md` Prerequisites).
- Maven 3.8+ (per `README.md`).
- Docker available for integration tests that use Testcontainers (MongoDB, PostgreSQL) and LocalStack (`qqq-backend-core`, `qqq-backend-module-filesystem`).

**Production:**
- JVM-compatible runtime (Java 21).
- Packaged as Maven library artifacts published to Maven Central under `com.kingsrook.qqq`.
- Deployable forms consumers build on:
  - Embedded Javalin HTTP server (via `qqq-middleware-javalin`).
  - AWS Lambda (via `qqq-middleware-lambda`, `qqq-utility-lambdas` — shaded fat-jars produced with `-P buildShadedJar`).
  - CLI (via `qqq-middleware-picocli`, jar-with-dependencies).
- Kubernetes-aware: `qqq-middleware-health` provides Kubernetes-compatible health endpoints (per its `<description>`).

## Multi-Module Overview

This is a 17-module Maven monorepo (as declared in root `pom.xml`), all sharing `${revision}` (currently `0.42.0-SNAPSHOT`) and the same plugin/dependency-management block. Module-by-module summary:

**BOM / aggregator:**
- `qqq-bom` (artifactId `qqq-bom-pom`, packaging `pom`) — bill of materials aligning all QQQ module versions. Reference: `qqq-bom/pom.xml`.

**Core:**
- `qqq-backend-core` — framework core: `actions`, `adapters`, `context`, `exceptions`, `instances`, `logging`, `model`, `modules` (`authentication`, `backend`, `messaging`), `processes`, `scheduler` (incl. `quartz`), `state`, `utils`. Pulls in AWS v1 (Secrets, SQS, SES), AWS v2 BOM (QuickSight, API Gateway), Auth0, Nimbus OAuth2, Google APIs, Quartz, Velocity, Jackson, POI, FastExcel, openhtmltopdf, jsoup, commonmark, Jakarta Mail. Reference: `qqq-backend-core/pom.xml`.

**Backends (data sources):**
- `qqq-backend-module-api` — uses a remote HTTP API as a backend. Thin module: depends only on `qqq-backend-core`. Source: `qqq-backend-module-api/src/main/java/com/kingsrook/qqq/backend/module/api/actions/` (`APIQueryAction.java`, `APIInsertAction.java`, `APIUpdateAction.java`, `APIDeleteAction.java`, `APIGetAction.java`, `APICountAction.java`, `AbstractAPIAction.java`, `BaseAPIActionUtil.java`). Reference: `qqq-backend-module-api/pom.xml`.
- `qqq-backend-module-rdbms` — generic JDBC/SQL backend; bundles MySQL driver (`mysql-connector-j` 9.5.0), c3p0 pool, H2 (test). Reference: `qqq-backend-module-rdbms/pom.xml`.
- `qqq-backend-module-postgres` — PostgreSQL-specific: extends `qqq-backend-module-rdbms`, adds `postgresql` 42.7.9 and Testcontainers-postgresql. Reference: `qqq-backend-module-postgres/pom.xml`.
- `qqq-backend-module-sqlite` — SQLite-specific: extends `qqq-backend-module-rdbms`, adds `sqlite-jdbc` 3.51.1.0. Reference: `qqq-backend-module-sqlite/pom.xml`.
- `qqq-backend-module-mongodb` — Mongo-specific via `mongodb-driver-sync` 5.6.2 + Testcontainers-mongodb. Reference: `qqq-backend-module-mongodb/pom.xml`.
- `qqq-backend-module-filesystem` — unified filesystem backend with three drivers under `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/`: `local/`, `s3/` (AWS SDK v1 `aws-java-sdk-s3` 1.12.797), `sftp/` (`org.apache.sshd:sshd-sftp` 2.16.0). Tests use LocalStack + Testcontainers. Reference: `qqq-backend-module-filesystem/pom.xml`.

**Language support:**
- `qqq-language-support-javascript` — JavaScript execution for user-defined scripts via Nashorn 15.7. Depends only on `qqq-backend-core`. Reference: `qqq-language-support-javascript/pom.xml`.

**API / Spec:**
- `qqq-openapi` — OpenAPI spec integration for QQQ; depends only on `qqq-backend-core`. Reference: `qqq-openapi/pom.xml`.

**Middleware (how clients talk to QQQ):**
- `qqq-middleware-javalin` — embedded HTTP server using Javalin 6.7.0 + Jetty 11.0.26 + Kotlin stdlib; OpenAPI annotation-processor-generated specs; bundles `QJavalinImplementation`, `QApplicationJavalinServer`, process handler, scripts handler, access logger, and route providers (process-based, SPA, simple filesystem directory). Also ships test-jar for downstream modules. Reference: `qqq-middleware-javalin/pom.xml`.
- `qqq-middleware-api` — HTTP API middleware layered on top of `qqq-middleware-javalin` + `qqq-openapi`. Reference: `qqq-middleware-api/pom.xml`.
- `qqq-middleware-picocli` — CLI using picocli 4.7.7; main class `com.kingsrook.qqq.frontend.picocli.QPicoCliImplementation`. Reference: `qqq-middleware-picocli/pom.xml`.
- `qqq-middleware-lambda` — AWS Lambda handler wrappers (`QAbstractLambdaHandler`, `QStandardLambdaHandler`, `QBaseCustomLambdaHandler`). Uses `aws-lambda-java-*` 1.4.0 / 3.16.1 / 2.9.0. Coverage gates relaxed to 10% (explicit `todo - remove these!!` comment in `qqq-middleware-lambda/pom.xml`). Reference: `qqq-middleware-lambda/pom.xml`.
- `qqq-middleware-slack` — Slack integration via `slack-api-client` 1.46.0; depends on `qqq-backend-core` + `qqq-middleware-javalin`. Reference: `qqq-middleware-slack/pom.xml`.
- `qqq-middleware-health` — Kubernetes-compatible health check endpoints; provided-scope deps on `qqq-middleware-javalin` + Javalin 6.7.0; compile-scope dep on `qqq-backend-module-rdbms` (for DB health indicators). Coverage gate overridden to 70/90. Reference: `qqq-middleware-health/pom.xml`.

**Utilities:**
- `qqq-utility-lambdas` — standalone AWS Lambda functions (e.g. `QPostToSQSLambda.java`). Notably does NOT depend on `qqq-backend-core`; uses Java 11 source level; Checkstyle block commented out. Reference: `qqq-utility-lambdas/pom.xml`.

**Not declared as a Maven module (excluded from root `<modules>`):**
- `qqq-sample-project` — reference consumer/example; depends on older published snapshots (`qqq-frontend-material-dashboard` 0.24.0, H2 2.2.220). Intentionally skipped from Maven Central publish. Reference: `qqq-sample-project/pom.xml`.
- `qqq-dev-tools` — developer shell/groovy/intellij scripts (`qqq-dev-tools/bin/`, `qqq-dev-tools/intellij/`), not a build artifact. Not in root `<modules>`.

Module dependency fan-in: every non-BOM, non-utility-lambda module depends (directly or transitively) on `qqq-backend-core`. `qqq-middleware-javalin` is the next biggest hub, pulled in by `qqq-middleware-api`, `qqq-middleware-slack`, and (provided) `qqq-middleware-health`.

---

*Stack analysis: 2026-04-22*
