# Roadmap: QQQ 4.0.0 — Stabilize & Publish

## Overview

This milestone ships QQQ 4.0.0 as the first semver-contract release. The work begins by measuring the current state (JaCoCo baseline, open-bug inventory, partner list, API-shape cleanup list), then executes the breaking cleanups that can only land now (javalin package consolidation, picocli package rename, deprecation sweep, targeted API-shape fixes), resolves all open bugs, writes the consumer-facing docs, updates the sample project, and finally publishes through the existing CircleCI pipeline. Phases 2 and 3 are independent and can run in parallel.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Baseline & Triage** - Capture JaCoCo baseline, inventory open bugs, finalize partner list, enumerate API-shape cleanup items
- [ ] **Phase 2: Javalin Package Consolidation** - Migrate all classes from `com.kingsrook.qqq.backend.javalin` into `com.kingsrook.qqq.middleware.javalin`
- [ ] **Phase 3: PicoCLI Package Rename** - Migrate `com.kingsrook.qqq.frontend.picocli` to `com.kingsrook.qqq.middleware.picocli`
- [ ] **Phase 4: Deprecation Sweep & API Shape Cleanups** - Remove all `@Deprecated` public surface; apply enumerated API-shape fixes
- [ ] **Phase 5: Bug Fix Execution** - Resolve or explicitly defer every open GitHub bug/defect issue; confirm CI green
- [ ] **Phase 6: Documentation** - Publish 4.0 release notes and migration guide
- [ ] **Phase 7: Sample Project** - Update `qqq-sample-project` to 4.0 with current frontend
- [ ] **Phase 8: Release Engineering** - RC publish, partner validation, final 4.0.0 publish

## Phase Details

### Phase 1: Baseline & Triage
**Goal**: Measure the current state so all subsequent phases have concrete targets
**Depends on**: Nothing (first phase)
**Requirements**: (no v1 requirement maps here — this phase produces the inputs that gate later requirements)
**Success Criteria** (what must be TRUE):
  1. A JaCoCo coverage report is generated from the current develop HEAD and the line/branch percentages per module are recorded in `.planning/baseline-coverage.md`
  2. Every open GitHub issue labeled `bug` or `defect` on this repo is enumerated in `.planning/bug-triage.md` with an owner (resolve or defer) assigned to each
  3. The partner/consumer application list is finalized and recorded in `.planning/partners.md` — each named app is confirmed willing to validate against 4.0-RC
  4. The set of targeted API-shape cleanups is enumerated and committed to `.planning/api-shape-cleanups.md`; each item has a brief rationale and becomes a plan in Phase 4
**Plans**: TBD
**UI hint**: no

### Phase 2: Javalin Package Consolidation
**Goal**: `qqq-middleware-javalin` has a single top-level root package
**Depends on**: Phase 1
**Requirements**: BREAK-01
**Success Criteria** (what must be TRUE):
  1. No Java file under `qqq-middleware-javalin/src/` is rooted at `com.kingsrook.qqq.backend.javalin` — the legacy root package is gone
  2. All classes formerly under `com.kingsrook.qqq.backend.javalin` (including `QJavalinImplementation`, `QJavalinMetaData`, `QJavalinProcessHandler`, etc.) now exist under `com.kingsrook.qqq.middleware.javalin`
  3. All import statements across the entire monorepo and `qqq-sample-project` referencing `com.kingsrook.qqq.backend.javalin` are updated to `com.kingsrook.qqq.middleware.javalin`
  4. `mvn verify` passes cleanly (checkstyle, tests, JaCoCo gate) on `qqq-middleware-javalin` and all dependent modules (`qqq-middleware-api`, `qqq-middleware-health`, `qqq-middleware-slack`)
**Plans**: TBD
**UI hint**: no

### Phase 3: PicoCLI Package Rename
**Goal**: `qqq-middleware-picocli` is rooted at `com.kingsrook.qqq.middleware.picocli`, consistent with every other `qqq-middleware-*` module
**Depends on**: Phase 1
**Requirements**: BREAK-02
**Success Criteria** (what must be TRUE):
  1. No Java file under `qqq-middleware-picocli/src/` is rooted at `com.kingsrook.qqq.frontend.picocli` — the old root package is gone
  2. `QPicoCliImplementation`, `QCommandBuilder`, and `PicoCliProcessCallback` now live under `com.kingsrook.qqq.middleware.picocli`
  3. All import statements across the monorepo and `qqq-sample-project` referencing `com.kingsrook.qqq.frontend.picocli` are updated
  4. `mvn verify` passes cleanly on `qqq-middleware-picocli` and any modules that import it
**Plans**: TBD
**UI hint**: no

### Phase 4: Deprecation Sweep & API Shape Cleanups
**Goal**: The 4.0 public API surface has zero `@Deprecated` elements and all held API-shape fixes are applied
**Depends on**: Phase 2, Phase 3
**Requirements**: BREAK-03, BREAK-04
**Success Criteria** (what must be TRUE):
  1. A grep for `@Deprecated` across all published modules (`qqq-backend-core`, `qqq-backend-module-*`, `qqq-middleware-*`, `qqq-openapi`, `qqq-language-support-javascript`, `qqq-bom`) returns zero results on public-surface members
  2. Every API-shape cleanup item from `.planning/api-shape-cleanups.md` (enumerated in Phase 1) is marked complete with a commit reference
  3. `mvn verify` passes cleanly across all published modules after the sweep
  4. No new `@Deprecated` annotations are introduced during the sweep — only removal
**Plans**: TBD
**UI hint**: no

### Phase 5: Bug Fix Execution
**Goal**: Every open GitHub bug/defect issue is resolved or explicitly deferred; CI runs cleanly
**Depends on**: Phase 1 (triage output), Phase 4 (breaking changes complete so bug fixes land on stable surface)
**Requirements**: STAB-01, STAB-02
**Success Criteria** (what must be TRUE):
  1. Every issue enumerated in `.planning/bug-triage.md` is either closed (with a fix merged) or carries a written deferral comment explaining why it does not block 4.0
  2. No open GitHub issues labeled `bug` or `defect` are in an untriaged state
  3. The CircleCI `mvn_test_only` and `static_analysis` jobs both pass green on `develop` with no `@Disabled` tests added during this milestone and no test classes excluded from surefire configuration
  4. Checkstyle (including the AGPL license header rule) passes on every modified file
**Plans**: TBD
**UI hint**: no

### Phase 6: Documentation
**Goal**: Consumers have accurate, complete release notes and a step-by-step migration guide for adopting 4.0
**Depends on**: Phase 4 (breaking changes finalized so migration content is accurate), Phase 5 (deferred-bug list from STAB-01 is complete)
**Requirements**: DOC-01, DOC-02
**Success Criteria** (what must be TRUE):
  1. A `CHANGELOG` / release-notes entry for 4.0.0 is committed to the repo — it enumerates breaking changes (package renames, removed deprecations, API-shape cleanups) and lists deferred bugs by issue number
  2. A migration guide is committed to the repo at a discoverable path (e.g., `docs/migration/4.0.md`) covering all four areas: javalin import updates, picocli import updates, replacements for each removed `@Deprecated` element, and guidance for each API-shape cleanup
  3. The migration guide contains at least one concrete before/after example for each category of breaking change
**Plans**: TBD
**UI hint**: no

### Phase 7: Sample Project
**Goal**: `qqq-sample-project` builds and runs on 4.0 artifacts with the current frontend
**Depends on**: Phase 2, Phase 3, Phase 4 (all breaking changes done so sample imports are final)
**Requirements**: SAMP-01
**Success Criteria** (what must be TRUE):
  1. `qqq-sample-project/pom.xml` references QQQ modules at version `4.0.0` (or the RC tag during pre-release verification) rather than any snapshot
  2. The pinned `qqq-frontend-material-dashboard 0.24.0` dependency is replaced with the current published release
  3. `mvn verify` in `qqq-sample-project/` succeeds against the 4.0 artifacts with zero test failures
  4. `SampleJavalinServer` boots and its primary UI is reachable (manual boot-test documented in the phase summary)
**Plans**: TBD
**UI hint**: no

### Phase 8: Release Engineering
**Goal**: 4.0.0-RC and 4.0.0 final artifacts are published to Maven Central and all quality gates are confirmed at the ship commit
**Depends on**: Phase 5, Phase 6, Phase 7
**Requirements**: STAB-03, STAB-04, REL-01, REL-02
**Success Criteria** (what must be TRUE):
  1. A `release/4.0.x` branch triggers the `publish_release_candidate` CircleCI workflow; GPG-signed artifacts including sources and javadoc jars are downloadable from Sonatype Central under the `4.0.0-RC` version and resolve from a clean Maven local cache
  2. Every named partner application listed in `.planning/partners.md` runs green against the `4.0.0-RC` artifacts and provides written sign-off recorded in the phase artifacts
  3. JaCoCo line and branch coverage at the 4.0.0 ship commit is at or above the baseline figures captured in `.planning/baseline-coverage.md` (Phase 1 output) for each module
  4. A `v4.0.0` tag on `main` triggers the `publish_release` CircleCI workflow; `mvn dependency:get -Dartifact=com.kingsrook.qqq:qqq-backend-core:4.0.0` resolves successfully from Maven Central with `${revision}` correctly resolved via `flatten-maven-plugin` so all module POMs carry `4.0.0` (not `${revision}`)
**Plans**: TBD
**UI hint**: no

## Progress

**Execution Order:**
Phases 2 and 3 are independent and can run in parallel. All other phases are sequential.

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Baseline & Triage | 0/TBD | Not started | - |
| 2. Javalin Package Consolidation | 0/TBD | Not started | - |
| 3. PicoCLI Package Rename | 0/TBD | Not started | - |
| 4. Deprecation Sweep & API Shape Cleanups | 0/TBD | Not started | - |
| 5. Bug Fix Execution | 0/TBD | Not started | - |
| 6. Documentation | 0/TBD | Not started | - |
| 7. Sample Project | 0/TBD | Not started | - |
| 8. Release Engineering | 0/TBD | Not started | - |
