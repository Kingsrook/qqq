# qqq-bom Coverage Audit

**Date:** 2026-04-22
**Audit scope:** `qqq-bom` Maven BOM at `/qrun-io/qqq/qqq-bom/`, evaluated against the full QQQ ecosystem (core modules, 20 QBits, starters, qctl) at `/Users/james.maes/Git.Local/qrun-io/`.
**Purpose:** Determine whether `qqq-bom` is positioned to serve as a release-train backbone. Facts only — no recommendations.

---

## 1 · What `qqq-bom` manages today

`qqq-bom/pom.xml` (coordinates: `com.kingsrook.qqq:qqq-bom-pom`, version: `${revision}` → currently `0.42.0-SNAPSHOT`) declares a `<dependencyManagement>` block with exactly **11 artifacts**, all internal qqq modules:

| # | Artifact | GroupId | Version mechanism |
|---|---|---|---|
| 1 | qqq-backend-core | com.kingsrook.qqq | `${revision}` |
| 2 | qqq-backend-module-rdbms | com.kingsrook.qqq | `${revision}` |
| 3 | qqq-backend-module-mongodb | com.kingsrook.qqq | `${revision}` |
| 4 | qqq-backend-module-api | com.kingsrook.qqq | `${revision}` |
| 5 | qqq-backend-module-filesystem | com.kingsrook.qqq | `${revision}` |
| 6 | qqq-middleware-javalin | com.kingsrook.qqq | `${revision}` |
| 7 | qqq-middleware-slack | com.kingsrook.qqq | `${revision}` |
| 8 | qqq-middleware-api | com.kingsrook.qqq | `${revision}` |
| 9 | qqq-middleware-picocli | com.kingsrook.qqq | `${revision}` |
| 10 | qqq-openapi | com.kingsrook.qqq | `${revision}` |
| 11 | qqq-language-support-javascript | com.kingsrook.qqq | `${revision}` |

**Version-control mechanism:** every managed dependency resolves `${revision}` — i.e., the BOM version and every managed artifact version are locked to the same number. `flatten-maven-plugin` substitutes `${revision}` with the concrete value at publish time (resolved by `qqq-parent-project` via the root `pom.xml`).

**README.md scope statement:** `qqq-bom/README.md` exists; its stated scope (per the `<description>` tag) is "Bill of Materials for aligning QQQ module versions" — i.e., same-reactor alignment only. No stated ambition to manage QBits, frontends, or tooling.

---

## 2 · What `qqq-bom` does NOT manage (but exists in the ecosystem)

### 2.1 · Internal qqq modules missing from BOM

The QQQ root `pom.xml` `<modules>` section lists **17 modules** (including `qqq-bom` itself). The BOM manages 11 of them. **6 internal modules are not in the BOM** (and `qqq-bom` itself isn't self-managed, which is typical):

| Module | In root poml? | In BOM? | Published to Maven Central? |
|---|---|---|---|
| qqq-backend-module-sqlite | ✓ | ✗ | (inferred yes; follows pattern) |
| qqq-backend-module-postgres | ✓ | ✗ | (inferred yes; follows pattern) |
| qqq-middleware-lambda | ✓ | ✗ | (inferred yes; follows pattern) |
| qqq-middleware-health | ✓ | ✗ | (inferred yes; follows pattern) |
| qqq-utility-lambdas | ✓ | ✗ | (inferred yes; follows pattern) |
| qqq-bom | ✓ | — (self) | ✓ |

### 2.2 · QBits not managed by qqq-bom

**All 20 QBits** at `/qrun-io/qbit-*/` are absent from `qqq-bom`. Detail by repo:

| QBit repo | GroupId | ArtifactId | Parent (qbit-build-parent version) |
|---|---|---|---|
| qbit-bom (this repo IS the parent) | com.kingsrook | qbit-build-parent | — defines `revision=1.6.0` |
| qbit-crm | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-custom-apps | com.kingsrook.qbits | qbit-custom-apps | 1.4.0 |
| qbit-customizable-table-views | com.kingsrook.qbits | (inherits) | 1.5.1 |
| qbit-easypost-tracking | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-geo-data | com.kingsrook.qbits | (inherits) | 1.5.1 |
| qbit-middleware-mcp | com.kingsrook.qbits | (inherits) | 1.5.1 |
| qbit-quick-search | com.kingsrook.qbits | (inherits) | **1.6.0** (newest) |
| qbit-session-store | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-sftp-data-integration | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-standard-process-trace | com.kingsrook.qbits | (inherits) | **1.1.0** (oldest) |
| qbit-template | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-template-application | com.kingsrook.qbits | qbit-example-app | — (uses `${qqq.version}`) |
| qbit-template-data | com.kingsrook.qbits | qbit-example-data | — (uses `${qqq.version}`) |
| qbit-template-extension | com.kingsrook.qbits | qbit-example-extension | — (uses `${qqq.version}=0.35.0`) |
| qbit-user-role-permissions | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-webhooks | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-wms | com.kingsrook.qbits | (inherits) | 1.4.0 |
| qbit-workflows | com.kingsrook.qbits | (inherits) | 1.5.1 |
| qbit-worm-audit | com.kingsrook.qbits | qbit-example-extension (duplicate) | — (uses `${qqq.version}=0.35.0`) |

### 2.3 · Tooling not managed by qqq-bom

- **qctl** (`/qrun-io/qctl/`): groupId `io.qrun.qctl`, packaging `pom`, own multi-module build (`qctl-shared`, `qctl-core`, `qctl-qqq`, `qctl-qbit`, `qctl-qrun`, `qctl-qstudio`, `qctl-cli`, `qctl-integration-tests`). `revision = 0.2.1`. Does not parent from qqq or qbit-build-parent; declares its own properties and imports `jackson-bom` + `testcontainers-bom` directly.
- **qqq-orb** (CircleCI orb, not a Maven artifact — out of Maven BOM scope).
- **qqq-maven-registry** (infrastructure, not a Maven artifact).
- **IntelliJ plugins** (`intellij-commentator-plugin`, `qqq-app-developer-intellij-plugin`) — likely not Maven or different build system; not surveyed.

### 2.4 · Frontends (TS/React)

- **qqq-frontend-material-dashboard** (qfmd) — npm package, not applicable to a Maven BOM.
- **qqq-frontend-next** (qnext) — npm.
- **qqq-frontend-core** (qfc) — npm.

A Maven BOM cannot manage these directly. Maven consumers that embed a frontend bundle (e.g., `qqq-sample-project`) depend on the frontend via a property such as `qqq.versions.frontendMaterialDashboard` — these properties are NOT in the BOM today.

---

## 3 · Gap analysis table

| Artifact | In BOM? | Applicable? | Notes |
|---|---|---|---|
| qqq-backend-core | ✓ | ✓ | — |
| qqq-backend-module-rdbms | ✓ | ✓ | — |
| qqq-backend-module-mongodb | ✓ | ✓ | — |
| qqq-backend-module-api | ✓ | ✓ | — |
| qqq-backend-module-filesystem | ✓ | ✓ | — |
| qqq-backend-module-sqlite | ✗ | ✓ | **gap** — in root poml, missing from BOM |
| qqq-backend-module-postgres | ✗ | ✓ | **gap** — in root poml, missing from BOM |
| qqq-middleware-javalin | ✓ | ✓ | — |
| qqq-middleware-slack | ✓ | ✓ | — |
| qqq-middleware-api | ✓ | ✓ | — |
| qqq-middleware-picocli | ✓ | ✓ | — |
| qqq-middleware-lambda | ✗ | ✓ | **gap** — in root poml, missing from BOM |
| qqq-middleware-health | ✗ | ✓ | **gap** — in root poml, missing from BOM |
| qqq-utility-lambdas | ✗ | ✓ | **gap** — in root poml, missing from BOM |
| qqq-openapi | ✓ | ✓ | — |
| qqq-language-support-javascript | ✓ | ✓ | — |
| qbit-build-parent | ✗ | ✓ | **gap** — drives qbit ecosystem but not BOM-managed |
| qbit-crm | ✗ | ✓ | **gap** — downstream consumers cannot `dependencyManagement import` to resolve qbit version |
| qbit-custom-apps | ✗ | ✓ | **gap** |
| qbit-customizable-table-views | ✗ | ✓ | **gap** |
| qbit-easypost-tracking | ✗ | ✓ | **gap** |
| qbit-geo-data | ✗ | ✓ | **gap** |
| qbit-middleware-mcp | ✗ | ✓ | **gap** |
| qbit-quick-search | ✗ | ✓ | **gap** |
| qbit-session-store | ✗ | ✓ | **gap** |
| qbit-sftp-data-integration | ✗ | ✓ | **gap** |
| qbit-standard-process-trace | ✗ | ✓ | **gap** |
| qbit-user-role-permissions | ✗ | ✓ | **gap** |
| qbit-webhooks | ✗ | ✓ | **gap** |
| qbit-wms | ✗ | ✓ | **gap** |
| qbit-workflows | ✗ | ✓ | **gap** |
| qbit-worm-audit | ✗ | ✓ | **gap** — currently a duplicate of qbit-template-extension (both declare artifactId `qbit-example-extension`) |
| qbit-template-application | ✗ | ✓ | **gap** — qbit-example-app, template |
| qbit-template-data | ✗ | ✓ | **gap** — qbit-example-data, template |
| qbit-template-extension | ✗ | ✓ | **gap** — qbit-example-extension, template |
| qbit-template | ✗ | ✓ | **gap** |
| qctl-cli | ✗ | ✓ | **gap** — standalone CLI artifact, would be useful in BOM if aligned to qqq versioning |
| qqq-frontend-material-dashboard | ✗ | ✗ | npm, not applicable to Maven BOM; but a **version property** could be added |
| qqq-frontend-next | ✗ | ✗ | npm; same as above |
| qqq-frontend-core | ✗ | ✗ | npm; same as above |
| qqq-app-starter | ✗ | (artifact `groupId=com.kingsrook`, `version=0.1-SNAPSHOT`) | Not published to Maven Central (artifact-only consumer) |
| new-qqq-application-template | ✗ | (no top-level pom — different structure) | Template, not a Maven artifact |

**Summary counts:**

- Internal qqq modules, in-BOM: **11**, not in BOM: **5 gaps** (sqlite, postgres, lambda, health, utility-lambdas)
- QBit ecosystem modules, in-BOM: **0**, not in BOM: **21 gaps** (qbit-build-parent + 20 QBit repos)
- Tooling, in-BOM: **0**, not in BOM: **1 gap** (qctl-cli)
- Frontend version properties, in-BOM: **0**, not in BOM: **3** (qfmd, qnext, qfc)

---

## 4 · Version-coordination risk surface

Four concrete facts from the data:

### Fact 1 — QBits are parented to **four different versions** of `qbit-build-parent`

| qbit-build-parent version | Count of QBits | QBit repos |
|---|---|---|
| **1.1.0** | 1 | qbit-standard-process-trace |
| **1.4.0** | 9 | qbit-crm, qbit-custom-apps, qbit-easypost-tracking, qbit-session-store, qbit-sftp-data-integration, qbit-template, qbit-user-role-permissions, qbit-webhooks, qbit-wms |
| **1.5.1** | 4 | qbit-customizable-table-views, qbit-geo-data, qbit-middleware-mcp, qbit-workflows |
| **1.6.0** | 1 | qbit-quick-search |
| (no qbit-build-parent) | 5 | qbit-bom (it IS the parent), qbit-template-{application,data,extension}, qbit-worm-audit |

Since each `qbit-build-parent` release pins its own `qqq-bom-pom` version, QBits are transitively pinned to different qqq versions. The current `qbit-build-parent@1.6.0` (from `qbit-bom/pom.xml`) imports `qqq-bom-pom:0.40.0`.

### Fact 2 — Template QBits pin `qqq.version = 0.35.0` directly

`qbit-template-extension/pom.xml` and `qbit-worm-audit/pom.xml` both declare `<qqq.version>0.35.0</qqq.version>` and depend on `qqq-backend-core` at `${qqq.version}`. These are template artifacts (`artifactId=qbit-example-extension`) — a new QBit generated from either template inherits the 0.35.0 pin. 0.35.0 is older than the 0.40.0 that `qbit-build-parent@1.6.0` pins.

### Fact 3 — `qqq-app-starter` is pinned to a **stale SNAPSHOT**

`qqq-app-starter/pom.xml` declares:

- `qqq.versions.bom = 0.24.0-20250106-145452` (timestamped SNAPSHOT of qqq-bom 0.24.0)
- `qqq.versions.frontendMaterialDashboard = 0.24.0-20241122.223138-1` (timestamped SNAPSHOT of qfmd 0.24.0)

Both significantly behind: qqq itself is now `0.42.0-SNAPSHOT` on develop. A new user cloning `qqq-app-starter` starts on qqq 0.24.0, not the current release line.

### Fact 4 — No repo declares compatibility ranges

No `pom.xml` surveyed uses Maven version ranges (`[0.40.0,0.43.0)` syntax) for the qqq dependency. Every pin is a single exact version. Where README files exist (e.g., `qqq-bom/README.md`, `qbit-bom/pom.xml` description), they describe the artifact's purpose but do not document qqq-version compatibility ranges. No `SUPPORTED-VERSIONS.md` / `COMPATIBILITY.md` file found across the surveyed repos.

### Fact 5 — `qbit-worm-audit` is a forked-but-not-customized copy of `qbit-template-extension`

Both declare identical `<groupId>`, `<artifactId>` (`qbit-example-extension`), `<version>`, `<name>`, `<description>`, and `<qqq.version>`. Appears to be a clone in progress.

### Fact 6 — GroupId inconsistency across the ecosystem

- `com.kingsrook.qqq` — core qqq modules
- `com.kingsrook` — qqq-app-starter, qbit-build-parent
- `com.kingsrook.qbits` — all QBit artifacts
- `io.qrun.qctl` — qctl
- (no groupId in BOM) — qqq-frontend-material-dashboard (npm)

Four different groupIds for closely-related artifacts means a single-BOM "import this, get everything" story needs to bridge at least three `com.kingsrook*` namespaces and (if tooling comes under the BOM) `io.qrun.qctl`.

---

## 5 · Structural gap — what's missing to make `qqq-bom` a release-train backbone

Based strictly on the data observed:

### 5.1 — Missing managed artifacts

- 5 internal qqq modules (sqlite, postgres, lambda, health, utility-lambdas) — trivial addition; same `${revision}` mechanism applies.
- 20 QBits + qbit-build-parent + qctl — **not trivial**, because these cross-repo artifacts cannot use the local `${revision}` property. Each requires either an explicit version declaration in the BOM or a chained BOM import.

### 5.2 — Missing version properties for cross-language artifacts

- No `qqq.versions.frontendMaterialDashboard` / `...Next` / `...Core` property declared in `qqq-bom/pom.xml`. Downstream consumers (like `qqq-app-starter`) define these themselves, leading to drift.

### 5.3 — The `${revision}` property does not extend cross-repo

`${revision}` is a Maven reactor-scoped property. When `qqq-bom` is consumed from outside its own build, `${revision}` resolves to the version flattened at publish time (e.g., `4.0.0`). That works for qqq-core modules (they share the property). It does NOT work for QBits or qctl, which live in different repos with independent `${revision}` declarations. Cross-repo BOM management requires one of:

- **Explicit version per artifact** in the BOM (e.g., `<version>1.6.0</version>` for `qbit-build-parent`).
- **Chained BOM import** — `qqq-bom` imports `qbit-bom` in its `<dependencyManagement>` (which `qbit-bom` would need to become — today it only declares the build parent, not a full qbit BOM of published artifacts).
- **Named version properties** for each external-repo artifact, manually bumped per release (e.g., `qbit.versions.build-parent=1.6.0`).

### 5.4 — Duplication of version declarations

Currently `qqq-app-starter/pom.xml` declares its own `qqq.versions.bom` and `qqq.versions.frontendMaterialDashboard`. The starter is already IMPORTING `qqq-bom` (via `<type>pom</type><scope>import</scope>`), so these property declarations effectively duplicate a decision that could live authoritatively in the BOM.

### 5.5 — No release-train naming / compatibility contract

Spring Cloud publishes its BOM with named release trains (Hoxton, Ilford, Jubilee). Each name maps to a tested-compatible set of component versions. `qqq-bom` has no such naming; a consumer looking at the BOM version sees the same version as qqq itself, which doesn't communicate "this version of the BOM was tested against QBit versions X Y Z."

---

## 6 · Raw data appendix

### 6.1 — Full `qqq-bom/pom.xml` `<dependencyManagement>`

```xml
<dependencyManagement>
   <dependencies>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-backend-core</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-backend-module-rdbms</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-backend-module-mongodb</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-backend-module-api</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-backend-module-filesystem</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-middleware-javalin</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-middleware-slack</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-middleware-api</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-openapi</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-middleware-picocli</artifactId><version>${revision}</version></dependency>
      <dependency><groupId>com.kingsrook.qqq</groupId><artifactId>qqq-language-support-javascript</artifactId><version>${revision}</version></dependency>
   </dependencies>
</dependencyManagement>
```

### 6.2 — QBit inventory with parent versions (already in section 4, Fact 1)

See table above.

### 6.3 — `qbit-bom/pom.xml` (the `qbit-build-parent` artifact) imports

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.kingsrook.qqq</groupId>
      <artifactId>qqq-bom-pom</artifactId>
      <version>0.40.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>org.junit</groupId>
      <artifactId>junit-bom</artifactId>
      <version>6.0.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Current `qbit-build-parent` version: **1.6.0** (declared `<revision>1.6.0</revision>`).

### 6.4 — `qqq-app-starter/pom.xml` version properties

```xml
<properties>
  <qqq.versions.bom>0.24.0-20250106-145452</qqq.versions.bom>
  <qqq.versions.frontendMaterialDashboard>0.24.0-20241122.223138-1</qqq.versions.frontendMaterialDashboard>
</properties>
```

### 6.5 — `qbit-template-extension/pom.xml` qqq pin

```xml
<properties>
  <qqq.version>0.35.0</qqq.version>
</properties>
<dependencies>
  <dependency>
    <groupId>com.kingsrook.qqq</groupId>
    <artifactId>qqq-backend-core</artifactId>
    <version>${qqq.version}</version>
  </dependency>
</dependencies>
```

Identical structure in `qbit-worm-audit/pom.xml` (duplicate fork).

---

*Audit complete. No recommendations — decisions for the maintainer.*
