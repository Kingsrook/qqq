# Coding Conventions

**Analysis Date:** 2026-04-22

The project ships an authoritative style guide at `CODE_STYLE.md` (226 lines). This document distills that guide and notes what the codebase **actually does** — where the written rules and the observed reality diverge, the divergence is called out explicitly.

## Authoritative References

- `CODE_STYLE.md` — project's own code style guide (single source of truth)
- `checkstyle/config.xml` — enforced formatting/naming rules (blocks build on violations)
- `checkstyle/license.txt` — required file-header block
- `pmd/ruleset.xml` — tuned PMD ruleset (report-only by default)
- `spotbugs/exclude-filter.xml` — SpotBugs suppressions documenting intentional patterns
- `spotbugs-summary.csv` — current SpotBugs findings inventory (top pattern: `EI_EXPOSE_REP` @ 554)
- `qodana.yaml` — JetBrains Qodana profile (`qodana.starter`, JDK 17)
- `suppression.xml` — OWASP dependency-check suppressions

## Java Version & Toolchain

- **Source/target:** Java 21 (`maven.compiler.release=21` in root `pom.xml:87`)
- **Build tool:** Maven 3.8+ (parent `qqq-parent-project`, `pom.xml:29`)
- **IDE alignment:** IntelliJ formatter + Checkstyle, as stated in `CODE_STYLE.md:7`. The project expects “Actions on Save → Reformat Code” enabled for Java. Any “special” formatting will be clobbered.
- **Per-file license header:** first ~20 lines of every Java file MUST match `checkstyle/license.txt` (enforced via `<module name="Header">` in `checkstyle/config.xml:241-245`). Checked in with `ignoreLines=3` so the copyright-year line can vary. Example observed: `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/QException.java:1-20`.

## Formatting

Checkstyle-enforced (see `checkstyle/config.xml`):

- **Indentation:** 3 spaces, no tabs. `FileTabCharacter` check fails on any tab (`checkstyle/config.xml:37-39`). `Indentation` module: `basicOffset=3`, `caseIndent=3`, `throwsIndent=6`, `arrayInitIndent=6` (`checkstyle/config.xml:169-176`).
- **Braces:** opening brace on its **next line** (`LeftCurly option="nl"`, `checkstyle/config.xml:69-71`), closing brace alone on its own line (`RightCurly option="alone"`, `checkstyle/config.xml:72-74`). See any class, e.g. `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/QException.java:32-34`.
- **Line length:** **NOT enforced**. The `LineLength` module is commented out in `checkstyle/config.xml:42-46`. `CODE_STYLE.md:120-126` confirms: "We do not enforce a limit on line lengths... due to living in the time of big monitors." Long fluent chains are broken with operators on the **next line** (`OperatorWrap option="NL"`, `SeparatorWrap DOT option="nl"`).
- **One statement per line** (`OneStatementPerLine`, `checkstyle/config.xml:87`).
- **One top-level class per file** (`OneTopLevelClass`, `checkstyle/config.xml:62`).
- **Whitespace:** at most 1 blank line within a block; **3 blank lines between methods** (`CODE_STYLE.md:20-21` — visible everywhere, e.g. `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/QException.java:46-54`).
- **No trailing whitespace** is enforced by IDE conventions (not Checkstyle), via the shared IntelliJ code-style config referenced in `CODE_STYLE.md:8-10`.

## Naming Patterns

All patterns below are enforced by Checkstyle regex in `checkstyle/config.xml:124-161,227-231`.

**Packages:** all-lowercase, `^[a-z]+(\.[a-z][a-z0-9]*)*$`.
- Observed root: `com.kingsrook.qqq.*` (groupId is `com.kingsrook.qqq`, `pom.xml:28`).

**Classes / interfaces / enums (types):** default Checkstyle pattern (UpperCamelCase). QQQ framework types carry a `Q` prefix to namespace them — `QInstance`, `QFieldMetaData`, `QTableMetaData`, `QException`, `QRecord`, `QContext`, `QLogger`, `QSession`, `QUser`. `CODE_STYLE.md:36` calls this out as a QQQ-specific convention.

**Methods:** `^[a-z][a-zA-Z0-9_]*$` — `lowerCamelCase`, underscores permitted (used in test method names only, e.g. `test_toJsonNull` in `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/JsonUtilsTest.java:113`).

**Fields / parameters / locals:** all `^[_a-zA-Z]([a-z0-9A-Z][a-zA-Z0-9]*)?$` — `lowerCamelCase`, leading underscore permitted only for members.
- **No Hungarian notation** (`strZipCode` forbidden by convention, `CODE_STYLE.md:27-28`).
- **Verbose over abbreviated:** `CODE_STYLE.md:25-26`. Loop index `i` and caught exception `e` are the accepted exceptions.
- **Suffix patterns** that invert Hungarian are accepted: `somethingList`, `somethingMap` (`CODE_STYLE.md:29-30`).
- **Generic type parameters:** `(^[A-Z][0-9]?)$|([A-Z][_A-Z0-9]*$)` — single uppercase letter with optional digit, or ALL_CAPS (`checkstyle/config.xml:150-161`).

**QQQ framework field meta-data:** `lowerCamelCase` at the Java layer, with `backendName` carrying the `snake_case` database column name. `QInstanceEnricher` converts between the two so code doesn't have to. (`CODE_STYLE.md:31-35`.)

**MetaDataProducer naming convention** (`CODE_STYLE.md:194-213`):
- `public static final String NAME` in each producer (`lowerCamelCase`, e.g. `cancelOrder`).
- Producer class = `{ObjectName}{MetaDataKind}MetaDataProducer`, e.g. `OrderTableMetaDataProducer`, `SendImportantMessageProcessMetaDataProducer`, `CoolParcelsWidgetMetaDataProducer`, `DailyDashboardAppMetaDataProducer`.
- `PVS` preferred over `PossibleValueSource` for brevity.
- Join producers: `FooJoinBarMetaDataProducer` (not `FooJoinBarJoinMetaDataProducer`).
- ~110 files reference `MetaDataProducer` / `MetaDataProducerInterface` (`grep -rln MetaDataProducer`).

## Comment Style

Javadoc is **mandatory** on classes and methods — enforced by `MissingJavadocMethod` and `MissingJavadocType` (`scope="private"` — i.e. required even on private members; `checkstyle/config.xml:221-226`).

**Block-style "flower box" Javadoc (80-char border)** — see `CODE_STYLE.md:40-60` and every production class, e.g. `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/logging/QLogger.java:45-48`:

```java
/*******************************************************************************
 ** Wrapper for
 **
 *******************************************************************************/
```

- Line 1: `/` + 79 `*`
- Middle lines: ` ** ` + text
- Last line: ` ` + 79 `*` + `/`
- **No blank line between Javadoc and the declaration** (`CODE_STYLE.md:44`).
- Empty Javadoc blocks (just the border) are acceptable as visual separators.
- HTML tags and `@param`/`@return` are **not required** and generally omitted — plain text is preferred (`CODE_STYLE.md:58-60`).
- Generated/maintained by the Kingsrook Commentator IntelliJ plugin.

**In-method "flower box" comments** — `CODE_STYLE.md:62-80`. Full rectangle of `/` with `//` on each line, right-padded to equal width. Example in `qqq-backend-core/src/test/java/com/kingsrook/qqq/backend/core/utils/JsonUtilsTest.java:334-338`:

```java
//////////////////////////////////////////////////////
// assert default behavior throws with null map key //
//////////////////////////////////////////////////////
```

**Zombie code is forbidden** (`CODE_STYLE.md:81-85`). If kept, it must be preceded by a flower-box comment explaining why. Delete-and-trust-git is the norm.

## Fluent / Builder Style

Fluent `withXxx(...)` setters returning `this` are used **everywhere** rather than classic `setXxx()` chains (`CODE_STYLE.md:88-92`).

- ~163 main-source files in `qqq-backend-core` use `withName`/`withLabel` method names.
- `QFieldMetaData` alone has ~15 `withXxx` methods (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/fields/QFieldMetaData.java:282-649`).
- Pattern: every field gets three accessors — `getFoo()` / `setFoo(...)` / `withFoo(...) { this.foo = x; return this; }` — with Javadoc "Getter for foo" / "Setter for foo" / "Fluent setter for foo".
- `QException` itself uses this pattern (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/QException.java:93-99,125-131`).

SpotBugs exemptions document this as intentional:
> "Fluent builders intentionally return 'this' and expose mutable internal state for chaining. This is by design in QQQ."
> (`spotbugs/exclude-filter.xml:24-26`; 554 `EI_EXPOSE_REP` findings in `spotbugs-summary.csv` are the cost of this pattern.)

## Primitive Types vs. Wrappers

- **Prefer wrapper types** (`Integer` over `int`, `Boolean` over `boolean`) — `CODE_STYLE.md:94-99`. Rationale: database-backed data can be null.
- **Use `.equals()` or `Objects.equals()`** for number comparison, **not `==`** (`CODE_STYLE.md:100-104`).
- Primitives permitted only in measured hot loops.
- Observed: `QFieldMetaData` still declares `private boolean isRequired = false;` etc. (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/fields/QFieldMetaData.java:67-69`) — so the rule is a preference, not absolute.

## Method Lengths

No hard limit. `CODE_STYLE.md:107-118` explicitly rejects both 1000-line methods and sub-10-line-method extremism. "Seek balance in the force" — roughly ~50-line methods in ~1000-line classes is cited as the comfort zone. `TooManyMethods`, `TooManyFields`, `GodClass`, `NcssCount`, `CyclomaticComplexity`, `CognitiveComplexity`, `NPathComplexity`, `ExcessiveParameterList` are all **excluded** from PMD (`pmd/ruleset.xml:52-70`) — the project intentionally permits large metadata classes.

## Imports

- **No wildcard imports.** `AvoidStarImport` enforced (`checkstyle/config.xml:61`). Reason: collision avoidance + IDE folding makes explicit imports free (`CODE_STYLE.md:143-147`).
- **Import order** (`checkstyle/config.xml:190-196`): `SPECIAL_IMPORTS` (`javax.*`) → `STANDARD_JAVA_PACKAGE` (`java.*`) → `THIRD_PARTY_PACKAGE` → `STATIC`. Alphabetical within group. **No separator blank lines between groups**.
- **Static imports are encouraged for common utilities** (`CODE_STYLE.md:130-141`): `logPair` (from `LogUtils`), `assertX` (JUnit + AssertJ), `QFilterOperator.*`. Observed: 117 main-source files statically import `logPair` in `qqq-backend-core`.
- **Qualified references for specialized utilities** (`ShippingUtilities.getMilesBetweenZipCodes()`) — don't static-import helpers that only ~a-few-classes use.
- PMD excludes `TooManyStaticImports` (`pmd/ruleset.xml:42`).

## Error Handling

**Custom exception hierarchy** — all rooted at `QException extends Exception` (checked). Located in `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/`:

| Class | Purpose |
|-------|---------|
| `QException` | Base — `qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/exceptions/QException.java` |
| `QRuntimeException` | Unchecked base |
| `QUserFacingException` | Message is safe to show end-users (`QUserFacingException.java`) |
| `QBadRequestException` | 400-ish |
| `QNotFoundException` | 404-ish |
| `QPermissionDeniedException` | 403-ish |
| `QAuthenticationException` | Auth failures |
| `QInstanceValidationException` | Meta-data validation |
| `QValueException` | Value conversion |
| `QCodeException`, `QFormulaException`, `QReportingException`, `QModuleDispatchException`, `AccessTokenException` | Domain-specific |

`QException` carries tri-state logged-flags (`hasLoggedWarning`, `hasLoggedError`) plus fluent setters — used to suppress duplicate logging up the stack (`QException.java:34-131`).

- **Checked exceptions are the norm.** `719` method signatures in `qqq-backend-core/src/main` declare `throws Exception` or `throws QException` (single-exception signatures — total is much higher with multi-throws).
- PMD excludes `SignatureDeclareThrowsException` and `AvoidThrowingRawExceptionTypes` and `AvoidCatchingGenericException` (`pmd/ruleset.xml:73-75`) — broad `throws`/`catch` is accepted.
- **Empty catch blocks** are allowed only if the caught variable is named `expected` (`EmptyCatchBlock exceptionVariableName="expected"` in `checkstyle/config.xml:232-234`).
- **`e.printStackTrace()` / `System.out` / `System.err`** are discouraged — should be flagged in code review (`CODE_STYLE.md:167-168`). Observed 314 call sites grepping across the repo — not yet fully purged.

## Logging

**Framework:** Log4j 2 via QQQ's `QLogger` wrapper.
- `org.apache.logging.log4j:log4j-api` and `log4j-core` — version `2.25.3` (`pom.xml:221-229`).
- SLF4J is bridged to log4j2 via `log4j-slf4j-impl` (`qqq-backend-core/pom.xml:249-254`); no direct SLF4J logger creation in the code (grep confirms 0 `LoggerFactory` usages).
- Root log config: `qqq-backend-core/src/main/resources/log4j2.xml` — console + syslog (RFC5424) appenders. Syslog emits JSON-shaped messages for structured log aggregation (Loggly).

**Mandatory pattern** (`CODE_STYLE.md:150-168`, enforced by code review):

```java
private static final QLogger LOG = QLogger.getLogger(YourClass.class);
```

- 298 files import `QLogger` via this exact pattern (`grep -rln "private static final QLogger LOG"`).
- Use overloads taking `LogPair` (from `com.kingsrook.qqq.backend.core.logging.LogUtils.logPair`) for structured key-value logging.
- `logPair` is conventionally **statically imported** (`CODE_STYLE.md:156-157`).
- Use exception-accepting overloads when logging an exception.

**Log-level guidance** (`CODE_STYLE.md:161-166`):
- `warn`/`error` are meant to be actionable — if one fires, an engineer should care.
- Target mix: ~95% `warn` vs. 5% `error`. `error` is reserved for framework/integration-level failures (e.g. "couldn't connect to DB").
- The guide acknowledges the codebase may not fully meet this goal today.

## Annotation-Based Entity Model (newer pattern)

Introduced early 2025 (`CODE_STYLE.md:224`). The preferred new way to define tables:
- `@QMetaDataProducingEntity` on a `QRecordEntity` subclass auto-generates table meta-data.
- Combined with `@QField(...)` per field.
- 67 files currently use `@QField` / `@QMetaDataProducingEntity` / `@QTable` / `extends QRecordEntity`.
- Coexists with older `MetaDataProducerInterface` classes — both are in active use.

## Configuration Classes / Framework Patterns

- **`QInstance`** is the top-level runtime meta-data container (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/metadata/QInstance.java`).
- **`QContext`** is a `ThreadLocal`-based request context (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/context/`). PMD excludes `DoNotUseThreads` and `UseConcurrentHashMap` to permit this pattern (`pmd/ruleset.xml:112-113`).
- **`QRecord`** is the generic data carrier — backed by `Map<String, Serializable>` (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/model/data/QRecord.java`).
- **Actions** (`qqq-backend-core/src/main/java/com/kingsrook/qqq/backend/core/actions/`) follow an Input/Output pattern: `QueryAction.execute(QueryInput) → QueryOutput`, `InsertAction`, `UpdateAction`, `CountAction`, `GetAction`, etc.
- **Processes / Steps** — backend steps implement `BackendStep` and are wired together by process meta-data.

## Static-Analysis Stance

Four-tool setup, but with deliberate tolerance levels:

| Tool | Config | Enforcement |
|------|--------|-------------|
| **Checkstyle 13.0.0** | `checkstyle/config.xml` | **Fails build** on warning-severity violations (`failOnViolation=true`, `violationSeverity=warning`, `pom.xml:319-321`). Runs in `validate` phase — before compile. Covers test sources too (`includeTestSourceDirectory=true`, `pom.xml:324`). |
| **SpotBugs 4.9.8.2** | `spotbugs/exclude-filter.xml` + `findsecbugs-plugin 1.14.0` | `effort=Max`, `threshold=Medium` — but `failOnError=${spotbugs.failOnError}` defaults to **`false`** (`pom.xml:97,500`). Report-only today. Excludes all `*Test` / `*Tests` / `*TestUtils` classes. |
| **PMD 3.28.0** (pmd-core 7.20.0) | `pmd/ruleset.xml` | `failOnViolation=${pmd.failOnViolation}` defaults to **`false`** (`pom.xml:98,526`). Report-only today. |
| **Qodana (JetBrains)** | `qodana.yaml` — `qodana.starter` profile, `projectJDK: 17` | Runs in CI; informational. Linter `jetbrains/qodana-jvm:latest`. |
| **JaCoCo 0.8.14** | Coverage gates (see below) | **Fails build** on coverage shortfall by default. |

CI (`.circleci/config.yml:20-26,37-42`) runs a dedicated `qqq-orb/static_analysis` job in parallel with `mvn_test_only` / `mvn_publish` — so a static-analysis regression won't block a feature test run but is visible in the pipeline.

**Current SpotBugs inventory** (from `spotbugs-summary.csv`):

Top 10 findings by count (project-wide, Medium+ threshold):

| Rank | Pattern | Count | Category |
|------|---------|-------|----------|
| 1 | `EI_EXPOSE_REP` | 554 | Code Quality (intentional — fluent-builder cost) |
| 2 | `CT_CONSTRUCTOR_THROW` | 52 | Code Quality |
| 3 | `SE_BAD_FIELD` | 45 | Serialization (intentional — JSON is the ser format) |
| 4 | `REC_CATCH_EXCEPTION` | 36 | Code Quality (broad catch) |
| 5 | `PATH_TRAVERSAL_IN` | 33 | Security |
| 6 | `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` | 23 | Concurrency (High severity) |
| 7 | `DM_DEFAULT_ENCODING` | 22 | i18n |
| 8 | `MS_EXPOSE_REP` | 20 | Code Quality |
| 9 | `DLS_DEAD_LOCAL_STORE` | 17 | Code Quality |
| 10 | `WMI_WRONG_MAP_ITERATOR` | 14 | Performance |

Notable **high-severity** findings still present:
- `SQL_INJECTION_JDBC` × 13, `SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING` × 1 — flagged for review.
- `COMMAND_INJECTION` × 3, `SCRIPT_ENGINE_INJECTION` × 1 — flagged for review.
- `UNSAFE_HASH_EQUALS` × 1 — timing attack.
- `EQ_ALWAYS_FALSE` × 2 — broken `equals`.

**Stance summary:** Checkstyle is strict and blocking (formatting + Javadoc presence). PMD and SpotBugs are tuned-down and report-only; the PMD ruleset (`pmd/ruleset.xml`) explicitly excludes ~40 rules that conflict with QQQ patterns (fluent builders, large metadata classes, wrapper-type preference, `QContext` thread-locals). The trajectory is clear: the team codified its conventions and tuned analyzers to match, rather than vice versa.

## Function / Method Design

- **Return values:** `null` is acceptable for "no value" (PMD `ReturnEmptyCollectionRatherThanNull` excluded — `pmd/ruleset.xml:91`).
- **Parameters:** `ExcessiveParameterList` excluded (`pmd/ruleset.xml:66`).
- **Side effects:** PMD `AccessorMethodGeneration` excluded (`pmd/ruleset.xml:25`) to allow fluent chaining.
- **Multiple returns per method:** permitted (`OnlyOneReturn` excluded, `pmd/ruleset.xml:38`).

## Module Design

- **Exports:** classes are public unless there's a reason otherwise. No sealed types observed.
- **No barrel files / package-info classes** observed at the top level.
- `META-INF/services/...MetaDataProducerInterface` is the ServiceLoader registration mechanism for plugin-discovery of meta-data producers (inferred from the `MetaDataProducerHelper.java` in `qqq-backend-core`).
- The root `pom.xml:59-77` owns 17 modules. `qqq-bom` is a Maven BOM for dependency-version sharing (it's listed in `modules` but does not build java).

## Module-Scoped Overrides

Several modules override parent properties in their own `pom.xml`:
- `qqq-middleware-health/pom.xml` — coverage minimums lowered to 0.70/0.90 (parent: 0.80/0.95) with comment "this module has reasonable coverage."
- `qqq-middleware-lambda/pom.xml` — coverage minimums lowered to 0.10/0.10 with comment "todo - remove these!!" — known debt.
- `qqq-utility-lambdas/pom.xml` — carries its own JaCoCo rule block.

---

*Convention analysis: 2026-04-22*
