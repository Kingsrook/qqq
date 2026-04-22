# Codebase Concerns

**Analysis Date:** 2026-04-22
**Current Version:** `0.42.0-SNAPSHOT` (Maven `${revision}` property in root `pom.xml`)
**Context:** 4+ years in production, 15+ modules, ~412k lines of Java across 5,973 source files. Milestone is a cleanup pass leading to v4.0 release that formally recognizes 0.x maturity.

## Release / Version Mechanics

### License Inconsistency (CRITICAL BLOCKER for v4.0)
- **What:** The project migrated from AGPL-3.0 to Apache-2.0 (per `CHANGELOG.md` line 35, 0.40.0 entry), and `LICENSE` is Apache-2.0, but 5,973 Java source files still carry AGPL license headers, the checkstyle header template (`checkstyle/license.txt`) enforces the AGPL text, and root `pom.xml` still declares `<name>GNU Affero General Public License v3.0</name>` (line 38) with an AGPL URL in `<licenses>`.
- **Where:** `pom.xml:7-19,37-43`, `checkstyle/license.txt`, every `.java` file under `qqq-*/src/**/*.java` (5973 files), `NOTICE`
- **Impact:** Shipping v4.0 artifacts to Maven Central with mixed Apache-2.0 POM metadata and AGPL source headers is a legal/compliance inconsistency. Checkstyle will reject new files with Apache headers; dev workflow actively reinforces AGPL.
- **Suggested phase type:** **cleanup** (high priority; must land before v4.0 tag).

### Gitflow `developmentBranch` Mismatch
- **What:** `pom.xml:374` configures `gitflow-maven-plugin` with `<developmentBranch>dev</developmentBranch>`, but the actual long-lived branch is `develop` (per `git branch -r` and `.circleci/config.yml:36` `only: [develop]`).
- **Where:** `pom.xml:367-385`
- **Impact:** `mvn gitflow:release-start` / `release-finish` invocations from `qqq-dev-tools/bin/end-of-sprint-release.sh` may operate on the wrong branch or silently fail; the release script also references `git checkout main && git pull && git checkout dev` (line 48) which does not match reality.
- **Severity:** HIGH
- **Suggested phase type:** **cleanup**

### Stale Version Claims in Documentation
- **What:** `README.md:8` claims `Latest Release: v0.35.0 | Development: v0.36.0-SNAPSHOT` — reality is `0.42.0-SNAPSHOT`. `README.md:36-49` shows `<version>0.35.0</version>` / `0.36.0-SNAPSHOT` in example XML. `SECURITY.md:7-9` lists supported versions as `0.36.x` / `0.35.x` and `< 0.35` as EOL. `qqq-dev-tools/CURRENT-SNAPSHOT-VERSION` contains `0.26.0`.
- **Where:** `README.md:8,36,43,49`, `SECURITY.md:7-9`, `qqq-dev-tools/CURRENT-SNAPSHOT-VERSION`
- **Severity:** HIGH (public-facing; misleads integrators)
- **Suggested phase type:** **cleanup**

### Inconsistent GitHub Org in Docs
- **What:** Repository URLs disagree. `pom.xml:35,44-46` and `checkstyle/license.txt` point to `github.com/Kingsrook/qqq`. `SECURITY.md:20,48` points to `github.com/QRun-IO/qqq`. `CONTRIBUTING.md` uses `github.com/Kingsrook/qqq`. `README.md:98,112,117,121` uses `github.com/QRun-IO/qqq`.
- **Severity:** MEDIUM
- **Suggested phase type:** **cleanup**

### v4.0 Rename — No Code Blockers Detected
- **What:** No runtime version parsing that would break on `0.x` → `4.0.0`. The `getVersion()` abstractions in `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/specs/AbstractMiddlewareVersion.java:68` and `QMiddlewareApiSpecHandler.java` concern the middleware API surface version (`v1`, `v2`), not the project's Maven version. Examples referencing `"0.23.0"` exist in `qqq-middleware-javalin/src/main/resources/openapi/v1/openapi.yaml:1767` and `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/specs/v1/MetaDataSpecV1.java:107` as OpenAPI spec examples only.
- **Where:** N/A (positive finding — safe to rename)
- **Severity:** LOW
- **Suggested phase type:** **defer** (no action needed; note in release plan)

---

## Static Analysis (SpotBugs via findsecbugs)

Source: `spotbugs-summary.csv` — 61 distinct bug patterns, ~942 total findings. SpotBugs is configured report-only (`pom.xml:99` `spotbugs.failOnError=false`).

### High-Severity Concurrency and Security Findings
| Pattern | Count | Severity | Notes |
|---|---|---|---|
| `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` | 23 | High | Instance methods mutate static fields — race-condition risk |
| `SQL_INJECTION_JDBC` | 13 | High | Non-constant SQL strings in JDBC — review each; likely mostly false positives in `qqq-backend-module-rdbms` but must be audited |
| `SING_SINGLETON_GETTER_NOT_SYNCHRONIZED` | 12 | Medium→High | Lazy singleton init unsafe under load |
| `COMMAND_INJECTION` | 3 | High | User input reaches `Runtime.exec` or `ProcessBuilder` — review & use argument lists |
| `HARD_CODE_PASSWORD` | 1 | High | Password literal in source — [redacted location, must locate and remove] |
| `UNSAFE_HASH_EQUALS` | 1 | High | Non-constant-time hash comparison — timing attack risk |
| `SCRIPT_ENGINE_INJECTION` | 1 | High | Likely in `qqq-language-support-javascript` (Nashorn eval of user input) |
| `SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING` | 1 | High | PreparedStatement SQL built from variable |
| `EQ_ALWAYS_FALSE` | 2 | High | Broken `equals()` implementations |
| `BC_IMPOSSIBLE_INSTANCEOF` | 1 | High | Dead code / type bug |

- **Where:** `spotbugs-summary.csv` (lines 7, 12, 13, 28, 41, 46, 48, 50, 57, 61). Individual findings require a full SpotBugs run to locate — no per-file XML in repo.
- **Impact:** `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` + `SING_SINGLETON_GETTER_NOT_SYNCHRONIZED` combined (35 sites) are real concurrency hazards in a framework that runs concurrent jobs through `ActionHelper` / `AsyncJobManager` / `AuditHandlerExecutor` thread pools (500 max threads each).
- **Severity:** HIGH (the 4 High-category security patterns), MEDIUM (concurrency)
- **Suggested phase type:** **refactor** (split into security sub-phase + concurrency sub-phase)

### Medium-Severity Bulk Findings (Biggest Offenders)
| Pattern | Count | Category |
|---|---|---|
| `EI_EXPOSE_REP` | 554 | Code Quality (fluent builder intentional — partially excluded in `spotbugs/exclude-filter.xml:27-42`) |
| `CT_CONSTRUCTOR_THROW` | 52 | Code Quality (finalizer-attack vector; low practical risk) |
| `SE_BAD_FIELD` | 45 | Serialization (QQQ rarely serializes) |
| `REC_CATCH_EXCEPTION` | 36 | Catching generic `Exception` — 659 `catch (Exception …)` sites exist |
| `PATH_TRAVERSAL_IN` | 33 | Security — file paths from user input (`qqq-backend-module-filesystem`) |
| `DM_DEFAULT_ENCODING` | 22 | Platform-default charset — should use `StandardCharsets.UTF_8` |
| `WMI_WRONG_MAP_ITERATOR` | 14 | Iterates `Map.keySet()` then `.get()` — 40 sites match this pattern |

- **Where:** Spread across `qqq-backend-core` and `qqq-backend-module-*`. `spotbugs/exclude-filter.xml` already suppresses the `EI_EXPOSE_REP` noise on `*MetaData`/`*Builder`/`with*`; remaining exposures are more genuine.
- **Severity:** MEDIUM (bulk easy-wins) + HIGH for the 33 `PATH_TRAVERSAL_IN` in the filesystem module
- **Suggested phase type:** **cleanup** for charset/map-iterator/catch-Exception; **refactor** for path traversal audit

### SpotBugs Report-Only Posture
- **What:** `pom.xml:99,500` sets `spotbugs.failOnError=false`. PMD is similarly `pmd.failOnViolation=false`. Findings are visible but build stays green, so debt does not regress noisily but also does not decrease without deliberate effort.
- **Severity:** MEDIUM (policy concern)
- **Suggested phase type:** **defer** until after the v4.0 cleanup pass, then flip `failOnError=true` for new code (via `<excludeFilterFile>` incremental baseline).

---

## Tech Debt Signals

### TODO Comments (Baseline)
- **What:** 51 `TODO` occurrences across 31 files. No `FIXME`, `HACK`, or genuine `XXX` markers. Density is low for a 4-year 412k-LOC codebase.
- **Top offending files:**
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/tools/codegenerators/SpecCodeGenerator.java` — 19 TODOs (intentional — this is a code-generator template outputting stub code for humans to fill in; **not real debt**)
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/actions/tables/query/QQueryFilter.java` — 3 (all Javadoc references to `${input.XXX}` variable syntax, not debt)
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/processes/implementations/columnstats/ColumnStatsTableConfig.java` — 2
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/dashboard/widgets/blocks/icon/IconStyles.java` — 2
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceValidator.java` — 2 (line 147: "let the instance …"; line 194: "possible point of customization")
- **Severity:** LOW
- **Suggested phase type:** **defer**

### `@Deprecated` Debt (Significant)
- **What:** 70 `@Deprecated` annotations across 20 files. Most document a real migration path via `since = "…"`. Nothing is being actively removed.
- **Top offenders:**
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/QBackendMetaData.java` — **22 deprecations** all related to `backendVariantsConfig` migration (`variantTypeField`, `variantTypeValue`, `...FieldNameMap`, `...TableName`, etc.)
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/branding/QBrandingMetaData.java` — 8 ("migrate to use banners map instead")
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/QInstance.java` — 5 ("migrated to metaDataCustomizer", "Use registerAuthenticationProvider(AuthScope.instanceDefault(), …)")
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/processes/QProcessMetaData.java` — 3 ("withStep was added")
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/automation/RecordAutomationHandler.java:28` — "0.26.0 — when RecordAutomationHandlerInterface was introduced" (deprecated for 16+ minor versions / ~2 years)
- **Impact:** These are ideal removal candidates for a v4.0 major release — the "semver break" exists precisely to delete long-deprecated APIs.
- **Severity:** MEDIUM
- **Suggested phase type:** **deprecation** (consolidate into a "v4.0 API surface cleanup" phase)

### Package Naming Inconsistency (Architectural)
- **What:** Middleware modules do not share a common root package. 4 of 8 middleware modules use `com.kingsrook.qqq.middleware.*` (javalin, health) while 4 others use divergent roots:
  - `qqq-middleware-api` → `com.kingsrook.qqq.api.*`
  - `qqq-middleware-slack` → `com.kingsrook.qqq.slack.*`
  - `qqq-middleware-lambda` → `com.kingsrook.qqq.lambda.*`
  - `qqq-middleware-picocli` → `com.kingsrook.qqq.frontend.picocli.*` (and `frontend` is misleading — picocli is a CLI, not web UI)
- **Additional:** `qqq-middleware-javalin` has a package split — both `com.kingsrook.qqq.backend.javalin.*` (older; contains `QJavalinImplementation.java`, 2147 lines) and `com.kingsrook.qqq.middleware.javalin.*` (newer) live in the same module.
- **Where:** See `find qqq-*/src/main/java -type d` for package tree. Representative files:
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/backend/javalin/QJavalinImplementation.java`
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/QApplicationJavalinServer.java`
  - `qqq-middleware-api/src/main/java/com/kingsrook/qqq/api/*` (should be `middleware.api.*`)
- **Impact:** A v4.0 major is the one moment to fix this without a patch-version break. Deferring means the inconsistency locks in for another major cycle.
- **Severity:** MEDIUM (cosmetic, but shapes long-term API surface)
- **Suggested phase type:** **refactor** (defer to after v4.0 if scope is too large — rename is mechanical but touches every importer; may warrant `@Deprecated` type-alias phase first)

### Stale `MODULE_LIST` / Dev Tools
- **What:** `qqq-dev-tools/MODULE_LIST` enumerates 13 modules but the root `pom.xml:62-78` lists 17 modules plus `qqq-bom`. Missing from `MODULE_LIST`: `qqq-backend-module-sqlite`, `qqq-backend-module-postgres`, `qqq-middleware-lambda`, `qqq-middleware-health`, `qqq-openapi`, `qqq-utility-lambdas`. Also lists `qqq-frontend-material-dashboard` (not in this repo) and `qqq-bom-pom` (misspelled).
- **Where:** `qqq-dev-tools/MODULE_LIST`
- **Severity:** LOW (tooling)
- **Suggested phase type:** **cleanup**

### Large Monolithic Files
- **What:** Five production-code files exceed 1,500 lines, suggesting responsibility overload:
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceValidator.java` — 2,904 lines
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/backend/javalin/QJavalinImplementation.java` — 2,147 lines
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/QInstance.java` — 2,084 lines
  - `qqq-middleware-api/src/main/java/com/kingsrook/qqq/api/actions/GenerateOpenApiSpecAction.java` — 2,007 lines
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/tables/QTableMetaData.java` — 2,004 lines
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceEnricher.java` — 1,990 lines
  - `qqq-backend-module-api/src/main/java/com/kingsrook/qqq/backend/module/api/actions/BaseAPIActionUtil.java` — 1,692 lines
  - `qqq-middleware-api/src/main/java/com/kingsrook/qqq/api/actions/ApiImplementation.java` — 1,595 lines
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/actions/tables/query/JoinsContext.java` — 1,553 lines
- **Impact:** High blast radius for any change; harder reviews; encourages scroll-and-append rather than extract.
- **Severity:** MEDIUM
- **Suggested phase type:** **refactor** (defer — low-urgency, high-risk; do after the v4.0 stability gate)

---

## Fragile / Error-Handling Concerns

### Raw `printStackTrace()` in Production Paths
- **What:** 15 `printStackTrace()` calls; 9 sit in non-test production code — notably inside the framework's own logging layer.
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/logging/QLogger.java:78,99` — logger fails silently with `e.printStackTrace()`
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/logging/LogUtils.java:206`
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceValidator.java:1936`
  - `qqq-middleware-picocli/src/main/java/com/kingsrook/qqq/frontend/picocli/QPicoCliImplementation.java:246,474`
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/routeproviders/SimpleFileSystemDirectoryRouter.java:119`
  - (Code generators and dev-tools calls are acceptable.)
- **Impact:** Stack traces go to stderr bypassing log4j — lost in containerized deployments that only capture stdout, and not structured.
- **Severity:** MEDIUM
- **Suggested phase type:** **cleanup**

### Silently Swallowed Exceptions
- **What:** 6 empty/ignored `catch` blocks in production code.
- **Severity:** MEDIUM
- **Suggested phase type:** **cleanup** (requires per-site judgment; most are "logged deliberately ignored" but must be made explicit)

### 659 Generic `catch (Exception …)` Sites
- **What:** SpotBugs flagged 36 as `REC_CATCH_EXCEPTION`; total incidence is 659 (plus 5 `catch (Throwable)`). Combined with `Exception` being the canonical top of the framework's `QException` hierarchy, many catches are deliberate. But a 659 count warrants an audit pass.
- **Severity:** MEDIUM
- **Suggested phase type:** **defer** (tackle incrementally as part of module-by-module refactors)

### `System.out` in Non-Test Production Code (156 occurrences)
- **What:** After excluding tests and tools/codegen:
  - `qqq-sample-project/src/main/java/com/kingsrook/sampleapp/IsolatedSpaServer.java` — 85 (sample/demo, acceptable)
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/actions/tables/query/JoinsContext.java` — 6 (debug prints in 1553-line file)
  - `qqq-middleware-javalin/src/main/java/com/kingsrook/qqq/middleware/javalin/examples/IsolatedSpaExample.java` — 5 (example, acceptable)
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/scripts/logging/SystemOutExecutionLogger.java` — 4 (intentional — named "SystemOut")
  - `qqq-backend-module-rdbms/src/main/java/com/kingsrook/qqq/backend/module/rdbms/actions/AbstractRDBMSAction.java` — 3 (debug prints in hot path)
  - `qqq-backend-module-mongodb/src/main/java/com/kingsrook/qqq/backend/module/mongodb/actions/AbstractMongoDBAction.java` — 2
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/messaging/email/SendEmailAction.java` — 2
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/QInstanceValidator.java` — 2
- **Severity:** LOW (mostly examples); MEDIUM for `JoinsContext`, `AbstractRDBMSAction`, `AbstractMongoDBAction` (core paths)
- **Suggested phase type:** **cleanup**

### 190 `@SuppressWarnings` Annotations
- **What:** Concentration worth reviewing:
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/dashboard/widgets/blocks/AbstractBlockWidgetData.java` — 14
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/reporting/GenerateReportAction.java` — 10
  - `qqq-backend-module-rdbms/src/main/java/com/kingsrook/qqq/backend/module/rdbms/jdbc/QueryManager.java` — 9
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/instances/loaders/AbstractMetaDataLoader.java` — 6
- **Severity:** LOW (informational — most suppress generic-array or unchecked-cast, legitimate)
- **Suggested phase type:** **defer**

---

## Security Surface

### `HARD_CODE_PASSWORD` (SpotBugs, 1 finding)
- **What:** SpotBugs detected one literal password in source. Location [redacted — requires running SpotBugs report to pinpoint; categorized as High severity].
- **Impact:** CRITICAL if it is a real credential; LOW if it is a default/dev seed.
- **Severity:** CRITICAL (pending location & classification)
- **Suggested phase type:** **cleanup** (investigate first — if real, treat as incident; if dev default, move to config with a clear `CHANGE-ME` marker)

### Path Traversal Surface (33 findings)
- **What:** 33 `PATH_TRAVERSAL_IN` findings; almost all will be in `qqq-backend-module-filesystem` (local / S3 file operations driven by metadata-declared paths, some of which reach user input via process inputs or file-upload flows). 2 `PATH_TRAVERSAL_OUT` additionally.
- **Where:** `qqq-backend-module-filesystem/src/main/java/com/kingsrook/qqq/backend/module/filesystem/**`, including `S3Utils.java` and `BasicETLCollectSourceFileNamesStep.java` (both have TODOs).
- **Severity:** HIGH
- **Suggested phase type:** **refactor** (per-site audit; adopt a `Path.normalize() + prefix-allowlist` helper)

### `SQL_INJECTION_JDBC` (13 findings)
- **What:** 13 sites where JDBC queries are built from non-constant strings. Most are likely QQQ's dynamic-column / dynamic-order-by pattern which is safe in practice (column names from metadata, not user input) but needs per-site proof.
- **Where:** `qqq-backend-module-rdbms/src/main/java/com/kingsrook/qqq/backend/module/rdbms/actions/*Action.java`, `qqq-backend-module-rdbms/src/main/java/com/kingsrook/qqq/backend/module/rdbms/jdbc/QueryManager.java` (1337 lines, 9 `@SuppressWarnings`)
- **Severity:** HIGH (needs audit regardless of likely-false-positive classification)
- **Suggested phase type:** **refactor**

### Script Engine Injection (Nashorn)
- **What:** `SCRIPT_ENGINE_INJECTION` (1 finding) + `qqq-language-support-javascript` embeds Nashorn 15.7 (`qqq-language-support-javascript/pom.xml`) to execute user-supplied JavaScript in scripts / process customizers.
- **Where:** `qqq-language-support-javascript/src/main/java/com/kingsrook/qqq/languages/javascript/**`; called via `qqq-backend-core` script execution framework.
- **Impact:** Any tenant who can author a script has code-execution inside the JVM context (by design — but review sandboxing, class allowlist, thread limits).
- **Severity:** HIGH (by design; document threat model explicitly for v4.0)
- **Suggested phase type:** **refactor** or **defer** (document-only) — decide after threat model review.

### Command Injection (3 findings)
- **What:** `COMMAND_INJECTION` — user input reaches `Runtime.exec` / `ProcessBuilder`.
- **Severity:** HIGH
- **Suggested phase type:** **cleanup**

### `FullyAnonymousAuthenticationModule` Exists in Core
- **What:** `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/modules/authentication/implementations/FullyAnonymousAuthenticationModule.java` — an unauthenticated authentication module is shipped with the framework. Confirmed legitimate for dev/test but must be documented and guarded in production starters.
- **Severity:** LOW (legitimate — flagged so that v4.0 docs explicitly warn against production use)
- **Suggested phase type:** **defer** (documentation only)

### `.env` Files
- **What:** No `.env` files present in working tree (verified by `find … -name '.env*'`). `qqq-dev-tools/bin/end-of-sprint-release.sh:50` conditionally creates one for `qqq-sample-project` via `setup-environments.sh --is-for-release`.
- **Severity:** LOW (healthy — no committed secrets discovered)
- **Suggested phase type:** **defer**

---

## Performance / Scalability Concerns

### Thread Pools: 500 Max Threads × 3 Pools
- **What:** `qqq-backend-core` declares three separate `ThreadPoolExecutor`s with `maxPoolSize=500` each and a `SynchronousQueue` (no queueing, direct hand-off):
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/ActionHelper.java:69`
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/async/AsyncJobManager.java:69`
  - `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/audits/AuditHandlerExecutor.java:55`
- **Impact:** A single server can spawn up to 1,500 concurrent worker threads across these pools, each with a full JVM stack (~1 MB default). Under load bursts, memory pressure spikes; tasks also cannot enqueue (SynchronousQueue rejects if no thread free) so callers see `RejectedExecutionException` once saturated rather than graceful queuing.
- **Severity:** MEDIUM (configuration only — but currently hard-coded)
- **Suggested phase type:** **refactor** (make configurable via instance metadata; unify policy)

### Map-Iterator Anti-Pattern (40 sites)
- **What:** 40 `for (x : map.keySet()) … map.get(x)` sites — double hash lookup on every iteration. SpotBugs flags 14 as `WMI_WRONG_MAP_ITERATOR`.
- **Severity:** LOW
- **Suggested phase type:** **cleanup** (mechanical; safe IntelliJ "Replace with entrySet()" inspection)

### Default-Encoding String Handling (22 sites)
- **What:** `DM_DEFAULT_ENCODING` — `new String(bytes)` without charset, etc. Differs between CI Linux UTF-8 and developer macOS UTF-8 (same today, but platform-fragile).
- **Severity:** LOW
- **Suggested phase type:** **cleanup**

---

## Dependency Hygiene

### Modern Core Dependencies (Healthy)
Root `pom.xml` and `qqq-backend-core/pom.xml` pin:
- Jackson 2.21.0, log4j 2.25.3, commons-lang3 3.20.0, commons-io 2.21.0, JUnit Jupiter 6.0.2, Mockito (via CHANGELOG) 5.21.0, protobuf-java 4.33.3, Javalin 6.7.0.
- Java 21 LTS target (`pom.xml:87` `maven.compiler.release=21`).
- mysql-connector-j 8.4.0 (migrated from deprecated mysql:mysql-connector-java per CHANGELOG 0.40.0).

All are current. No 0.x-era / EOL core libraries detected. Dependabot is active (per `.github/dependabot.yml` and open `dependabot/maven/*` branches).

### Jetty 11 — End of Life
- **What:** `pom.xml:83` pins `jetty.version=11.0.26`. Eclipse Jetty 11 reached end-of-community-support in 2024. `CHANGELOG.md:50` notes that jetty-http CVEs are "dismissed — requires Jetty 12.x (Javalin 7.x)" — i.e., the upgrade is blocked by Javalin 6.x.
- **Where:** `pom.xml:83,186-201`, `qqq-middleware-javalin/pom.xml` (Javalin 6.7.0)
- **Impact:** Future Jetty 11 CVEs will not receive upstream fixes. Javalin 7.x upgrade is a known-pending breaking change — a logical candidate for a v4.0 or v4.x release.
- **Severity:** HIGH
- **Suggested phase type:** **upgrade** (Javalin 6→7 + Jetty 11→12; substantial — scope as a dedicated phase)

### Nashorn 15.7 (Community Fork)
- **What:** Nashorn was removed from the JDK in Java 15. `qqq-language-support-javascript` depends on the OpenJDK community fork at `org.openjdk.nashorn:nashorn-core:15.7`. The fork is maintained but intermittently. Combined with the `SCRIPT_ENGINE_INJECTION` finding, this is a concentrated risk.
- **Where:** `qqq-language-support-javascript/pom.xml`
- **Severity:** MEDIUM
- **Suggested phase type:** **defer** (evaluate GraalJS or Rhino as alternatives in a separate feature phase; no immediate action)

### aws-java-sdk-secretsmanager 1.x (Legacy)
- **What:** `qqq-backend-core/pom.xml` includes `com.amazonaws:aws-java-sdk-secretsmanager:1.12.797`. The main AWS SDK elsewhere is v2 (`software.amazonaws:bom:2.41.10`). v1 AWS SDK is deprecated (end-of-maintenance Dec 2025 per AWS).
- **Where:** `qqq-backend-core/pom.xml`
- **Severity:** MEDIUM
- **Suggested phase type:** **upgrade**

### Suppression Template Empty
- **What:** `suppression.xml` (dependency-check suppressions) is template-only (30 lines of examples, no real entries).
- **Severity:** LOW (informational — means dependency-check is not currently silenced anywhere)
- **Suggested phase type:** **defer**

---

## Documentation Decay

### README vs Reality
- **What:** Version claims stale by 6 minor versions (see "Stale Version Claims" above). `README.md:107-108` roadmap ("Improved widget system / Enhanced process tracing") is generic, not tied to current CHANGELOG work (OAuth2 externalBaseUrl, JoinsContext fixes, Field Functions, etc.).
- **Severity:** HIGH
- **Suggested phase type:** **cleanup**

### CHANGELOG.md — Maintained but Sparse Before 0.35.0
- **What:** `CHANGELOG.md` is actively maintained for recent releases (0.40.0 and 0.35.0 have rich entries). However, 0.27.0 / 0.26.1 / 0.26.0 carry placeholder dates `2024-01-XX` and vague entries ("Various bug fixes and improvements"). Gap between 0.27.0 and 0.35.0 (8 minor versions) has **no** entries at all.
- **Where:** `CHANGELOG.md:95-126`
- **Impact:** Anyone doing a v4.0 retrospective / changelog rewrite has no middle-era ground truth.
- **Severity:** MEDIUM
- **Suggested phase type:** **cleanup** (v4.0 release note compile-up; might be acceptable to collapse 0.27–0.34 into a single summary block)

### `SECURITY.md` Stale Supported-Version Table
- **What:** See "Stale Version Claims" above. Lists `0.35.x` as "Latest stable" and `< 0.35` as EOL; reality is `0.41.x` shipped / `0.42.0-SNAPSHOT` in develop.
- **Where:** `SECURITY.md:7-9`
- **Severity:** MEDIUM (security posture mis-advertised)
- **Suggested phase type:** **cleanup**

### Wiki Links
- **What:** `README.md` and `CONTRIBUTING.md` link 19+ times to GitHub wiki pages (`github.com/Kingsrook/qqq/wiki/*` and `github.com/QRun-IO/qqq/wiki/*`). Wiki content is not in this repo — cannot verify staleness from the tree alone. Some wiki page names (e.g., `Code-Review-Standards`, `Release-Flow`, `Compatibility-Matrix`) may be drift candidates.
- **Severity:** LOW (unverifiable from repo)
- **Suggested phase type:** **defer**

### Docs Directory (`docs/`) Coverage Gaps
- **What:** `docs/` is AsciiDoc-based and partial. `docs/actions/` covers only 4 actions (Get, Insert, Query, RenderTemplate) — framework has dozens (Update, Delete, Count, Aggregate, ETL variants, automation, etc.).
- **Severity:** LOW (documentation gap, not decay)
- **Suggested phase type:** **defer**

---

## Test Coverage Gaps (Informational)

- **What:** Jacoco is configured with instruction-coverage minimum `0.80` and class-coverage minimum `0.95` (`pom.xml:89-90`) and `haltOnFailure=true`. That is a strong gate. No obvious coverage gaps detectable statically.
- **Note:** 3 files exceed 2,000 lines of production code; splitting them likely requires test restructuring — the associated test files (e.g., `QInstanceValidatorTest.java` at 3,165 lines) mirror the monolith.
- **Severity:** LOW
- **Suggested phase type:** **defer**

---

## Summary — Suggested v4.0 Cleanup Phase Buckets

The items below are the naturally grouped phases a roadmapper could create from this map. Ordering reflects dependency / risk, not priority.

| # | Phase | Severity | Type |
|---|---|---|---|
| 1 | **License Migration Completion** — update 5973 source headers, `checkstyle/license.txt`, root `pom.xml` `<licenses>` to Apache-2.0 | CRITICAL | cleanup |
| 2 | **Investigate HARD_CODE_PASSWORD SpotBugs finding** — locate & remediate | CRITICAL (pending location) | cleanup |
| 3 | **Version & Docs Sync** — README, SECURITY, CURRENT-SNAPSHOT-VERSION, MODULE_LIST, gitflow `<developmentBranch>` dev→develop, GitHub org consistency | HIGH | cleanup |
| 4 | **High-severity SpotBugs cleanup** — 23 `ST_WRITE_TO_STATIC`, 12 singleton-sync, 3 command-injection, 1 timing-attack hash | HIGH | refactor |
| 5 | **Path-traversal audit** in `qqq-backend-module-filesystem` (33 findings) | HIGH | refactor |
| 6 | **SQL-injection audit** in `qqq-backend-module-rdbms` (13 findings) | HIGH | refactor |
| 7 | **`@Deprecated` API removal** for v4.0 — 70 annotations across 20 files (QBackendMetaData variants, QBrandingMetaData banners, QInstance customizer migrations, RecordAutomationHandler since 0.26.0) | MEDIUM | deprecation |
| 8 | **Logger hygiene** — replace 9 production `printStackTrace()` calls (notably in `QLogger`/`LogUtils` themselves), remove `System.out` from core hot paths (`JoinsContext`, `AbstractRDBMSAction`, `AbstractMongoDBAction`) | MEDIUM | cleanup |
| 9 | **Bulk SpotBugs cleanup** — 22 `DM_DEFAULT_ENCODING`, 40 `keySet()` iterators, 17 dead local stores, 11 `\n` vs `%n` | MEDIUM | cleanup |
| 10 | **Javalin 6→7 + Jetty 11→12 upgrade** | HIGH | upgrade |
| 11 | **AWS SDK v1 → v2 for secretsmanager** | MEDIUM | upgrade |
| 12 | **CHANGELOG backfill** for 0.27.0–0.34.0 gap (or deliberate collapse for v4.0 release notes) | MEDIUM | cleanup |
| 13 | **Package-naming consolidation** — middleware modules under `com.kingsrook.qqq.middleware.*` (slack, lambda, picocli, api, + javalin backend.javalin→middleware.javalin split) | MEDIUM | refactor |
| 14 | **Thread-pool configurability** — three 500-thread hard-coded pools in `ActionHelper`/`AsyncJobManager`/`AuditHandlerExecutor` | MEDIUM | refactor |
| 15 | **Large-file decomposition** — `QInstanceValidator`, `QJavalinImplementation`, `QInstance`, `QTableMetaData`, `QInstanceEnricher`, `GenerateOpenApiSpecAction`, `BaseAPIActionUtil` (all > 1500 LOC) | MEDIUM | refactor (defer past v4.0) |
| 16 | **Flip `spotbugs.failOnError`/`pmd.failOnViolation` to true** once baseline is clean | MEDIUM | cleanup (post-v4.0) |
| 17 | **Nashorn/Script-engine threat model & sandboxing review** | HIGH | refactor or defer |

---

*Concerns audit: 2026-04-22*
