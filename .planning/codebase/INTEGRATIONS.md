# External Integrations

**Analysis Date:** 2026-04-22

QQQ is a framework, so "integrations" covers two distinct axes:

1. **Integrations QQQ consumes / speaks to** — third-party systems that QQQ itself calls out to (AWS services, Auth0, Google Drive, Slack, Mongo, etc.).
2. **Integrations QQQ exposes to its users** — how downstream application developers plug their apps into the framework (HTTP REST/OpenAPI, CLI, AWS Lambda, etc.).

Both are covered below.

## APIs & External Services

**HTTP API backends (QQQ-as-client to external REST APIs):**
- `qqq-backend-module-api` — generic module that treats any remote HTTP API as a QQQ "backend," providing query/insert/update/delete/count/get semantics against it.
  - Implementation: `qqq-backend-module-api/src/main/java/com/kingsrook/qqq/backend/module/api/actions/` (`APIQueryAction.java`, `APIInsertAction.java`, `APIUpdateAction.java`, `APIDeleteAction.java`, `APIGetAction.java`, `APICountAction.java`, `AbstractAPIAction.java`, `BaseAPIActionUtil.java`, `APIRecordUtils.java`, `QHttpResponse.java`).
  - Bootstrapper: `qqq-backend-module-api/src/main/java/com/kingsrook/qqq/backend/module/api/APIBackendModule.java`.
  - Auth/connection configuration comes from QInstance metadata supplied by consuming applications.

**HTTP API exposed (QQQ-as-server):**
- `qqq-middleware-javalin` — embedded Javalin 6.7.0 HTTP server. Entry: `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/QApplicationJavalinServer.java`; implementation in `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/backend/javalin/QJavalinImplementation.java`. Route providers under `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/routeproviders/` support process-based routes, SPA hosting, isolated-SPA, and simple filesystem directory routes.
- `qqq-middleware-api` — HTTP layer on top of `qqq-middleware-javalin` that publishes the standard QQQ REST API (tables, processes, reports, metadata).
- `qqq-openapi` — OpenAPI 3.x spec generation for the QQQ HTTP surface. Javalin integration uses `io.javalin.community.openapi:openapi-annotation-processor` 6.7.0 at compile time. The middleware exposes a `QMiddlewareApiSpecHandler` (`qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/QMiddlewareApiSpecHandler.java`) and a `ValidateAPIVersions` tool binary built via `appassembler-maven-plugin`.
- Versioned API contracts live under `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/specs/` (e.g. v1 specs mentioned in test files such as `ProcessMetaDataSpecV1Test.java`, `TableQuerySpecV1Test.java`).

**AWS services (QQQ consumes):**
- **AWS Secrets Manager** — `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/SecretsManagerUtils.java`. Uses AWS SDK v1 `com.amazonaws.services.secretsmanager.AWSSecretsManager` with `BasicAWSCredentials` / `AWSStaticCredentialsProvider`. SDK: `com.amazonaws:aws-java-sdk-secretsmanager` 1.12.797 (`qqq-backend-core/pom.xml`).
- **AWS SQS** — queue polling + enqueue. Implementations in `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/queues/SQSQueuePoller.java`, `GetQueueSize.java`, and `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/scheduler/schedulable/runner/SchedulableSQSQueueRunner.java`. Metadata: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/queues/` (`SQSQueueMetaData.java`, `SQSQueueProviderMetaData.java`, `SQSPollerSettings.java`, `QQueueProviderMetaData.java`, `QueueType.java`). Standalone lambda that posts to SQS: `qqq-utility-lambdas/src/main/java/com/kingsrook/qqq/utilitylambdas/QPostToSQSLambda.java`. SDK: `com.amazonaws:aws-java-sdk-sqs` 1.12.797.
- **AWS SES (Simple Email Service)** — `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/messaging/ses/` (`SESMessagingProvider.java`, `SESMessagingProviderMetaData.java`, `SendSESAction.java`). SDK: `com.amazonaws:aws-java-sdk-ses` 1.12.797.
- **AWS S3** — `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/s3/` (driver `S3BackendModule.java`, `actions/S3QueryAction.java`, `S3InsertAction.java`, `S3UpdateAction.java`, `S3DeleteAction.java`, `S3CountAction.java`, `S3StorageAction.java`, `AbstractS3Action.java`, `utils/S3Utils.java`, `utils/S3UploadOutputStream.java`). SDK: `com.amazonaws:aws-java-sdk-s3` 1.12.797. Tested against LocalStack.
- **AWS QuickSight** — dashboard widget renderer at `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/dashboard/widgets/QuickSightChartRenderer.java`. SDK: AWS SDK v2 `software.amazon.awssdk:quicksight` (BOM 2.41.10).
- **AWS API Gateway** — SDK v2 `software.amazon.awssdk:apigateway` present in `qqq-backend-core/pom.xml` (callsites under `qqq-backend-core`).
- **AWS Lambda (QQQ-as-lambda-runtime)** — consumer applications deploy QQQ workloads as Lambda functions. Handlers:
  - `qqq-middleware-lambda/src/main/java/com/kingsrook/qqq/lambda/QAbstractLambdaHandler.java`
  - `qqq-middleware-lambda/src/main/java/com/kingsrook/qqq/lambda/QStandardLambdaHandler.java`
  - `qqq-middleware-lambda/src/main/java/com/kingsrook/qqq/lambda/QBaseCustomLambdaHandler.java`
  - Example: `qqq-middleware-lambda/src/main/java/com/kingsrook/qqq/lambda/examples/ExampleLambdaHandler.java`.
  - Request/response models: `qqq-middleware-lambda/src/main/java/com/kingsrook/qqq/lambda/model/QLambdaRequest.java` / `QLambdaResponse.java`.
  - SDKs: `com.amazonaws:aws-lambda-java-core` 1.4.0, `aws-lambda-java-events` 3.16.1, `aws-lambda-java-runtime-interface-client` 2.9.0.
  - Standalone utility lambdas in `qqq-utility-lambdas/`.
- **LocalStack** — `cloud.localstack:localstack-utils` 0.2.23 (test scope) in `qqq-backend-core/pom.xml` and `qqq-backend-module-filesystem/pom.xml` for emulating AWS services in tests.

**Mixed SDK versions warning:** QQQ uses both AWS SDK v1 (`com.amazonaws:aws-java-sdk-*` 1.12.797 for Secrets Manager, SQS, SES, S3) and AWS SDK v2 (`software.amazon.awssdk:bom` 2.41.10 for QuickSight, API Gateway). This is a 4.0 cleanup candidate.

**Google APIs:**
- **Google Drive** — file storage integration. Deps in `qqq-backend-core/pom.xml`:
  - `com.google.api-client:google-api-client` 1.35.2
  - `com.google.auth:google-auth-library-oauth2-http` 1.41.0
  - `com.google.apis:google-api-services-drive` v3-rev20251114-2.0.0

**Slack:**
- `qqq-middleware-slack` — Slack integration via Bolt-style SDK `com.slack.api:slack-api-client` 1.46.0. Depends on `qqq-backend-core` + `qqq-middleware-javalin`. Reference: `qqq-middleware-slack/pom.xml`.

**SFTP:**
- `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/sftp/` — SFTP driver (`SFTPBackendModule.java` plus `actions/`, `model/`, `utils/`). Library: `org.apache.sshd:sshd-sftp` 2.16.0.

**SMTP Email:**
- Jakarta Mail 2.0.2 (`com.sun.mail:jakarta.mail`) + Angus Activation 2.0.3 — generic SMTP mail sender used for email messaging provider (see `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/messaging/email/`).

**HTTP / PDF / Office rendering:**
- HTML → PDF via openhtmltopdf 1.0.10 + jsoup 1.22.1 (`qqq-backend-core/pom.xml`) — used for report rendering (e.g. `processes/implementations/savedreports/RenderSavedReportExecuteStep.java`).
- Excel read/write: fastexcel 0.19.0 and Apache POI 5.5.1.
- CSV: Apache Commons CSV 1.14.1.

## Data Storage

**Relational databases (via `qqq-backend-module-rdbms`):**
- **MySQL** — driver `com.mysql:mysql-connector-j` 9.5.0 (migrated from deprecated `mysql:mysql-connector-java` per `CHANGELOG.md`).
- **PostgreSQL** — driver `org.postgresql:postgresql` 42.7.9 (via `qqq-backend-module-postgres` extending `qqq-backend-module-rdbms`). Integration tested with Testcontainers-postgresql 2.0.3.
- **SQLite** — driver `org.xerial:sqlite-jdbc` 3.51.1.0 (via `qqq-backend-module-sqlite`).
- **H2** — `com.h2database:h2` 2.4.240 (test scope in rdbms, javalin, picocli, api, health modules). `qqq-sample-project` uses 2.2.220 (older).
- **Connection pooling** — `com.mchange:c3p0` 0.11.2. `mchange-commons-java` 0.3.2 pinned in `qqq-backend-core/pom.xml` to override Quartz transitive.

**Document database:**
- **MongoDB** — driver `org.mongodb:mongodb-driver-sync` 5.6.2 (via `qqq-backend-module-mongodb`). Integration tested with Testcontainers-mongodb 2.0.3. Test imports confirm usage of `com.mongodb.client.MongoClient`, `MongoCollection`, `MongoDatabase` (e.g. `qqq-backend-module-mongodb/src/test/java/com/kingsrook/qqq/backend/module/mongodb/BaseTest.java`).

**File Storage (via `qqq-backend-module-filesystem`):**
- **Local filesystem** — `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/local/`.
- **AWS S3** — `.../filesystem/s3/` (see AWS section above).
- **SFTP** — `.../filesystem/sftp/` (see SFTP above).
- **Google Drive** — via `google-api-services-drive` in `qqq-backend-core`.

**Caching:**
- No dedicated cache service (no Redis / Memcached dependencies detected). In-memory caching appears to be handled within QQQ itself (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/backend/implementations/memory/`).

**State / session storage:**
- Pluggable state provider interface in `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/state/`.
- Authentication session storage pluggable via `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/QSessionStoreProviderInterface.java`, `QSessionStoreHelper.java`, `QSessionStoreRegistry.java`.

## Authentication & Identity

**Pluggable auth providers** — dispatcher-based architecture:
- Entry: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/QAuthenticationModuleDispatcher.java`
- Interface: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/QAuthenticationModuleInterface.java`
- Customizer: `.../QAuthenticationModuleCustomizerInterface.java`

**Built-in implementations** (under `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/implementations/`):
- `Auth0AuthenticationModule.java` — Auth0 JWT-based authentication. Uses `com.auth0:auth0` 3.0.0, `com.auth0:java-jwt` 4.5.0, `com.auth0:jwks-rsa` 0.23.0.
- `OAuth2AuthenticationModule.java` — generic OAuth2 / OIDC via `com.nimbusds:oauth2-oidc-sdk` 11.31.1. Per `CHANGELOG.md` 0.40.0 adds `externalBaseUrl` to support split internal/external URLs for Kubernetes deployments where pods cannot reach LoadBalancer VIPs.
- `TableBasedAuthenticationModule.java` — local username/password auth backed by QQQ tables. 0.40.0 removed SHA1 backward compatibility; now only SHA256 (`sha256:iterations:salt:hash`).
- `MockAuthenticationModule.java` — testing.
- `FullyAnonymousAuthenticationModule.java` — no-auth mode.

**Javalin HTTP-level auth:**
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/routeproviders/authentication/RouteAuthenticatorInterface.java`
- `.../SimpleRouteAuthenticator.java`

## Monitoring & Observability

**Health checks:**
- `qqq-middleware-health` — Kubernetes-compatible health endpoints (liveness/readiness). Depends (provided scope) on `qqq-middleware-javalin` + Javalin 6.7.0; compile-scope dep on `qqq-backend-module-rdbms` for database health indicators. Reference: `qqq-middleware-health/pom.xml`.

**Logging:**
- Log4j 2.25.3 (`log4j-api` + `log4j-core`) is the backend logger across all modules (managed in root `pom.xml`).
- `log4j-slf4j-impl` 2.25.3 in `qqq-backend-core/pom.xml` routes SLF4J callers to Log4j.
- `slf4j-simple` 1.7.36 used as a runtime binding in `qqq-middleware-javalin/pom.xml` and `qqq-middleware-api/pom.xml`; `slf4j-simple` 2.0.16 + `slf4j-api` 2.0.17 in `qqq-middleware-slack/pom.xml`. (Mixed SLF4J versions — 4.0 cleanup candidate.)
- Custom access logger: `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/backend/javalin/QJavalinAccessLogger.java`.
- Framework logging helpers: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/logging/`.

**Error Tracking:**
- No external error-tracking SaaS integration detected (no Sentry / Rollbar / Datadog SDKs).

**Test coverage reporting:**
- Jacoco 0.8.14 produces HTML + XML reports per module; a custom `exec-maven-plugin` shell block parses `target/site/jacoco/index.html` during `verify` for a CLI summary.

## CI/CD & Deployment

**CI platform:**
- CircleCI. Pipeline: `.circleci/config.yml`.
- Shared orb: `kingsrook/qqq-orb@0.6.0`.
- Workflows:
  - `test_only` — feature branches (ignores `develop|main|release/.*|hotfix/.*|integration.*`). Runs `qqq-orb/mvn_test_only` + parallel `qqq-orb/static_analysis`.
  - `publish_snapshot` — `develop` branch; publishes `-SNAPSHOT` jars and runs static analysis.
  - `publish_feature` — `feature/*` branches, gated on `publish*` tags.
  - `publish_release_candidate` — `release/*` branches.
  - `publish_release` — `main` + `v*` tags.
  - `publish_hotfix_release` — `hotfix/*` branches.
- CircleCI contexts in use: `qqq-maven-registry-credentials`, `build-qqq-sample-app`.
- Maven settings for CI: `.circleci/mvn-settings.xml`.

**Static analysis:**
- Checkstyle 13.0.0 (`maven-checkstyle-plugin` 3.6.0) — config `checkstyle/config.xml`, license header `checkstyle/license.txt`, Allman braces, no star imports, no tabs, `violationSeverity=warning`, `failOnViolation=true`. Runs on every module at `validate` phase. Enforces AGPL / Apache-2.0 header (license migrated per `CHANGELOG.md` 0.40.0).
- PMD 7.20.0 (`maven-pmd-plugin` 3.28.0) — config `pmd/ruleset.xml`. Gated by `pmd.failOnViolation` (default `false` — report-only per root `pom.xml` comment).
- SpotBugs 4.9.8.2 + Find Security Bugs 1.14.0 plugin — config `spotbugs/exclude-filter.xml`. Gated by `spotbugs.failOnError` (default `false` — report-only). Effort `Max`, threshold `Medium`. Current findings tracked in `spotbugs-summary.csv`.
- Qodana (JetBrains) — config `qodana.yaml`, profile `qodana.starter`, linter `jetbrains/qodana-jvm:latest`, `projectJDK: 17` (stale — runtime is Java 21).

**Publishing:**
- Artifacts publish to **Maven Central** via `org.sonatype.central:central-publishing-maven-plugin` 0.10.0 (`publishingServerId=central`, `autoPublish=true`). No `distributionManagement` block in poms — publishing is plugin-driven.
- `release` profile attaches sources (`maven-source-plugin` 3.4.0), Javadoc (`maven-javadoc-plugin` 3.12.0), and GPG signs artifacts (`maven-gpg-plugin` 3.2.8, `--pinentry-mode loopback`).
- Sonatype snapshot repository referenced for upstream frontend snapshots in `qqq-middleware-javalin/pom.xml`: `https://central.sonatype.com/repository/maven-snapshots/`.
- Branch model (`gitflow-maven-plugin` 1.21.0 in root `pom.xml`): `productionBranch=main`, `developmentBranch=dev`, `releaseBranchPrefix=rel/`, `versionTagPrefix=version-`. (Note: CircleCI workflows treat `develop` as the snapshot branch and `release/*` (with slash) as RC — slight divergence from the gitflow plugin's `rel/` prefix.)

**Hosting / deployment targets (for QQQ consumers):**
- Embedded HTTP server — Javalin + Jetty, packaged as shaded fat-jar (via `-P buildShadedJar`).
- AWS Lambda — shaded fat-jar via `qqq-middleware-lambda` / `qqq-utility-lambdas`.
- CLI — jar-with-dependencies via `qqq-middleware-picocli`.
- Kubernetes — `qqq-middleware-health` provides k8s-compatible endpoints, and `OAuth2AuthenticationModule.externalBaseUrl` explicitly addresses pod-to-LoadBalancer URL split.

## Environment Configuration

**Configuration loader:**
- `.env` file support via `io.github.cdimascio:dotenv-java` 3.2.0 (`qqq-backend-core`).
- AWS Secrets Manager via `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/SecretsManagerUtils.java`.
- Configuration is predominantly expressed programmatically as Java metadata (QInstance, QTableMetaData, QBackendMetaData, etc.) rather than as declarative config files.

**Required env vars:**
- Not centrally defined in repo; vary per QQQ consumer application. The framework itself reads secrets on demand (e.g. AWS credentials when `SecretsManagerUtils` is invoked, Auth0 / OAuth2 issuer/audience via metadata, SMTP / SES connection info from messaging provider metadata).
- CI secrets are provided through CircleCI contexts `qqq-maven-registry-credentials` and `build-qqq-sample-app` (referenced in `.circleci/config.yml`).

**Secrets location:**
- Maven Central publishing credentials: CI `settings.xml` (`.circleci/mvn-settings.xml` + CircleCI context `qqq-maven-registry-credentials`), expected `<server id="central">` in the Maven `settings.xml`.
- GPG signing: environment-provided `${gpg.keyname}` (root `pom.xml` release profile).
- Runtime: AWS Secrets Manager for application secrets.
- No `.env` file committed to the repo (none detected).

## Webhooks & Callbacks

**Incoming:**
- Any routes the QQQ application author defines via `qqq-middleware-javalin` route providers (`qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/routeproviders/`): `ProcessBasedRouter`, `SimpleFileSystemDirectoryRouter`, `IsolatedSpaRouteProvider`. Framework does not define specific hard-coded webhook endpoints itself.
- Slack middleware (`qqq-middleware-slack`) is structured to run alongside `qqq-middleware-javalin`, enabling Slack event/interaction callbacks.
- Health endpoints from `qqq-middleware-health` (Kubernetes probes).

**Outgoing:**
- SQS message publishing via `SchedulableSQSQueueRunner`, `QPostToSQSLambda`.
- SES email sends via `SendSESAction`.
- Slack messages via `slack-api-client`.
- SMTP email via Jakarta Mail.
- HTTP calls from `qqq-backend-module-api` to arbitrary configured REST APIs.

## Messaging / Scheduling

**In-process scheduler:**
- Quartz 2.5.2 implementation under `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/scheduler/quartz/` (`QuartzScheduler.java`, `QuartzJobRunner.java`, `QuartzJobAndTriggerWrapper.java`, `processes/ResumeAllQuartzJobsProcess.java`, `tables/QuartzJobDataPostQueryCustomizer.java`).
- Simple scheduler fallback under `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/scheduler/simple/`.
- Schedule manager + interface: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/scheduler/QScheduleManager.java` + `QSchedulerInterface.java`.
- Cron helpers: `CronDescriber.java`, `CronExpressionTooltipFieldBehavior.java`.

**Queue/messaging abstractions:**
- Generic messaging provider: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/messaging/MessagingProviderInterface.java` + dispatcher.
- Concrete SES provider under `.../model/metadata/messaging/ses/`, email provider under `.../model/metadata/messaging/email/`.
- SQS queue provider metadata + poller under `.../model/metadata/queues/` and `.../actions/queues/`.

---

*Integration audit: 2026-04-22*
