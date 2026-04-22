# Requirements: QQQ 4.0

**Defined:** 2026-04-22
**Core Value:** Ship QQQ 4.0 as the first semver-contract release — the version downstream consumers can pin to with confidence that the public API won't churn underneath them.

## v1 Requirements

Milestone 1 scope. Each requirement maps to exactly one roadmap phase.

### Stability (STAB)

Quality gates that must hold at the 4.0.0 ship commit.

- [ ] **STAB-01**: All open GitHub issues labeled `bug`/`defect` on this repo are triaged and either resolved or explicitly deferred (with written reasoning) before 4.0.0 final publish. The deferral list is enumerated in the release notes.
- [ ] **STAB-02**: CI on `develop` passes cleanly on the 4.0.0 release commit — no quieted flakes, no `@Disabled` tests added during the milestone, no excluded test classes in surefire/failsafe configuration, `mvn_test_only` + `static_analysis` both green.
- [ ] **STAB-03**: JaCoCo line + branch coverage at the 4.0.0 ship commit is ≥ the baseline captured at milestone start (baseline recorded in the first phase's research output and committed to `.planning/` for reference).
- [ ] **STAB-04**: Named external/partner consumer applications run green against `4.0.0-RC` artifacts before 4.0.0 final publish. Partner list finalized during `/gsd-discuss-phase` for the partner-validation phase; each partner provides written sign-off recorded in the phase artifacts.

### Breaking Cleanups (BREAK)

Cleanups that use the semver inflection at 4.0 to pay import/API churn once, cleanly.

- [ ] **BREAK-01**: Consolidate `qqq-middleware-javalin` root packages — migrate all classes under `com.kingsrook.qqq.backend.javalin` (legacy) into `com.kingsrook.qqq.middleware.javalin`. `qqq-middleware-javalin` has a single top-level root package post-migration. Imports across the repo and in `qqq-sample-project` updated accordingly.
- [ ] **BREAK-02**: Migrate `qqq-middleware-picocli` from `com.kingsrook.qqq.frontend.picocli` to `com.kingsrook.qqq.middleware.picocli` to align with the middleware-naming convention used by every other `qqq-middleware-*` module.
- [ ] **BREAK-03**: Remove all elements annotated `@Deprecated` from the public surface of every published module (`qqq-backend-core`, `qqq-backend-module-*`, `qqq-middleware-*`, `qqq-openapi`, `qqq-language-support-javascript`, `qqq-bom`). Zero `@Deprecated` public API at the 4.0.0 ship commit. Internal/private deprecations may remain if scoped appropriately.
- [ ] **BREAK-04**: Apply the set of targeted API-shape cleanups held for the semver-contract inflection. Specific cleanups enumerated and committed to `.planning/` during the `/gsd-discuss-phase` step for the API-cleanup phase; each cleanup item becomes a plan inside that phase.

### Release Engineering (REL)

Exercises the existing publishing pipeline against the 4.0 changeset.

- [ ] **REL-01**: Successful 4.0.0-RC publish through the existing pipeline — `release/4.0.x` branch triggers the `publish_release_candidate` CircleCI workflow, `central-publishing-maven-plugin` pushes GPG-signed artifacts (including sources + javadoc jars) to Sonatype Central. All `-RC` artifacts downloadable and resolvable from a clean Maven cache.
- [ ] **REL-02**: Successful 4.0.0 final publish — `v4.0.0` tag on `main` triggers the `publish_release` workflow; artifacts resolve from Maven Central under `com.kingsrook.qqq`. `${revision}` property correctly resolved via `flatten-maven-plugin` so all module POMs carry the final `4.0.0` version.

### Documentation (DOC)

Release comms and migration support for consumers.

- [ ] **DOC-01**: 4.0.0 release notes published — enumerates scope, breaking changes (package renames, removed deprecations, API-shape cleanups), highlights, and the deferred-bug list referenced by STAB-01.
- [ ] **DOC-02**: Migration guide for consumers adopting 4.0 — step-by-step migration covering (a) javalin package-rename import updates, (b) picocli package-rename import updates, (c) specific replacements for each removed `@Deprecated` element, (d) guidance for each API-shape cleanup from BREAK-04. Committed to the repo in a discoverable location (e.g., `docs/migration/4.0.md` or equivalent).

### Sample Project (SAMP)

Out-of-the-box first-run experience.

- [ ] **SAMP-01**: `qqq-sample-project` builds and runs on 4.0.0 with the current published `qqq-frontend-material-dashboard` release (replacing the pinned `0.24.0`). `mvn verify` in `qqq-sample-project/` succeeds against the 4.0.0 artifacts; the reference app boots and serves its primary UI.

## v2 Requirements

No v2 requirements defined for this milestone.

The mono-repo / QBit + external-UI reorg is explicitly a *future milestone*, not v2 deferrals under this one. Opens via `/gsd-new-milestone` after 4.0 ships.

## Out of Scope

| Feature | Reason |
|---|---|
| Mono-repo / QBit + external-UI reorg | Large enough to warrant its own milestone; deferred to milestone 2 via `/gsd-new-milestone`. 4.0 provides a stable baseline against which the reorg can be regression-tested. |
| Raising the JaCoCo coverage floor | 4.0 is a stability release, not a quality-improvement milestone. `STAB-03` requires no-regression, not improvement. |
| Additional breaking renames beyond javalin + picocli | Only the two already in BREAK-01/BREAK-02 are in-scope. Additional renames surface → deferred to 5.0 unless a blocking defect forces the issue. |
| New feature work | Anything that doesn't support shipping 4.0 stably is deferred. New features can land in 4.x minor releases post-ship. |
| New publishing targets / registries | Existing `central-publishing-maven-plugin` → Sonatype Central pipeline is sufficient. No GitHub Packages, internal Nexus, or additional registries in this milestone. |
| Build-tool migration | Maven multi-module stays. Gradle / Bazel / other migrations are out of scope. |
| Sample project being published to Maven Central | `qqq-sample-project` remains committed-but-not-published by design. SAMP-01 requires it to *build and run* on 4.0, not that it ship as a Maven artifact. |

## Traceability

Which phases cover which requirements. Populated by `/gsd-plan-phase` and the roadmapper.

| Requirement | Phase | Status |
|---|---|---|
| STAB-01 | — | Pending |
| STAB-02 | — | Pending |
| STAB-03 | — | Pending |
| STAB-04 | — | Pending |
| BREAK-01 | — | Pending |
| BREAK-02 | — | Pending |
| BREAK-03 | — | Pending |
| BREAK-04 | — | Pending |
| REL-01 | — | Pending |
| REL-02 | — | Pending |
| DOC-01 | — | Pending |
| DOC-02 | — | Pending |
| SAMP-01 | — | Pending |

**Coverage:**
- v1 requirements: 13 total
- Mapped to phases: 0 (roadmap pending)
- Unmapped: 13 ⚠️ — resolves when roadmapper runs

---
*Requirements defined: 2026-04-22*
*Last updated: 2026-04-22 after initial definition*
