# Codebase Structure

**Analysis Date:** 2026-04-22

## Directory Layout

```
qqq/
├── pom.xml                              # Parent POM (groupId=com.kingsrook.qqq, artifactId=qqq-parent-project, packaging=pom, Java 21)
├── README.md                            # Public overview
├── CHANGELOG.md                         # Release notes
├── CODE_OF_CONDUCT.md
├── CODE_STYLE.md                        # Formatting / style contract (~15KB, enforced by checkstyle)
├── CONTRIBUTING.md
├── SECURITY.md
├── LICENSE                              # AGPL-3.0
├── NOTICE
├── suppression.xml                      # Checkstyle/PMD suppressions (root-level, empty or near-empty)
├── spotbugs-summary.csv                 # Pre-computed spotbugs report export
├── qodana.yaml                          # JetBrains Qodana config
├── .circleci/                           # CircleCI pipeline config
├── .github/                             # GitHub Actions, issue templates
├── .idea/                               # IntelliJ shared project settings
├── .planning/                           # GSD planning artifacts (this file lives here)
│   └── codebase/
├── checkstyle/
│   ├── config.xml                       # Checkstyle rules
│   └── license.txt                      # AGPL header that every .java file MUST begin with
├── pmd/
│   └── ruleset.xml                      # PMD rules
├── spotbugs/
│   └── exclude-filter.xml               # SpotBugs exclusions
├── docs/                                # Asciidoc project documentation
│   ├── index.adoc
│   ├── Introduction.adoc
│   ├── actions/
│   ├── implementations/
│   ├── metaData/
│   ├── utilities/
│   ├── misc/
│   ├── justfile
│   ├── docinfo.html
│   └── variables.adoc
│
├── qqq-backend-core/                    # Framework core (largest module)
├── qqq-backend-module-rdbms/            # JDBC-backed storage (MySQL/H2/Aurora)
├── qqq-backend-module-postgres/         # Postgres specialization over rdbms
├── qqq-backend-module-sqlite/           # SQLite specialization over rdbms
├── qqq-backend-module-mongodb/          # MongoDB storage
├── qqq-backend-module-api/              # Remote REST API as a backend
├── qqq-backend-module-filesystem/       # Local FS / S3 / SFTP storage
│
├── qqq-openapi/                         # OpenAPI model POJOs
├── qqq-language-support-javascript/     # Run JS snippets as QCodeReferences
│
├── qqq-middleware-javalin/              # Javalin-based HTTP server
├── qqq-middleware-api/                  # Versioned public REST API
├── qqq-middleware-health/               # /healthz probes for Kubernetes
├── qqq-middleware-picocli/              # CLI frontend
├── qqq-middleware-lambda/               # AWS Lambda handler
├── qqq-middleware-slack/                # Slack bot frontend
│
├── qqq-utility-lambdas/                 # Standalone AWS Lambda utilities
├── qqq-sample-project/                  # Reference implementation (NOT released)
│
├── qqq-bom/                             # Bill-of-materials POM
│
└── qqq-dev-tools/                       # Maintainer scripts and QBit generator (NOT released, version 1.0.0-SNAPSHOT)
    ├── bin/                             # Shell scripts: release, snapshot deps, jacoco reporter, etc.
    ├── intellij/
    ├── lib/
    ├── src/main/java/.../devtools/
    ├── MODULE_LIST                      # Text list of modules consumed by tooling
    └── CURRENT-SNAPSHOT-VERSION
```

## Module Naming Convention

Every published module is prefixed with `qqq-` and follows a **role–kind–variant** pattern:

| Prefix | Meaning | Examples |
|---|---|---|
| `qqq-backend-core` | The framework heart. | (singleton) |
| `qqq-backend-module-<kind>` | A storage/integration backend implementing `QBackendModuleInterface`. | `qqq-backend-module-rdbms`, `qqq-backend-module-mongodb`, `qqq-backend-module-filesystem`, `qqq-backend-module-api` |
| `qqq-middleware-<kind>` | A transport/frontend layer that accepts external requests and invokes actions. | `qqq-middleware-javalin`, `qqq-middleware-picocli`, `qqq-middleware-lambda`, `qqq-middleware-slack`, `qqq-middleware-api`, `qqq-middleware-health` |
| `qqq-language-support-<lang>` | Executes foreign-language code via `QCodeReference`. | `qqq-language-support-javascript` |
| `qqq-<domain>` | Stand-alone modules. | `qqq-openapi`, `qqq-bom`, `qqq-sample-project`, `qqq-utility-lambdas`, `qqq-dev-tools` |

Special notes:
- `qqq-backend-module-postgres` and `qqq-backend-module-sqlite` extend `qqq-backend-module-rdbms` rather than `qqq-backend-core` directly — they are RDBMS specializations.
- `qqq-middleware-api` builds on top of `qqq-middleware-javalin`.
- `qqq-middleware-slack` and `qqq-middleware-health` also build on `qqq-middleware-javalin`.
- `qqq-dev-tools` uses its own hard-coded version (`1.0.0-SNAPSHOT`) instead of `${revision}` — it is not part of the release train.
- `qqq-bom` has artifactId `qqq-bom-pom` (with `-pom` suffix) even though its directory is `qqq-bom`.

## Java Package Convention

All production Java code is rooted at `com.kingsrook.qqq`. Within that, each module claims a stable sub-namespace:

| Module | Java root package |
|---|---|
| `qqq-backend-core` | `com.kingsrook.qqq.backend.core` |
| `qqq-backend-module-rdbms` | `com.kingsrook.qqq.backend.module.rdbms` |
| `qqq-backend-module-postgres` | `com.kingsrook.qqq.backend.module.postgres` |
| `qqq-backend-module-sqlite` | `com.kingsrook.qqq.backend.module.sqlite` |
| `qqq-backend-module-mongodb` | `com.kingsrook.qqq.backend.module.mongodb` |
| `qqq-backend-module-api` | `com.kingsrook.qqq.backend.module.api` |
| `qqq-backend-module-filesystem` | `com.kingsrook.qqq.backend.module.filesystem` |
| `qqq-middleware-javalin` | `com.kingsrook.qqq.backend.javalin` (legacy) **and** `com.kingsrook.qqq.middleware.javalin` (newer) |
| `qqq-middleware-api` | `com.kingsrook.qqq.api` |
| `qqq-middleware-health` | `com.kingsrook.qqq.middleware.health` |
| `qqq-middleware-picocli` | `com.kingsrook.qqq.frontend.picocli` |
| `qqq-middleware-lambda` | `com.kingsrook.qqq.lambda` |
| `qqq-middleware-slack` | `com.kingsrook.qqq.slack` |
| `qqq-openapi` | `com.kingsrook.qqq.openapi` |
| `qqq-language-support-javascript` | `com.kingsrook.qqq.languages.javascript` |
| `qqq-utility-lambdas` | `com.kingsrook.qqq.utilitylambdas` |
| `qqq-sample-project` | `com.kingsrook.sampleapp` *(note: not under `qqq.`)* |
| `qqq-dev-tools` | `com.kingsrook.qqq.devtools` |

**Convention inconsistencies to note** (worth flagging for the 0.42→4.0 cleanup):
- `qqq-middleware-javalin` has TWO top-level packages (`backend.javalin` for older classes like `QJavalinImplementation`, and `middleware.javalin` for newer versioned code under `specs/`, `executors/`, `routeproviders/`).
- `qqq-middleware-picocli` uses `.frontend.picocli` rather than `.middleware.picocli` (pre-dates the "middleware" naming).
- `qqq-middleware-slack` uses `.slack` (short form, no `middleware.` prefix).
- `qqq-middleware-lambda` uses `.lambda` (short form).
- `qqq-middleware-api` uses `.api` (short form).
- `qqq-sample-project` lives under `com.kingsrook.sampleapp` with no `qqq` segment.

## Per-Module Internal Structure

### `qqq-backend-core` — `com.kingsrook.qqq.backend.core`
The largest and most important module. 12 top-level packages:

```
qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/
├── actions/                # Concrete operations (the "what QQQ does")
│   ├── tables/             # QueryAction, GetAction, InsertAction, UpdateAction, DeleteAction, CountAction, AggregateAction, ReplaceAction, StorageAction
│   │   └── helpers/        # ActionTimeoutHelper, CacheUtils, UniqueKeyHelper, QueryStatManager, ValidateRecordSecurityLockHelper, ...
│   ├── interfaces/         # SPI: CountInterface, QueryInterface, GetInterface, InsertInterface, UpdateInterface, DeleteInterface, AggregateInterface, QStorageInterface, BaseQueryInterface
│   ├── processes/          # RunProcessAction, RunBackendStepAction, BackendStep, QProcessCallback, QProcessCallbackFactory, CancelProcessAction
│   ├── reporting/          # GenerateReportAction, ExportAction, RecordPipe, CSV/TSV/JSON/Excel/ListOfMaps streamers, FormulaInterpreter, ExportStyleCustomizerInterface
│   ├── automation/         # Record automation handlers + polling
│   ├── customizers/        # Pre/Post customizer abstract classes + TableCustomizers + QCodeLoader
│   ├── async/              # AsyncJobManager, AsyncJobCallback, JobGoingAsyncException, AsyncRecordPipeLoop
│   ├── audits/             # Audit logging
│   ├── dashboard/          # Widget rendering (RenderWidgetAction)
│   ├── messaging/          # Outbound message sending
│   ├── metadata/           # MetaDataAction, TableMetaDataAction, ProcessMetaDataAction, JoinGraph, personalization/
│   ├── permissions/        # PermissionsHelper, PermissionCheckResult, TablePermissionSubType
│   ├── queues/             # SQSQueuePoller, GetQueueSize
│   ├── scripts/            # Script execution
│   ├── templates/          # Template rendering
│   ├── values/             # QValueFormatter, SearchPossibleValueSourceAction
│   ├── AbstractQActionFunction.java
│   ├── AbstractQActionBiConsumer.java
│   ├── ActionHelper.java
│   └── QBackendTransaction.java
├── adapters/               # CsvToQRecordAdapter, JsonToQRecordAdapter, JsonToQFieldMappingAdapter, QRecordToCsvAdapter, QRecordToTsvAdapter, QInstanceAdapter, QQueryFilterJsonAdapter
├── context/                # QContext (thread-locals), CapturedContext
├── exceptions/             # QException + subclasses (QUserFacing, QBadRequest, QNotFound, QPermissionDenied, QAuthentication, QInstanceValidation, QModuleDispatch, ...)
├── instances/              # Application bootstrap
│   ├── AbstractQQQApplication.java, MetaDataProducerBasedQQQApplication.java, ConfigFilesBasedQQQApplication.java, AbstractMetaDataProducerBasedQQQApplication.java
│   ├── QInstanceEnricher.java, QInstanceValidator.java, QInstanceValidationKey.java, QInstanceValidationState.java
│   ├── QMetaDataVariableInterpreter.java, SecretsManagerUtils.java
│   ├── assessment/, enrichment/, validation/
│   └── loaders/            # AbstractMetaDataLoader, MetaDataLoaderHelper, MetaDataLoaderRegistry, implementations/
├── logging/                # QLogger, LogPair, LogUtils, QCollectingLogger, CollectedLogMessage
├── model/                  # POJO-only: metadata + action I/O + records
│   ├── data/               # QRecord, QRecordEntity, @QField, @QAssociation, QIgnore, QVirtualField, QRecordEnum, QRecordWithJoinedRecords
│   ├── metadata/           # QInstance, QTableMetaData, QFieldMetaData, QBackendMetaData, QProcessMetaData, QJoinMetaData, + subpackages:
│   │   ├── tables/, fields/, processes/, joins/, authentication/, security/, permissions/
│   │   ├── qbits/, frontend/, reporting/, scheduleing/, variants/, sharing/, branding/
│   │   ├── audits/, automation/, code/, dashboard/, help/, layout/, menus/, messaging/
│   │   ├── possiblevalues/, producers/, queues/, serialization/
│   │   ├── MetaDataProducerInterface.java, MetaDataProducerHelper.java, MetaDataProducerMultiOutput.java
│   │   └── TopLevelMetaDataInterface.java, QSupplementalInstanceMetaData.java
│   ├── actions/            # Input/Output DTOs (mirrors actions/ packages): tables/, processes/, reporting/, metadata/, scripts/, audits/, messaging/, widgets/, templates/, values/, shared/
│   ├── tables/             # QQQTable, QQQTableTableManager (tables-as-records support)
│   ├── session/            # QSession
│   └── bulk/, scheduledjobs/, audits/, querystats/, backends/, savedviews/, savedreports/, savedbulkloadprofiles/, helpcontent/, dashboard/, common/, scripts/, templates/, automation/, statusmessages/
├── modules/                # SPI dispatchers + in-core implementations
│   ├── backend/            # QBackendModuleInterface, QBackendModuleDispatcher, implementations/ (memory, enumeration, mock)
│   ├── authentication/     # QAuthenticationModuleInterface, QAuthenticationModuleDispatcher, QSessionStoreProviderInterface, QSessionStoreRegistry, implementations/ (Auth0, OAuth2, TableBased, Mock, FullyAnonymous)
│   └── messaging/          # Messaging provider SPI
├── processes/              # Non-action process support
│   ├── implementations/    # Built-in reusable processes: bulk/, sharing/, garbagecollector/, audits/, general/, etl/
│   ├── locks/              # Process-level locking
│   ├── tracing/            # Process execution tracing
│   └── utils/
├── scheduler/              # QScheduleManager, QSchedulerInterface, CronDescriber, simple/, quartz/, processes/, schedulable/
├── state/                  # State provider SPI + implementations
└── utils/                  # Generic Java helpers: StringUtils, CollectionUtils, DateUtils, JsonUtils, ListingHash, MapBuilder, ObjectUtils, ExceptionUtils, ValueUtils, ... + lambdas/, memoization/, aggregates/, collections/
```

### `qqq-backend-module-rdbms` — `com.kingsrook.qqq.backend.module.rdbms`
```
rdbms/
├── RDBMSBackendModule.java               # Implements QBackendModuleInterface
├── actions/                              # AbstractRDBMSAction + RDBMSQueryAction, RDBMSInsertAction, RDBMSUpdateAction, RDBMSDeleteAction, RDBMSCountAction, RDBMSAggregateAction, RDBMSTransaction, StatementTimeoutCanceller
├── jdbc/                                 # Connection/query helpers
├── model/metadata/                       # RDBMSBackendMetaData, field/table-specific metadata
├── fieldfunctions/                       # Database-specific field-level functions
└── strategy/                             # Dialect strategies
```

### `qqq-backend-module-filesystem` — `com.kingsrook.qqq.backend.module.filesystem`
```
filesystem/
├── base/                                  # Shared superclasses (actions, model, utils, model/metadata)
├── local/                                 # FilesystemBackendModule (local FS) + local actions + metadata
├── s3/                                    # S3BackendModule + actions + utils + model/metadata
├── sftp/                                  # SFTPBackendModule + actions + utils + model/metadata
├── processes/implementations/             # Filesystem-specific processes
│   ├── etl/{basic,streamed}
│   └── filesystem/{importer, sync}
└── exceptions/
```

### `qqq-backend-module-mongodb` — `com.kingsrook.qqq.backend.module.mongodb`
```
mongodb/
├── MongoDBBackendModule.java
├── actions/                              # Mongo query/insert/update/delete actions
├── fieldfunctions/
└── model/metadata/
```

### `qqq-backend-module-postgres` — `com.kingsrook.qqq.backend.module.postgres`
Small — extends RDBMS. Has `model/metadata`, `fieldfunctions`, `strategy`.

### `qqq-backend-module-sqlite` — `com.kingsrook.qqq.backend.module.sqlite`
Smallest RDBMS specialization. Has `model/metadata` and `strategy`.

### `qqq-backend-module-api` — `com.kingsrook.qqq.backend.module.api`
```
api/
├── APIBackendModule.java
├── actions/, model/, model/metadata/
├── exceptions/
└── utils/
```

### `qqq-middleware-javalin` — TWO root packages
```
com.kingsrook.qqq.backend.javalin/                       # LEGACY, still primary server
├── QJavalinImplementation.java                          # The big class (>1000 lines)
├── QJavalinMetaData.java
├── QJavalinProcessHandler.java
├── QJavalinScriptsHandler.java
├── QJavalinUtils.java
└── QJavalinAccessLogger.java

com.kingsrook.qqq.middleware.javalin/                    # Newer, versioned surface
├── QApplicationJavalinServer.java                       # Server bootstrap
├── QJavalinRouteProviderInterface.java
├── QMiddlewareApiSpecHandler.java
├── specs/v1/*SpecV1.java                                # AuthenticationMetaDataSpecV1, MetaDataSpecV1, TableQuerySpecV1, ProcessInitSpecV1, ProcessStepSpecV1, ProcessStatusSpecV1, TableCountSpecV1, TableMetaDataSpecV1, ProcessMetaDataSpecV1, ManageSessionSpecV1, LogoutSpecV1, BackChannelLogoutSpecV1, MiddlewareVersionV1
├── executors/                                           # Actual handler logic used by specs
├── routeproviders/                                      # ProcessBasedRouter, SimpleFileSystemDirectoryRouter, IsolatedSpaRouteProvider, SpaNotFoundHandlerRegistry, authentication/, contexthandlers/, handlers/
├── metadata/                                            # Javalin-specific metadata
├── misc/, examples/, tools/                             # PublishAPI, ValidateAPIVersions, codegenerators/
├── schemabuilder/
└── (resources: src/main/resources/openapi/v1/openapi.yaml)
```

### `qqq-middleware-api` — `com.kingsrook.qqq.api`
```
api/
├── actions/, implementations/
├── javalin/                              # Route wiring into Javalin
├── middleware/{specs,executors}/         # Versioned API specs
├── model/{actions,metadata}/
├── utils/
└── ApiSupplementType.java
```

### `qqq-middleware-picocli` — `com.kingsrook.qqq.frontend.picocli`
Flat — only three classes: `QPicoCliImplementation.java`, `QCommandBuilder.java`, `PicoCliProcessCallback.java`.

### `qqq-middleware-lambda` — `com.kingsrook.qqq.lambda`
```
lambda/
├── QAbstractLambdaHandler.java           # Base class
├── QStandardLambdaHandler.java           # Default table/process dispatcher
├── QBaseCustomLambdaHandler.java         # For custom handlers
├── model/                                # QLambdaRequest, QLambdaResponse
└── examples/
```

### `qqq-middleware-slack` — `com.kingsrook.qqq.slack`
Flat — just `QSlackImplementation.java`.

### `qqq-middleware-health` — `com.kingsrook.qqq.middleware.health`
```
health/
├── JavalinHealthRouteProvider.java
├── HealthCheckExecutor.java
├── HealthIndicator.java
├── HealthMetaDataProducer.java
├── indicators/                           # Concrete health indicators
└── model/metadata/
```

### `qqq-openapi` — `com.kingsrook.qqq.openapi.model`
Pure POJOs: `OpenAPI`, `Info`, `Contact`, `Server`, `Tag`, `Path`, `Method`, `Parameter`, `RequestBody`, `Response`, `Content`, `Schema`, `Example*`, `SecurityScheme`, `OAuth2`, `OAuth2Flow`, `Components`, `Property`, `Type`, `Discriminator`, `ExternalDocs`, `HttpMethod`, `In`, `package-info.java`.

### `qqq-language-support-javascript` — `com.kingsrook.qqq.languages.javascript`
Single class: `QJavaScriptExecutor.java`.

### `qqq-sample-project` — `com.kingsrook.sampleapp`
```
sampleapp/
├── SampleJavalinServer.java              # Entry point (has main())
├── SampleCli.java                        # Entry point (has main())
├── ConfigFileBasedSampleJavalinServer.java
├── IsolatedSpaServer.java
├── metadata/                             # SampleMetaDataProvider, SampleJavalinMetaDataProducer, DynamicSiteProcessMetaDataProducer, OAuth2MetaDataProvider, widgetsdashboard/
├── processes/                            # dynamicsite/, clonepeople/
└── dashboard/widgets/
```

### `qqq-utility-lambdas` — `com.kingsrook.qqq.utilitylambdas`
Flat: `QPostToSQSLambda.java`.

### `qqq-dev-tools`
```
qqq-dev-tools/
├── bin/                                  # End-of-sprint release, snapshot deps, jacoco reporter, etc.
├── intellij/, lib/
├── src/main/java/com/kingsrook/qqq/devtools/CreateNewQBit.java
├── MODULE_LIST                           # Consumed by shell scripts
└── CURRENT-SNAPSHOT-VERSION
```

### `qqq-bom` — `qqq-bom-pom` artifact
POM-only module: declares versions for core + rdbms + mongodb + api + filesystem + middleware-{javalin, slack, api, picocli} + openapi + language-support-javascript.

## Key File Locations

**Entry Points (classes with `public static void main`):**
- `qqq-sample-project/src/main/java/com/kingsrook/sampleapp/SampleJavalinServer.java`
- `qqq-sample-project/src/main/java/com/kingsrook/sampleapp/SampleCli.java`
- `qqq-sample-project/src/main/java/com/kingsrook/sampleapp/ConfigFileBasedSampleJavalinServer.java`
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/backend/javalin/QJavalinImplementation.java` (server bootstrap method)
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/tools/PublishAPI.java`
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/tools/ValidateAPIVersions.java`
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/tools/codegenerators/SpecCodeGenerator.java`
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/tools/codegenerators/ExecutorCodeGenerator.java`
- `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/examples/IsolatedSpaExample.java`
- `qqq-middleware-picocli/src/main/java/com/kingsrook/qqq/frontend/picocli/QPicoCliImplementation.java`
- `qqq-dev-tools/src/main/java/com/kingsrook/qqq/devtools/CreateNewQBit.java`

**Core Framework Roots:**
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/QInstance.java` — the root aggregate.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/context/QContext.java` — thread-local state.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceValidator.java` — validator every app must pass.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/AbstractQQQApplication.java` — application bootstrap superclass.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/backend/QBackendModuleInterface.java` — backend SPI.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/backend/QBackendModuleDispatcher.java` — backend registry.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/QAuthenticationModuleInterface.java` — auth SPI.
- `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/data/QRecord.java` — unified record.

**Configuration:**
- `pom.xml` (root) — parent POM, dependency management, build plugins, AGPL license header enforcement, Jacoco coverage gate (80% instruction / 95% class).
- `checkstyle/config.xml` + `checkstyle/license.txt` — enforced at Maven `validate` phase; every `.java` file must start with the exact AGPL header.
- `pmd/ruleset.xml` — PMD rules (runs at `verify`, non-failing by default: `pmd.failOnViolation=false`).
- `spotbugs/exclude-filter.xml` — SpotBugs exclusions (runs at `verify`, non-failing by default: `spotbugs.failOnError=false`).
- `suppression.xml` — root-level suppression file.
- `qodana.yaml` — JetBrains Qodana config.
- `qqq-backend-core/src/main/resources/log4j2.xml` — logging config.
- `qqq-backend-core/src/main/resources/logTemplate.json` — structured-log JSON template.
- `qqq-middleware-javalin/src/main/resources/openapi/v1/openapi.yaml` — public OpenAPI spec.
- `qqq-dev-tools/MODULE_LIST` — module list consumed by release tooling.

**Test Infrastructure:**
- `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/BaseTest.java` — shared test superclass used across modules.
- `qqq-backend-core/src/test/resources/personQInstance.json`, `personQInstanceIncludingBackend.json` — canonical test `QInstance` fixtures.
- `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/testutils/` — test helpers.
- Tests that live in `qqq-middleware-javalin` are additionally published with the `tests` classifier (consumed by `qqq-middleware-api`).

**Static Analysis Output:**
- `spotbugs-summary.csv` (root) — snapshotted SpotBugs output committed to the repo.

## Naming Conventions

**Files:**
- One public class per `.java` file, file name = public class name.
- Every `.java` file begins with the AGPL-3.0 header from `checkstyle/license.txt` (enforced).
- Javadoc banner comments use a box pattern: `/******** … ********/` — this is the house style visible throughout `qqq-backend-core`.

**Classes:**
- **`Q` prefix** on framework types (domain-root classes): `QInstance`, `QTableMetaData`, `QFieldMetaData`, `QRecord`, `QRecordEntity`, `QSession`, `QException`, `QBackendModuleInterface`, `QBackendTransaction`, `QContext`, `QLogger`, `QFieldType`, `QJoinMetaData`, etc. This prefix distinguishes framework types from third-party types.
- **`MetaData` suffix** for metadata classes: `QTableMetaData`, `QProcessMetaData`, `QFieldMetaData`, `QBackendMetaData`, `QJavalinMetaData`, etc.
- **`Interface` suffix** for SPI interfaces: `QBackendModuleInterface`, `QueryInterface`, `InsertInterface`, `MetaDataProducerInterface`, `TableCustomizerInterface`, `QSchedulerInterface`, `QAuthenticationModuleInterface`.
- **`Abstract` prefix** for partial-implementation base classes: `AbstractQQQApplication`, `AbstractQActionFunction`, `AbstractRDBMSAction`, `AbstractPreInsertCustomizer`, `AbstractPostInsertCustomizer`, `AbstractPostUpdateCustomizer`, `AbstractMetaDataLoader`, `AbstractPostQueryCustomizer`, `AbstractPreDeleteCustomizer`, `AbstractPostDeleteCustomizer`.
- **`Action` suffix** for action classes: `QueryAction`, `InsertAction`, `UpdateAction`, `DeleteAction`, `RunProcessAction`, `ExportAction`, `GenerateReportAction`, `MetaDataAction`, `CountAction`, `AggregateAction`, `GetAction`.
- **`Input` / `Output` suffixes** paired for every action: `QueryInput`/`QueryOutput`, `InsertInput`/`InsertOutput`, etc., living under `model/actions/.../` mirroring the action package structure.
- **`Dispatcher` suffix** for SPI registry/factory: `QBackendModuleDispatcher`, `QAuthenticationModuleDispatcher`.
- **`Helper` suffix** for static utility holders: `ActionHelper`, `PermissionsHelper`, `UniqueKeyHelper`, `MetaDataProducerHelper`, `OldRecordHelper`, `QInstanceHelpContentManager`.
- **`*Exception` suffix** for exception types, rooted at `QException` / `QRuntimeException`.
- **`Q*Module` / `*BackendModule` pattern** for concrete `QBackendModuleInterface` implementations: `RDBMSBackendModule`, `MongoDBBackendModule`, `APIBackendModule`, `S3BackendModule`, `SFTPBackendModule`, `FilesystemBackendModule`, `MemoryBackendModule`, `EnumerationBackendModule`, `MockBackendModule`.
- **`V1` suffix** on versioned API specs: `TableQuerySpecV1`, `ProcessInitSpecV1`, etc. (under `middleware/javalin/specs/v1/`).

**Packages:**
- all-lowercase, no underscores or camelCase (e.g., `possiblevalues`, `scheduledjobs`, `savedviews`, `querystats` — not `possible_values` or `possibleValues`).
- Minor typo preserved in the tree: `scheduleing/` (not `scheduling/`) — present at `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/scheduleing/`. Candidate cleanup item.
- Parallel structure between `actions/<kind>/` and `model/actions/<kind>/` — one holds executors, the other holds DTOs.

**Variables / methods:** standard Java camelCase. See `CODE_STYLE.md` for formatting specifics (IntelliJ-style braces, spaces, etc., enforced by checkstyle).

## Where to Find Tests

- **Location:** Co-located with source per module — `<module>/src/test/java/<same-package-path>/`.
- **Naming:** `<ClassName>Test.java` for class-level tests; occasionally `<Feature>IT.java` for integration-style tests.
- **Shared base class:** `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/BaseTest.java` — handles `QContext` setup/teardown, loads a canonical `QInstance` for tests.
- **Test-only `QInstance` fixtures:** `qqq-backend-core/src/test/resources/personQInstance.json` and `personQInstanceIncludingBackend.json`.
- **Test helpers:** `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/testutils/`.
- **Cross-module test reuse:** `qqq-middleware-javalin` publishes a `tests` classifier JAR, consumed by `qqq-middleware-api` for shared test scaffolding.
- **Framework:** JUnit 5 (Jupiter 6.0.2) + AssertJ 3.27.6 (declared in root POM `dependencyManagement`).
- **Coverage:** Jacoco with gates `instructionCoveredRatioMinimum=0.80` and `classCoveredRatioMinimum=0.95` — enforced at `verify` phase; override with `-Dcoverage.haltOnFailure=false`.

## Where to Find Resources & Config

**Main resources per module:** `<module>/src/main/resources/` (present where needed, e.g., `qqq-backend-core` for log4j2.xml and log template; `qqq-middleware-javalin` for openapi.yaml).

**Test resources:** `<module>/src/test/resources/` — JSON fixtures, SQL seed files, H2/SQLite files, etc.

**Build resources:** From root `pom.xml`, `src/main/java` is registered as a resource root (`<directory>src/main/java</directory>`) — this means non-`.java` files living alongside source are also copied into the JAR. This is unusual and worth noting.

## Where to Add New Code (contributor quick-reference)

**Adding a new table/process (consumer app code):** Write a `MetaDataProducerInterface<QTableMetaData>` or `MetaDataProducerInterface<QProcessMetaData>` in your app; make sure it's on the classpath when you call `MetaDataProducerHelper.findProducers(...)`. See `qqq-sample-project/src/main/java/com/kingsrook/sampleapp/metadata/SampleMetaDataProvider.java` for a reference.

**Adding a new backend type (framework code):**
- Create `qqq-backend-module-<name>/` with parent POM = `qqq-parent-project` and dependency on `qqq-backend-core`.
- Java root: `com.kingsrook.qqq.backend.module.<name>`.
- Implement `QBackendModuleInterface` at the module root: `<Name>BackendModule.java`.
- Actions under `<name>/actions/`, metadata POJOs under `<name>/model/metadata/`.
- Register the module in `qqq-bom/pom.xml` and add it to root `pom.xml` `<modules>`.

**Adding a new middleware (framework code):**
- Create `qqq-middleware-<name>/` with dependency on `qqq-backend-core` (and probably `qqq-middleware-javalin` if HTTP-based).
- Java root: follow the existing short-prefix convention (`com.kingsrook.qqq.<name>`) or the newer `com.kingsrook.qqq.middleware.<name>` convention (preferred for new code per the `qqq-middleware-health` and newer `qqq-middleware-javalin` package layout).
- Entry point: `Q<Name>Implementation.java` at module root.
- Register in BOM and root POM.

**Adding a new action:** Put the action class in `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/<kind>/<NameAction>.java` and its `Input`/`Output` DTOs in the mirrored `model/actions/<kind>/` package.

**Adding a new customizer type:** New `Abstract<When><Operation>Customizer` class under `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/customizers/`; register in `TableCustomizers` enum.

**Adding a new built-in process:** `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/processes/implementations/<group>/`.

**Adding a new utility:** `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/utils/` for generic helpers. Prefer augmenting existing `StringUtils`/`CollectionUtils`/`ValueUtils`/`JsonUtils` classes over creating new ones.

**Adding a new exception type:** `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/Q<Name>Exception.java` extending `QException` (checked) or `QRuntimeException` (unchecked).

**Writing tests:** Extend `BaseTest` from `qqq-backend-core`; use `MemoryBackendModule` for storage when possible to avoid external dependencies.

## Special Directories

**`docs/`** — Asciidoc project documentation (actions, implementations, metaData, utilities, misc). Source-controlled; not generated. Has a `justfile` for local builds.

**`qqq-sample-project/`** — Reference implementation. Committed, not published to Maven Central (no `<distributionManagement>` intention). Depends on `qqq-frontend-material-dashboard` version `0.24.0` (external repo).

**`qqq-dev-tools/`** — Maintainer tooling. Does **not** follow the `${revision}` versioning scheme (hard-coded `1.0.0-SNAPSHOT`). Contains release scripts, snapshot update scripts, a Jacoco summary reporter, and `CreateNewQBit.java` for scaffolding new QBit modules. Not published.

**`qqq-utility-lambdas/`** — Standalone AWS Lambdas (currently only `QPostToSQSLambda`). Typically built as a shaded JAR.

**`.circleci/`, `.github/`** — CI config (CircleCI is the primary pipeline based on the presence of `qqq-dev-tools/bin/xbar-circleci-latest.sh`).

**`spotbugs-summary.csv`** — A pre-computed SpotBugs report committed at the root. Presence suggests the team tracks SpotBugs findings as a working list rather than a gate (matches `spotbugs.failOnError=false`).

---

*Structure analysis: 2026-04-22*
