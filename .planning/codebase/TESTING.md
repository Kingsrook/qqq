# Testing Patterns

**Analysis Date:** 2026-04-22

## Test Framework

**Runner:** JUnit **5** (Jupiter) — version `6.0.2` of `junit-jupiter-engine` + `junit-jupiter-params` (`pom.xml:230-241`). No JUnit 4 imports exist anywhere in the tree (grep for `org.junit.Test` as a non-jupiter import returns 0). Surefire is the test executor:

```xml
<!-- pom.xml:275-285 -->
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.5.4</version>
  <configuration>
    <argLine>@{jaCoCoArgLine}</argLine>
    <redirectTestOutputToFile>${testOutputToFile}</redirectTestOutputToFile>
  </configuration>
</plugin>
```

`testOutputToFile=true` by default (`pom.xml:94`) — console output is redirected to `target/surefire-reports/*.txt` to keep CI output readable.

**Assertion library:** Both `org.junit.jupiter.api.Assertions` and **AssertJ 3.27.6** (`pom.xml:242-247`). They are used **side-by-side in the same test class** — see `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/JsonUtilsTest.java:43-51`:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
```

- **Usage stats:** 393 files use JUnit `assertEquals`/`assertTrue`/`assertThrows`; 203 files use AssertJ `assertThat`/`assertThatThrownBy`. AssertJ is preferred for fluent chains (`hasMessageContaining`, `rootCause()`, etc. — see `JsonUtilsTest.java:337`).

**Parameterized tests:** `junit-jupiter-params` is on the classpath but sparingly used — only 8 `@ParameterizedTest` annotations across the entire codebase. Observed providers: `@ValueSource`, `@NullAndEmptySource`.

**Run commands:**

```bash
mvn test                         # unit tests for current module
mvn verify                       # tests + JaCoCo check + SpotBugs + PMD
mvn clean install                # full build including tests
mvn test -pl qqq-backend-core    # single module
mvn test -Dcoverage.haltOnFailure=false  # skip coverage gate
```

CI entry points (CircleCI orb `kingsrook/qqq-orb@0.6.0`, `.circleci/config.yml:6`):
- `qqq-orb/mvn_test_only` — test-only job for feature branches
- `qqq-orb/mvn_publish` — publish + tests for develop/release/main/hotfix branches
- `qqq-orb/static_analysis` — runs in parallel with tests

## Test File Organization

**Location:** co-located with the code under test, mirrored package path. Each module owns its own `src/test/java/<same package>/`.

**Naming:**
- Test classes end in `Test.java` (472 such files project-wide).
- Integration variants end in `IntegrationTest.java` (4 files: `AuditHandlerIntegrationTest`, `OAuth2AuthenticationModuleIntegrationTest`, `MongoDBFieldFunctionIntegrationTest`, `SimpleFileSystemDirectoryRouterIntegrationTest`). **There are no `*IT.java`** files — Maven Failsafe is **not** configured; all tests run under Surefire.
- Selenium tests end in `SeleniumTest.java` (see `qqq-sample-project/src/test/java/com/kingsrook/sampleapp/selenium/`).
- Test method names use snake_case with a `test_` prefix: `test_toJsonNull()`, `test_toJSONObject_malformed()` — permitted because Checkstyle's `MethodName` regex allows underscores (`^[a-z][a-zA-Z0-9_]*$`, `checkstyle/config.xml:227-231`). Some tests also use plain JUnit-style camelCase (`testNullKeyInMap()`). Both styles coexist.

**Test infrastructure per-module:** every major module has its own `BaseTest.java` + `TestUtils.java`:

| Module | BaseTest | TestUtils |
|--------|----------|-----------|
| `qqq-backend-core` | `src/test/java/com/kingsrook/qqq/backend/core/BaseTest.java` | `src/test/java/com/kingsrook/qqq/backend/core/utils/TestUtils.java` |
| `qqq-backend-module-rdbms` | `.../rdbms/BaseTest.java` | `.../rdbms/TestUtils.java` |
| `qqq-backend-module-postgres` | `.../postgres/BaseTest.java` (extends pattern) | `.../postgres/TestUtils.java` |
| `qqq-backend-module-sqlite` | `.../sqlite/BaseTest.java` | `.../sqlite/TestUtils.java` |
| `qqq-backend-module-mongodb` | `.../mongodb/BaseTest.java` | `.../mongodb/TestUtils.java` |
| `qqq-backend-module-filesystem` | `.../filesystem/BaseTest.java` (+ `sftp/BaseSFTPTest.java`) | `.../filesystem/TestUtils.java` |
| `qqq-backend-module-api` | `.../api/BaseTest.java` + `actions/BaseAPIActionUtilTest.java` | — |
| `qqq-middleware-api` | `.../api/BaseTest.java` | `.../api/TestUtils.java` |
| `qqq-middleware-javalin` | `qqq-backend-core` BaseTest reused | `.../javalin/TestUtils.java` |
| `qqq-middleware-picocli` | — | `.../picocli/TestUtils.java` |
| `qqq-language-support-javascript` | — | `.../javascript/TestUtils.java` |

**Directory layout example** (`qqq-backend-core`):

```
qqq-backend-core/src/test/
├── java/com/kingsrook/qqq/backend/core/
│   ├── BaseTest.java                 ← @BeforeEach/@AfterEach: QContext init, MemoryRecordStore reset
│   ├── actions/                      ← parallel to src/main/java/.../actions
│   ├── context/
│   ├── modules/backend/implementations/
│   ├── processes/
│   ├── scheduler/
│   └── utils/
│       ├── TestUtils.java            ← defines test QInstance with sample tables/processes
│       ├── CollectionAssert.java     ← custom AssertJ-style assertion helper
│       ├── JsonUtilsTest.java
│       ├── StringUtilsTest.java
│       └── ...
└── resources/                        ← test YAML, fixtures, certificates, etc.
```

## Test Structure

**Canonical unit-test skeleton** (extends the module's `BaseTest`):

```java
// qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/JsonUtilsTest.java
package com.kingsrook.qqq.backend.core.utils;


import ... ;  // alphabetical, javax first then java.* then 3rd party, static imports last
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for JsonUtils.
 **
 *******************************************************************************/
class JsonUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJsonQRecordInput()
   {
      QRecord qRecord = getQRecord();
      String  json    = JsonUtils.toJson(qRecord);
      assertEquals("""
         {"tableName":"foo","values":{"foo":"Foo","bar":3.14159}}""", json);
   }
```

Key structural features:
- License header + package + blank line + imports + two blank lines before class (checkstyle-enforced).
- Test classes **extend `BaseTest`** (343 files across the tree) to inherit `QContext` setup/teardown.
- Each `@Test` method carries a full Javadoc flower-box (empty is fine) — `MissingJavadocMethod` is enforced even in tests (`checkstyle/config.xml:324` — `includeTestSourceDirectory=true`). `@Test` and `@Override` are on the list of annotations that waive param/return Javadoc tags (`checkstyle/config.xml:216-220`).
- 3 blank lines between methods, same as production code.
- Java 15+ text blocks (`"""..."""`) used liberally for expected-JSON strings.

**Setup / teardown pattern** — `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/BaseTest.java`:

```java
public class BaseTest
{
   private static final QLogger LOG = QLogger.getLogger(BaseTest.class);
   public static final String DEFAULT_USER_ID = "001";

   static
   {
      TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));   // pin timezone for reproducibility
   }

   @BeforeEach
   void baseBeforeEach()
   {
      System.setProperty("qqq.logger.logSessionId.disabled", "true");
      QContext.init(TestUtils.defineInstance(), newSession());
      resetMemoryRecordStore();
      ExamplePersonalizer.reset();
   }

   @AfterEach
   void baseAfterEach()
   {
      QContext.clear();
      resetMemoryRecordStore();
   }
}
```

- **TimeZone pinned to UTC** in a `static {}` block so tests are deterministic across developer machines.
- **`QContext`** (the thread-local request context) is initialized with a fresh `QInstance` from `TestUtils.defineInstance()` and cleared after each test.
- **`MemoryRecordStore`** (in-memory data backend used by most tests) is reset before and after each test — no cross-test state leakage.
- `@BeforeAll` / `@AfterAll` are used sparingly (mainly in Testcontainers-based subclasses where container lifecycle is expensive).

## Mocking

**Minimal and targeted.** Mockito 5.21.0 is declared as a test dep in `qqq-backend-core/pom.xml:304-309`, but only **3** test files actually import `org.mockito.*`:

1. `qqq-middleware-javalin/src/test/java/com/kingsrook/qqq/middleware/javalin/executors/ExecutorSessionUtilsTest.java`
2. `qqq-middleware-javalin/src/test/java/com/kingsrook/qqq/middleware/javalin/routeproviders/SimpleFileSystemDirectoryRouterIntegrationTest.java`
3. (one third file in the tree)

The codebase's preferred alternative to mocks:
- **`MemoryRecordStore`** as a swappable in-memory backend for most data-layer tests.
- **Hand-rolled fakes** named `Mock*`: `MockApiActionUtils.java`, `MockApiUtilsHelper.java`, `MockContext.java`, `MockHttpResponse.java` — these are plain classes, not Mockito mocks.
- **`ExamplePersonalizer.reset()`** — per-test reset pattern for a singleton fake, called from `BaseTest.baseBeforeEach()`.

**PowerMock is not used.** **No bytecode-level mocking.**

**Other "test-double" libraries in use:**
- **WireMock 3.13.2** (`org.wiremock:wiremock`, `qqq-backend-core/pom.xml:312-316`) — used by 1 integration test, `OAuth2AuthenticationModuleIntegrationTest.java`, to stub the OAuth2 provider.
- **Localstack** (`cloud.localstack:localstack-utils:0.2.23`, `qqq-backend-core/pom.xml:229-234`) — wraps LocalStack Docker for AWS SES integration testing: `SendSESActionTest.java` uses `@ExtendWith(LocalstackDockerExtension.class)` + `@LocalstackDockerProperties`.

**SpotBugs skips test classes entirely** (`spotbugs/exclude-filter.xml:12-21`) — test classes ending in `Test`, `Tests`, or `TestUtils?` are excluded from analysis.

## Fixtures and Factories

**`TestUtils.defineInstance()` per module** — each `TestUtils` class builds a "canonical" `QInstance` with sample tables, processes, possible-value-sources, joins, etc. Example from `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/TestUtils.java`: defines tables for `Person`, `Shape`, uses classes like `PersonsByCreateDateBarChart`, `AddAge`, `GetAgeStatistics` as sample process steps.

**Sample entity fixtures** co-located with tests: `PersonQRecord.java`, `Asset.java`, `Client.java`, `Item.java`, `ItemWithPrimitives.java`, `LineItem.java`, `Order.java`, `OrderWithoutTableName.java`, `Group.java` — plain `QRecordEntity` subclasses used as test data shapes.

**Custom AssertJ-style helpers:**
- `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/CollectionAssert.java`
- `qqq-sample-project/.../ProcessSummaryAssert.java`
- `qqq-sample-project/.../ProcessSummaryLineInterfaceAssert.java`
- Pattern: extends an AssertJ base class to add domain-specific fluent assertions.

**Test resources** live in `src/test/resources/` per module — 8 modules have one:
- `qqq-backend-core`, `qqq-middleware-javalin`, `qqq-backend-module-filesystem`, `qqq-backend-module-postgres`, `qqq-backend-module-rdbms`, `qqq-middleware-lambda`, `qqq-backend-module-sqlite`, `qqq-middleware-picocli`.

## Coverage (JaCoCo)

**Tool:** JaCoCo 0.8.14 (`org.jacoco:jacoco-maven-plugin`, `pom.xml:440-491`).

**Gates (parent defaults, `pom.xml:90-92`):**

```xml
<coverage.haltOnFailure>true</coverage.haltOnFailure>
<coverage.instructionCoveredRatioMinimum>0.80</coverage.instructionCoveredRatioMinimum>
<coverage.classCoveredRatioMinimum>0.95</coverage.classCoveredRatioMinimum>
```

- **80% instruction coverage, 95% class coverage** — JaCoCo `check` goal runs in `verify` and **fails the build** on shortfall (`pom.xml:455-481`). This is the strictest gate in the project.
- Override with `-Dcoverage.haltOnFailure=false` for local iteration.
- `CONTRIBUTING.md:33` promises "80% instructions, 95% classes" — matches the actual enforcement.

**Module-level overrides:**

| Module | Instruction | Class | Note |
|--------|-------------|-------|------|
| `qqq-middleware-health/pom.xml` | `0.70` | `0.90` | "Override parent — this module has reasonable coverage" |
| `qqq-middleware-lambda/pom.xml` | `0.10` | `0.10` | Comment says "todo - remove these!!" — known debt |
| `qqq-utility-lambdas/pom.xml` | (custom rule block) | | |

**JaCoCo exclusions** in `qqq-backend-core/pom.xml:337-342`:

```xml
<excludes>
   <exclude>com/kingsrook/qqq/backend/core/model/**/*.class</exclude>
   <exclude>com/kingsrook/qqq/backend/core/exceptions/**/*.class</exclude>
</excludes>
```

Model classes (DTOs with fluent setters) and exception classes are excluded from coverage — they're largely boilerplate generated by the withXxx/getXxx pattern.

**View coverage:** after `mvn verify`, the parent `pom.xml:386-439` defines an `exec-maven-plugin` step that prints a JaCoCo summary to the console and lists untested classes. Full HTML report at `target/site/jacoco/index.html` per module.

## Test Types

**Unit tests** — dominant form. Extend `BaseTest`, use `MemoryRecordStore` as backend, no external I/O. Fast enough that no separation from integration tests is done at build time.

**Integration tests** — use real external systems via Testcontainers (see below) or `@Disabled`-by-default guards. They live **alongside unit tests** and run under the same `mvn test` phase. There is no separate Failsafe phase — 4 `*IntegrationTest.java` files run as normal tests.

**Testcontainers 2.0.3** — declared as a test dep in 6 modules' `pom.xml` (`qqq-backend-core`, `qqq-backend-module-filesystem`, `qqq-backend-module-mongodb`, `qqq-backend-module-postgres`, `qqq-middleware-health`, `qqq-middleware-javalin`):

| Module | Container | Use |
|--------|-----------|-----|
| `qqq-backend-module-postgres` | `PostgreSQLContainer` | Real Postgres for RDBMS tests — `.../postgres/BaseTest.java` uses `@TestInstance(PER_CLASS)` + `@BeforeAll`/`@AfterAll` container lifecycle |
| `qqq-backend-module-mongodb` | `GenericContainer` (MongoDB image) | Real MongoDB — `.../mongodb/BaseTest.java:50-70` |
| `qqq-backend-module-filesystem` | `GenericContainer("atmoz/sftp:latest")` | SFTP server for filesystem module — `.../filesystem/sftp/BaseSFTPTest.java:46-80` |
| `qqq-middleware-health` | (various) | Health-check integration |
| `qqq-middleware-javalin` | (route-provider filesystem tests) | |

A helper dep `net.java.dev.jna:jna:5.18.1` is added in filesystem/postgres poms "to help make testcontainers work".

**Selenium tests** — end-to-end UI tests in `qqq-sample-project/src/test/java/com/kingsrook/sampleapp/selenium/` (`BaseSampleSeleniumTest.java`, `BulkLoadSeleniumTest.java`). The class extends a commented-out `QBaseSeleniumTest` (external library). These appear to be partially disabled / reference-only (per `// extends QBaseSeleniumTest` comment in `BaseSampleSeleniumTest.java:33`).

**H2 / SQLite / in-memory DBs** — the `qqq-backend-module-sqlite` module is itself the SQLite backend; its tests use real SQLite files. No H2 usage detected in test code.

## Per-Module Test Coverage Snapshot

Test-file count vs. main-file count per module (higher ratio = denser test coverage):

| Module | Main .java | Test .java | Ratio |
|--------|-----------:|-----------:|------:|
| `qqq-backend-core` | 1019 | 337 | 0.33 |
| `qqq-backend-module-api` | 24 | 13 | 0.54 |
| `qqq-backend-module-filesystem` | 57 | 42 | 0.74 |
| `qqq-backend-module-rdbms` | 30 | 33 | **1.10** |
| `qqq-backend-module-sqlite` | 4 | 8 | **2.00** |
| `qqq-backend-module-postgres` | 10 | 11 | 1.10 |
| `qqq-backend-module-mongodb` | 18 | 12 | 0.67 |
| `qqq-language-support-javascript` | 1 | 3 | 3.00 |
| `qqq-openapi` | 27 | **0** | **0.00** |
| `qqq-middleware-picocli` | 3 | 3 | 1.00 |
| `qqq-middleware-javalin` | 157 | 40 | 0.25 |
| `qqq-middleware-lambda` | 6 | 3 | 0.50 |
| `qqq-middleware-slack` | 1 | **0** | **0.00** |
| `qqq-middleware-api` | 56 | 21 | 0.38 |
| `qqq-middleware-health` | 12 | 8 | 0.67 |
| `qqq-utility-lambdas` | 1 | **0** | **0.00** |
| `qqq-sample-project` | 23 | 8 | 0.35 |

**Observations:**
- **Thick coverage:** `qqq-backend-module-rdbms`, `qqq-backend-module-sqlite`, `qqq-backend-module-postgres`, `qqq-backend-module-filesystem` — all backend-data modules, tested with Testcontainers.
- **Thin coverage:** `qqq-middleware-javalin` is the largest middleware module (157 main files) with only 40 test files (ratio 0.25) — a known coverage hotspot.
- **Zero test files:** `qqq-openapi` (27 main files), `qqq-middleware-slack` (1 file), `qqq-utility-lambdas` (1 file). The `qqq-middleware-lambda` module has its JaCoCo gate lowered to 10%, also flagged as debt in its `pom.xml`.
- 337 test files in `qqq-backend-core` is by far the largest test suite — `BaseTest` + `TestUtils` pattern has scaled.

## Common Patterns

**Async testing** — the codebase uses thread pools / schedulers (Quartz in `qqq-backend-core/pom.xml:237-240`), but tests avoid real async where possible by driving scheduler ticks directly or disabling sessionId logging via `System.setProperty("qqq.logger.logSessionId.disabled", "true")` (set in `BaseTest.baseBeforeEach`).

**Error testing** — two idiomatic forms:

```java
// JUnit 5 style
assertThrowsExactly(IllegalArgumentException.class,
   () -> JsonUtils.toJson(badInput));

// AssertJ style, with root-cause walking
assertThatThrownBy(() -> JsonUtils.toJson(mapWithNullKey))
   .rootCause()
   .hasMessageContaining("Null key for a Map not allowed in JSON");
```

Both examples from `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/JsonUtilsTest.java:115-116,337`.

**Disabled tests** — 20 `@Disabled` / `@DisabledIf` annotations across the tree. Also used: `@DisabledOnOs` (to skip OS-specific tests, e.g. on macOS dev machines).

**`@TestInstance(Lifecycle.PER_CLASS)`** — used in Testcontainers-based `BaseTest` classes so `@BeforeAll`/`@AfterAll` can be non-static instance methods owning the container field (`qqq-backend-module-postgres/src/test/java/com/kingsrook/qqq/backend/module/postgres/BaseTest.java:50`).

---

*Testing analysis: 2026-04-22*
