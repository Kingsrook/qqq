# QQQ

## What This Is

QQQ is a metadata-driven Java application framework, delivered as a multi-module Maven library suite published to Maven Central under `com.kingsrook.qqq`. Consuming applications declare a `QInstance` — tables, processes, fields, backends, authentication, apps, reports, widgets — and QQQ's action pipeline drives all CRUD and process execution against that metadata. The same metadata-defined app is exposed through pluggable middleware surfaces (HTTP via Javalin, CLI via PicoCLI, AWS Lambda, Slack, health probes) with no duplication of business logic. AGPL-3.0 licensed.

## Core Value

**Ship QQQ 4.0 as the first semver-contract release — the version downstream consumers can pin to with confidence that the public API won't churn underneath them.**

## Requirements

### Validated

<!-- Inferred from the existing codebase; locked and relied upon. -->

- ✓ Metadata-driven application model — `QInstance` aggregates tables, processes, fields, backends, auth, joins, apps, reports, widgets, permissions, security locks — existing
- ✓ Uniform action pipeline — every CRUD/read/report/process operation flows through typed `*Input`/`*Output` + action class pairs — existing
- ✓ Pluggable backend storage via SPI — `QBackendModuleInterface` + dispatcher; in-core memory/enumeration/mock; external RDBMS/filesystem/MongoDB/DynamoDB modules — existing
- ✓ Pluggable authentication via SPI — Auth0, OAuth2, TableBased, Mock, FullyAnonymous — existing
- ✓ Pluggable scheduling & messaging via SPI — existing
- ✓ Multi-transport middleware — Javalin HTTP, PicoCLI, Slack, AWS Lambda, health probes, OpenAPI spec publishing — existing
- ✓ Reporting & export — CSV / TSV / JSON / Excel streamers, `RecordPipe`, `GenerateReportAction` — existing
- ✓ Automation — trigger handlers, polling automation, SQS-based scheduling — existing
- ✓ Customizer hooks — pre/post insert/update/delete/query via `QCodeReference` — existing
- ✓ QBit packaging pattern — `QBitProducer` + `QBitConfig` + `QBitMetaData` for drop-in bundles of tables/processes/widgets — existing
- ✓ Security locks, permissions, audits — existing
- ✓ Thread-local execution context — `QContext` holds `QInstance` / `QSession` / transaction / action stack — existing
- ✓ Maven Central publishing pipeline — `central-publishing-maven-plugin` + GPG signing via `release` profile, CircleCI workflows for snapshot / RC / release / hotfix — existing
- ✓ CircleCI orb (`kingsrook/qqq-orb`) — `mvn_test_only`, `mvn_publish`, `static_analysis` jobs — existing
- ✓ JaCoCo coverage reporting — existing (baseline to be captured during milestone 1)

### Active

<!-- Milestone 1 scope: ship 4.0. Hypotheses until validated by the release itself. -->

- [ ] **Zero open bugs at ship** — triage all open GitHub bug/defect issues on this repo, resolve or explicitly defer with reasoning
- [ ] **CI green on develop at ship** — no quieted flakes, no excluded tests, all workflows green
- [ ] **No coverage regression** — JaCoCo line/branch coverage at ship ≥ baseline captured at milestone start
- [ ] **External / partner consumer apps validated on 4.0-RC** — named apps run green against the release candidate before 4.0.0 final (partner list captured during discuss-phase)
- [ ] **Package rename: javalin** — consolidate `com.kingsrook.qqq.backend.javalin` into `com.kingsrook.qqq.middleware.javalin`, eliminating the dual-root-package state
- [ ] **Package rename: picocli** — migrate `com.kingsrook.qqq.frontend.picocli` to `com.kingsrook.qqq.middleware.picocli` (aligns with the middleware convention)
- [ ] **Deprecation sweep** — remove all `@Deprecated` elements on the public surface across published modules
- [ ] **API shape cleanups** — targeted signature / return-type cleanups held for the semver-contract inflection (list finalized during discuss-phase)
- [ ] **Release-candidate dry run** — publish 4.0.0-RC through the existing CircleCI → Sonatype Central pipeline and shake out release-only issues
- [ ] **4.0.0 final publish** — ship with GPG-signed artifacts, sources + javadoc jars, through the existing pipeline
- [ ] **Release notes + changelog** — user-facing 4.0 release notes covering scope, breaks, and migration
- [ ] **Migration guide** — step-by-step guide for consumers adopting 4.0: package renames, removed deprecations, API-shape cleanups
- [ ] **Sample project on 4.0** — `qqq-sample-project` updated to run on 4.0 with current `qqq-frontend-material-dashboard` (not the pinned 0.24.0)

### Out of Scope

<!-- Explicit boundaries for milestone 1. -->

- Mono-repo / QBit + external-UI reorg — deferred to milestone 2 via `/gsd-new-milestone` after 4.0 ships. The stable 4.0 baseline makes that reorg measurable (regression-test against 4.0).
- Raising the JaCoCo coverage floor — 4.0 requires *no regression*, not improvement. Quality-improvement work is valid follow-on.
- Breaking renames beyond javalin + picocli — only the two already named are in-scope. Additional renames that surface are deferred to a future major (5.0) unless they're a blocking defect.
- New feature work — anything that doesn't support shipping 4.0 stably is deferred.
- New publishing targets / registries — the existing `central-publishing-maven-plugin` → Sonatype Central pipeline is sufficient. No GitHub Packages, internal Nexus, or additional registries in this milestone.

## Context

- **Version-number correction.** Current develop is `0.42.0-SNAPSHOT`; `0.40.0` already shipped. The jump to 4.0 corrects long-standing version-number drift (per author: "0.40 should have been 4.0") and explicitly starts the semver contract for QQQ.
- **Why breaking cleanups belong in 4.0.** Post-4.0, the renames of `backend.javalin` / `frontend.picocli` and the deprecation removals become multi-year-deferred under semver. Consumers pay import-churn once, cleanly, at the version inflection point.
- **Mature publishing infrastructure.** `central-publishing-maven-plugin` 0.10.0 publishes to Sonatype Central (`publishingServerId=central`, `autoPublish=true`), GPG signing via `maven-gpg-plugin` in the `release` profile. CI workflows are branch-gated: `publish_snapshot` (develop), `publish_feature` (`feature/*` with `publish*` tags), `publish_release_candidate` (`release/*`), `publish_release` (main + `v*` tags), `publish_hotfix_release` (`hotfix/*`). No pipeline rebuild is needed.
- **Package-name drift (known technical debt).** `qqq-middleware-javalin` currently carries two top-level root packages — legacy `com.kingsrook.qqq.backend.javalin` (older classes like `QJavalinImplementation`) and newer `com.kingsrook.qqq.middleware.javalin` (versioned code under `specs/`, `executors/`, `routeproviders/`). `qqq-middleware-picocli` uses `com.kingsrook.qqq.frontend.picocli` from before the middleware-naming convention existed.
- **Sample project lag.** `qqq-sample-project` is committed, not published, and pins `qqq-frontend-material-dashboard 0.24.0` — noticeably behind the actively-developed frontend. Updating it is part of the 4.0 "works out of the box" story.
- **Reorg queued for milestone 2.** After 4.0 ships: look across QBits + external UIs (`qqq-frontend-material-dashboard` and siblings) and reshape the mono-repo / mono-project structure so that (a) the whole thing versions and publishes as one coherent release, and (b) a user can bootstrap a new QQQ project with all features working out of the box. Explicitly scoped in a future `/gsd-new-milestone`.

## Constraints

- **Compatibility**: 4.0 begins the public semver contract. Any API break inside 4.x is forbidden post-ship — breaking cleanups MUST land in 4.0 or wait for 5.0.
- **Publishing**: Must flow through the existing `central-publishing-maven-plugin` + CircleCI + Sonatype Central pipeline. No new registries or publishing infrastructure for this milestone.
- **Licensing**: AGPL-3.0. License-header checkstyle rule enforced on every Java source file.
- **Build stack**: Maven multi-module. `${revision}` property drives all module versions (`flatten-maven-plugin` resolves at publish time). No build-tool migration in scope.
- **Dependencies**: `qqq-dev-tools` and `qqq-sample-project` are unpublished by design — don't accidentally add `<distributionManagement>` / publish configuration to them.
- **Timeline**: Soft — ship when quality gates are met. No calendar pressure.

## Key Decisions

| Decision | Rationale | Outcome |
|---|---|---|
| Ship next release as **4.0.0** (not 0.42.0) | Corrects long-standing version-number drift ("0.40 should have been 4.0") and marks the start of the semver contract. | — Pending |
| Fold breaking renames + deprecation sweep into 4.0 | Post-4.0, these become multi-year-deferred under the semver contract. Consumer pain is paid once, cleanly, at the inflection point. | — Pending |
| Include `qqq-sample-project` update in 4.0 scope | The "works out of the box" story is part of a stable release. Pinning a sample to an old frontend signals instability. | — Pending |
| Hold JaCoCo coverage at current baseline, don't raise the floor | 4.0 is a stability release, not a quality-improvement milestone. Keeps scope tight. | — Pending |
| Defer mono-repo / QBit + UI reorg to milestone 2 | Keeps 4.0 focus tight; reorg is large enough to warrant its own milestone. The stable 4.0 base makes reorg regressions measurable. | — Pending |
| Bug-source = all open GitHub bug/defect issues on this repo | Single authoritative source; triage during phase 1. No separate tracker to sync. | — Pending |
| Reuse existing publishing pipeline; no new registry | Pipeline is mature and branch-gated. Adding registries is scope creep that doesn't serve the stability goal. | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-22 after initialization*
